import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { SummaryResponse } from "@/types/analytics/analyticstypes";
import { Logger, LogTags } from "@/lib/logger";


export async function getSummary(): Promise<SummaryResponse> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.ANALYTICS, `Getting analytics summary [${requestId}]`);
    Logger.d(LogTags.ANALYTICS, `Making API call to /v1/analytics/summary [${requestId}]`);
    
    const res = await apiClient.get<SummaryResponse>("/v1/analytics/summary");
    
    Logger.i(LogTags.ANALYTICS, `Analytics summary retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.ANALYTICS, `Summary data: facilities_count: ${res.data.facilities_count || 'N/A'}, total_co2e_kg: ${res.data.total_co2e_kg || 'N/A'}, last_event_at: ${res.data.last_event_at || 'N/A'}`);

    // Validate response structure
    if (typeof res.data !== 'object' || res.data === null) {
      Logger.e(LogTags.ANALYTICS, `Invalid response: summary data is not an object [${requestId}]`);
      throw new Error("Invalid response from server: summary data should be an object");
    }

    if (typeof res.data.total_co2e_kg !== 'number') {
      Logger.e(LogTags.ANALYTICS, `Invalid response: missing or invalid total_co2e_kg [${requestId}]`);
      throw new Error("Invalid response from server: missing total_co2e_kg");
    }

    Logger.i(LogTags.ANALYTICS, `Analytics summary validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.ANALYTICS, `Get summary failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.ANALYTICS, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.ANALYTICS, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Minimal route handlers to satisfy Next.js route type validation.
// These modules are helper libraries and not intended to be used as API routes.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
