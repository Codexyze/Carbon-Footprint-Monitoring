import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { IngestRequest,IngestResponse } from "@/types/ingest/ingesttypes";
import { Logger, LogTags } from "@/lib/logger";

/** Send a batch of events to the ingest endpoint */
export async function ingestEvents(payload: IngestRequest): Promise<IngestResponse> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.INGEST, `Ingesting events batch [${requestId}]`);
    Logger.d(LogTags.INGEST, `Payload contains ${payload.events?.length || 0} events`);

    // Validate payload structure
    if (!payload || typeof payload !== 'object') {
      Logger.w(LogTags.INGEST, "Invalid ingest payload: not an object");
      throw new Error("Ingest payload must be an object");
    }

    if (!payload.events || !Array.isArray(payload.events)) {
      Logger.w(LogTags.INGEST, "Invalid ingest payload: missing or invalid events array");
      throw new Error("Ingest payload must contain an events array");
    }

    if (payload.events.length === 0) {
      Logger.w(LogTags.INGEST, "Empty events array in ingest payload");
      throw new Error("Events array cannot be empty");
    }

    if (payload.events.length > 1000) {
      Logger.w(LogTags.INGEST, `Large batch size: ${payload.events.length} events`);
      Logger.i(LogTags.INGEST, `Proceeding with large batch [${requestId}]`);
    }

    // Validate sample events
    const sampleEvent = payload.events[0];
    if (!sampleEvent.facility_id || !sampleEvent.category || !sampleEvent.occurred_at) {
      Logger.w(LogTags.INGEST, "Invalid event structure: missing required fields");
      throw new Error("Events must contain facility_id, category, and occurred_at");
    }

    // Log sample event details (anonymized)
    Logger.d(LogTags.INGEST, `Sample event - facility_id: ${sampleEvent.facility_id}, category: ${sampleEvent.category}, occurred_at: ${sampleEvent.occurred_at}`);

    Logger.d(LogTags.INGEST, `Making API call to /v1/ingest/events [${requestId}]`);
    
    const res = await apiClient.post<IngestResponse>("/v1/ingest/events", payload);
    
    Logger.i(LogTags.INGEST, `Events ingested successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.INGEST, `Response: created_events: ${res.data.created_events}, skipped_duplicates: ${res.data.skipped_duplicates}, created_emissions: ${res.data.created_emissions}`);

    // Validate response structure
    if (typeof res.data !== 'object' || res.data === null) {
      Logger.e(LogTags.INGEST, `Invalid response: data is not an object [${requestId}]`);
      throw new Error("Invalid response from server: response should be an object");
    }

    if (typeof res.data.created_events !== 'number') {
      Logger.e(LogTags.INGEST, `Invalid response: missing or invalid created_events field [${requestId}]`);
      throw new Error("Invalid response from server: missing created_events field");
    }

    if (res.data.created_events === 0 && res.data.skipped_duplicates === 0) {
      Logger.w(LogTags.INGEST, `No events created or skipped - possible processing issue [${requestId}]`);
    }

    Logger.i(LogTags.INGEST, `Events ingest validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.INGEST, `Ingest events failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.INGEST, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.INGEST, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
