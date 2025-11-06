import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { KpisResponse } from "@/types/analytics/analyticstypes";
import { Logger,LogTags } from "@/lib/logger";

export async function getKpis(from: string, to: string): Promise<KpisResponse> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.ANALYTICS, `Getting KPIs [${requestId}]`);
    Logger.d(LogTags.ANALYTICS, `Date range - from: ${from}, to: ${to}`);

    // Validate date parameters
    if (!from || !to) {
      Logger.w(LogTags.ANALYTICS, "Missing date parameters for KPIs");
      throw new Error("Both 'from' and 'to' dates are required");
    }

    if (!isValidDateString(from) || !isValidDateString(to)) {
      Logger.w(LogTags.ANALYTICS, "Invalid date format for KPIs");
      throw new Error("Dates must be in valid format (YYYY-MM-DD or ISO-8601)");
    }

    const fromDate = new Date(from);
    const toDate = new Date(to);
    
    if (fromDate >= toDate) {
      Logger.w(LogTags.ANALYTICS, "Invalid date range for KPIs: from date is not before to date");
      throw new Error("'from' date must be before 'to' date");
    }

    const params = new URLSearchParams();
    params.append('from', from);
    params.append('to', to);
    
    const url = `/v1/analytics/kpis?${params.toString()}`;
    Logger.d(LogTags.ANALYTICS, `Making API call to ${url} [${requestId}]`);
    
    const res = await apiClient.get<KpisResponse>(url);
    
    Logger.i(LogTags.ANALYTICS, `KPIs retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.ANALYTICS, `KPIs data: total_co2e_kg: ${res.data.total_co2e_kg || 'N/A'}, scope1_kg: ${res.data.scope1_kg || 'N/A'}, scope2_kg: ${res.data.scope2_kg || 'N/A'}, scope3_kg: ${res.data.scope3_kg || 'N/A'}`);

    // Validate response structure
    if (typeof res.data !== 'object' || res.data === null) {
      Logger.e(LogTags.ANALYTICS, `Invalid response: KPIs data is not an object [${requestId}]`);
      throw new Error("Invalid response from server: KPIs data should be an object");
    }

    if (typeof res.data.total_co2e_kg !== 'number') {
      Logger.e(LogTags.ANALYTICS, `Invalid response: missing or invalid total_co2e_kg [${requestId}]`);
      throw new Error("Invalid response from server: missing total_co2e_kg");
    }

    Logger.i(LogTags.ANALYTICS, `KPIs data validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.ANALYTICS, `Get KPIs failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.ANALYTICS, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.ANALYTICS, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Helper function to validate date strings
function isValidDateString(dateString: string): boolean {
  // Accept both YYYY-MM-DD and ISO-8601 formats
  const dateOnlyRegex = /^\d{4}-\d{2}-\d{2}$/;
  const isoDateRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z?$/;
  
  if (!dateOnlyRegex.test(dateString) && !isoDateRegex.test(dateString)) {
    return false;
  }
  
  const date = new Date(dateString);
  return !isNaN(date.getTime());
}

// Minimal route handlers to satisfy Next.js route type validation.
// These modules are helper libraries and not intended to be used as API routes.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
