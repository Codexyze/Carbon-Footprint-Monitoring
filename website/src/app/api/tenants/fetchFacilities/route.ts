import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type {Facility} from "@/types/tenants/tenantstypes";
import { Logger,LogTags } from "@/lib/logger";


/** Fetch list of facilities for the current tenant */
export async function fetchFacilities(): Promise<Facility[]> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.FACILITIES, `Fetching facilities list [${requestId}]`);
    Logger.d(LogTags.FACILITIES, `Making API call to /v1/tenants/facilities [${requestId}]`);
    
    const res = await apiClient.get<Facility[]>('/v1/tenants/facilities');
    
    Logger.i(LogTags.FACILITIES, `Facilities retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.FACILITIES, `Response: ${res.data.length} facilities found`);

    // Validate response structure
    if (!Array.isArray(res.data)) {
      Logger.e(LogTags.FACILITIES, `Invalid response: data is not an array [${requestId}]`);
      throw new Error("Invalid response from server: facilities data should be an array");
    }

    // Log facility details if any
    if (res.data.length > 0) {
      Logger.d(LogTags.FACILITIES, `Sample facility: ${res.data[0].name || 'unnamed'}, ID: ${res.data[0].id || 'unknown'}`);
    }

    Logger.i(LogTags.FACILITIES, `Facilities list validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.FACILITIES, `Fetch facilities failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
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
