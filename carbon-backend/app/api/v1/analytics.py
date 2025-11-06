from __future__ import annotations
from dotenv import load_dotenv
import os
load_dotenv()
from datetime import datetime
from typing import Annotated, Literal, Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from sqlalchemy import func, select, case
from sqlalchemy.orm import Session

from app.core.auth import require_role
from app.db.database import get_db
from app.db.models import ActivityEvent, Emission, EmissionFactor, Facility, Organization, User
from app.services.analytics.queries import kpis as kpis_query, last_event_time
from app.utils.time import parse_dt
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI


router = APIRouter(prefix="/v1/analytics", tags=["analytics"])


class KPIsOut(BaseModel):
    total_co2e_kg: float
    scope1_kg: float
    scope2_kg: float
    scope3_kg: float


@router.get("/kpis", response_model=KPIsOut)
def kpis(db: Session = Depends(get_db), user: Annotated[User, Depends(require_role("viewer", "analyst", "admin"))] = None, from_: str = Query(alias="from"), to: str = Query()):
    start = parse_dt(from_)
    end = parse_dt(to)
    if end <= start:
        raise HTTPException(status_code=400, detail="to must be after from")
    data = kpis_query(db, org_id=user.org_id, date_from=start, date_to=end)
    return KPIsOut(**data)


class TrendPoint(BaseModel):
    period: str
    co2e_kg: float


@router.get("/trend", response_model=list[TrendPoint])
def trend(
    db: Session = Depends(get_db),
    user: Annotated[User, Depends(require_role("viewer", "analyst", "admin"))] = None,
    grain: Literal["day", "month"] = Query(default="day"),
    from_: str = Query(alias="from"),
    to: str = Query(),
):
    start = parse_dt(from_)
    end = parse_dt(to)
    if end <= start:
        raise HTTPException(status_code=400, detail="to must be after from")

    # Group emissions by event.occurred_at truncated to day/month
    trunc_func = func.date_trunc("day", ActivityEvent.occurred_at) if grain == "day" else func.date_trunc("month", ActivityEvent.occurred_at)
    stmt = (
        select(trunc_func.label("period"), func.coalesce(func.sum(Emission.co2e_kg), 0))
        .select_from(Emission)
        .join(ActivityEvent, ActivityEvent.id == Emission.event_id)
        .where(Emission.org_id == user.org_id)
        .where(ActivityEvent.occurred_at >= start)
        .where(ActivityEvent.occurred_at < end)
        .group_by("period")
        .order_by("period")
    )
    rows = list(db.execute(stmt))
    return [TrendPoint(period=r[0].date().isoformat() if grain == "day" else r[0].date().replace(day=1).isoformat(), co2e_kg=float(r[1] or 0)) for r in rows]


class SummaryOut(BaseModel):
    total_co2e_kg: float
    scope1_kg: float
    scope2_kg: float
    scope3_kg: float
    facilities_count: int
    last_event_at: Optional[datetime]
    top_categories: list[tuple[str, float]]


@router.get("/summary")
def summary(id: int = Query(..., description="Organization ID"), db: Session = Depends(get_db), user: Annotated[User, Depends(require_role("viewer", "analyst", "admin"))] = None):
    org_id = id
    
    totals_stmt = select(
        func.coalesce(func.sum(Emission.co2e_kg), 0),
        func.coalesce(func.sum(case((Emission.scope == "1", Emission.co2e_kg), else_=0)), 0),
        func.coalesce(func.sum(case((Emission.scope == "2", Emission.co2e_kg), else_=0)), 0),
        func.coalesce(func.sum(case((Emission.scope == "3", Emission.co2e_kg), else_=0)), 0),
    ).where(Emission.org_id == org_id)
    total, s1, s2, s3 = db.execute(totals_stmt).one()

    facilities_count = db.scalar(select(func.count()).select_from(Facility).where(Facility.org_id == org_id))
    last_ev = last_event_time(db, org_id=org_id)

    top_stmt = (
        select(ActivityEvent.category, func.coalesce(func.sum(Emission.co2e_kg), 0))
        .join(ActivityEvent, ActivityEvent.id == Emission.event_id)
        .where(Emission.org_id == org_id)
        .group_by(ActivityEvent.category)
        .order_by(func.coalesce(func.sum(Emission.co2e_kg), 0).desc())
        .limit(5)
    )
    top = [(r[0], float(r[1] or 0)) for r in db.execute(top_stmt)]

    summary_dict = {
        "id": org_id,
        "total_co2e_kg": float(total or 0),
        "scope1_kg": float(s1 or 0),
        "scope2_kg": float(s2 or 0),
        "scope3_kg": float(s3 or 0),
        "facilities_count": int(facilities_count or 0),
        "last_event_at": last_ev.isoformat() if last_ev else None,
        "top_categories": [{"category": cat, "co2e_kg": kg} for cat, kg in top],
    }
    
    return summary_dict


