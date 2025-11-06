import { apiClient } from "@/lib/axios/apiClient";
import {categorizeAxiosError}from "@/lib/errors";
import type {CreateFacilityRequest, Facility} from "@/types/tenants/tenantstypes";
import { Logger,LogTags } from "@/lib/logger";

/** Create a new facility */
export async function createFacility(payload: CreateFacilityRequest): Promise<Facility> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.FACILITIES, `Creating new facility [${requestId}]`);
    Logger.d(LogTags.FACILITIES, `Facility data - name: ${payload.name}, country: ${payload.country || 'not specified'}, grid_region: ${payload.grid_region || 'not specified'}`);

    // Validate request payload
    if (!payload.name || payload.name.trim().length === 0) {
      Logger.w(LogTags.FACILITIES, "Missing or empty facility name");
      throw new Error("Facility name is required");
    }

    if (payload.name.length > 200) {
      Logger.w(LogTags.FACILITIES, "Facility name too long");
      throw new Error("Facility name must be 200 characters or less");
    }

    Logger.d(LogTags.FACILITIES, `Making API call to /v1/tenants/facilities [${requestId}]`);
    
    const res = await apiClient.post<Facility>('/v1/tenants/facilities', payload);
    
    Logger.i(LogTags.FACILITIES, `Facility created successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.FACILITIES, `Created facility: ${res.data.name}, ID: ${res.data.id}`);

    // Validate response structure
    if (!res.data.id || !res.data.name) {
      Logger.e(LogTags.FACILITIES, `Invalid response: missing required facility data [${requestId}]`);
      throw new Error("Invalid response from server: incomplete facility data");
    }

    Logger.i(LogTags.FACILITIES, `New facility validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.FACILITIES, `Create facility failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.FACILITIES, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.FACILITIES, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