@router.get("/suggestions")
def suggestion(id: int = Query(..., description="Organization ID"), db: Session = Depends(get_db), user: Annotated[User, Depends(require_role("viewer", "analyst", "admin"))] = None):
    """Combine all data from all tables for a given organization ID into one comprehensive dictionary."""
    org_id = id
    
    # 1. Get Organization details
    org = db.get(Organization, org_id)
    if not org:
        raise HTTPException(status_code=404, detail=f"Organization {org_id} not found")
    
    # 2. Get all Users for this organization
    users = db.scalars(select(User).where(User.org_id == org_id)).all()
    users_data = [
        {
            "id": u.id,
            "email": u.email,
            "role": u.role,
            "is_active": u.is_active,
            "created_at": u.created_at.isoformat() if u.created_at else None,
        }
        for u in users
    ]
    
    # 3. Get all Facilities for this organization
    facilities = db.scalars(select(Facility).where(Facility.org_id == org_id)).all()
    facilities_data = [
        {
            "id": f.id,
            "name": f.name,
            "country": f.country,
            "grid_region": f.grid_region,
            "created_at": f.created_at.isoformat() if f.created_at else None,
        }
        for f in facilities
    ]
    
    # 4. Get all Activity Events for this organization
    events = db.scalars(select(ActivityEvent).where(ActivityEvent.org_id == org_id).order_by(ActivityEvent.occurred_at.desc())).all()
    events_data = [
        {
            "id": e.id,
            "facility_id": e.facility_id,
            "source_id": e.source_id,
            "occurred_at": e.occurred_at.isoformat() if e.occurred_at else None,
            "category": e.category,
            "subcategory": e.subcategory,
            "unit": e.unit,
            "value_numeric": float(e.value_numeric) if e.value_numeric else 0,
            "currency": e.currency,
            "spend_value": float(e.spend_value) if e.spend_value else None,
            "scope_hint": e.scope_hint,
            "created_at": e.created_at.isoformat() if e.created_at else None,
        }
        for e in events
    ]
    
    # 5. Get all Emissions for this organization
    emissions = db.scalars(
        select(Emission)
        .where(Emission.org_id == org_id)
        .order_by(Emission.created_at.desc())
    ).all()
    emissions_data = [
        {
            "id": em.id,
            "event_id": em.event_id,
            "factor_id": em.factor_id,
            "scope": em.scope,
            "co2e_kg": float(em.co2e_kg) if em.co2e_kg else 0,
            "calc_version": em.calc_version,
            "uncertainty_pct": float(em.uncertainty_pct) if em.uncertainty_pct else None,
            "provenance_json": em.provenance_json,
            "created_at": em.created_at.isoformat() if em.created_at else None,
        }
        for em in emissions
    ]
    
    # 6. Get Emission Factors used by this organization's emissions
    factor_ids = [em.factor_id for em in emissions if em.factor_id]
    unique_factor_ids = list(set(factor_ids))
    factors_data = []
    if unique_factor_ids:
        factors = db.scalars(select(EmissionFactor).where(EmissionFactor.id.in_(unique_factor_ids))).all()
        factors_data = [
            {
                "id": f.id,
                "namespace": f.namespace,
                "category": f.category,
                "unit_in": f.unit_in,
                "unit_out": f.unit_out,
                "factor_value": float(f.factor_value) if f.factor_value else 0,
                "gwp_horizon": int(f.gwp_horizon) if f.gwp_horizon else 100,
                "geography": f.geography,
                "vendor": f.vendor,
                "method": f.method,
                "valid_from": f.valid_from.isoformat() if f.valid_from else None,
                "valid_to": f.valid_to.isoformat() if f.valid_to else None,
                "version": int(f.version) if f.version else 1,
            }
            for f in factors
        ]
    
    # 7. Calculate summary statistics
    totals_stmt = select(
        func.coalesce(func.sum(Emission.co2e_kg), 0),
        func.coalesce(func.sum(case((Emission.scope == "1", Emission.co2e_kg), else_=0)), 0),
        func.coalesce(func.sum(case((Emission.scope == "2", Emission.co2e_kg), else_=0)), 0),
        func.coalesce(func.sum(case((Emission.scope == "3", Emission.co2e_kg), else_=0)), 0),
    ).where(Emission.org_id == org_id)
    total, s1, s2, s3 = db.execute(totals_stmt).one()
    
    last_ev = last_event_time(db, org_id=org_id)
    
    # 8. Combine everything into one comprehensive dictionary
    combined_dict = {
        "id": org_id,
        "organization": {
            "id": org.id,
            "name": org.name,
            "plan": org.plan,
            "created_at": org.created_at.isoformat() if org.created_at else None,
            "updated_at": org.updated_at.isoformat() if org.updated_at else None,
        },
        "users": users_data,
        "facilities": facilities_data,
        "activity_events": events_data,
        "emissions": emissions_data,
        "emission_factors": factors_data,
        "summary": {
            "total_co2e_kg": float(total or 0),
            "scope1_kg": float(s1 or 0),
            "scope2_kg": float(s2 or 0),
            "scope3_kg": float(s3 or 0),
            "users_count": len(users_data),
            "facilities_count": len(facilities_data),
            "events_count": len(events_data),
            "emissions_count": len(emissions_data),
            "last_event_at": last_ev.isoformat() if last_ev else None,
        },
    }

    # Create a prompt template that includes the dictionary
    prompt = ChatPromptTemplate.from_messages([
        (
            "system",
            """You are an expert sustainability and carbon footprint analyst. 
    Your job is to analyze organizational carbon emissions data and provide insights, observations, and recommendations in a clear, structured way.

    The data you receive contains:
    - Organization details
    - Facilities, users, and emission events
    - Total and scope-wise CO₂e emissions (Scope 1, 2, and 3)
    - Emission factors and categories
    - Activity trends (energy, transport, spend)

    Analyze this information thoroughly and produce:
    1. **Overall Assessment**
    - Comment on whether the total and per-scope emissions appear high, moderate, or low.
    - Mention which scope dominates (Scope 1, 2, or 3) and why that might be.
    2. **Key Insights**
    - Highlight notable trends or anomalies (e.g., one facility producing 70% emissions, rapid monthly increase, etc.).
    - Identify any under-reported or missing activity areas.
    3. **Improvement Recommendations**
    - Provide 3–5 practical and measurable steps to reduce emissions (e.g., switch to renewable grid sources, optimize logistics, energy efficiency).
    - Suggest data-quality improvements (e.g., more granular reporting, missing factors).
    4. **Forecast or Target Suggestions**
    - Propose a realistic CO₂e reduction target for next quarter or year.
    - Suggest key KPIs the organization should track.

    Be concise but insightful — use short paragraphs and bullet points where needed. Avoid restating data directly; focus on interpretation and advice."""
        ),
        (
            "user",
            "Here is the organization's combined carbon data:\n\n{product_info}"
        ),
    ])


    # Initialize the LLM
    llm = ChatOpenAI(model="gpt-4o")

    # Invoke the chain with the dictionary
    chain = prompt | llm
    response = chain.invoke({"product_info": combined_dict})
    send = {"message": response.content.strip()}


    return send
