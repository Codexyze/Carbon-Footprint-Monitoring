import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { TrendPoint } from "@/types/analytics/analyticstypes";
import { Logger, LogTags } from "@/lib/logger";


export async function getTrend(from: string, to: string, grain?: 'day' | 'month'): Promise<TrendPoint[]> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.ANALYTICS, `Getting trend data [${requestId}]`);
    Logger.d(LogTags.ANALYTICS, `Trend params - from: ${from}, to: ${to}, grain: ${grain || 'default'}`);

    // Validate date parameters
    if (!from || !to) {
      Logger.w(LogTags.ANALYTICS, "Missing date parameters for trend");
      throw new Error("Both 'from' and 'to' dates are required");
    }

    if (!isValidDateString(from) || !isValidDateString(to)) {
      Logger.w(LogTags.ANALYTICS, "Invalid date format for trend");
      throw new Error("Dates must be in valid format (YYYY-MM-DD or ISO-8601)");
    }

    const fromDate = new Date(from);
    const toDate = new Date(to);
    
    if (fromDate >= toDate) {
      Logger.w(LogTags.ANALYTICS, "Invalid date range for trend: from date is not before to date");
      throw new Error("'from' date must be before 'to' date");
    }

    // Validate grain parameter
    if (grain && !['day', 'month'].includes(grain)) {
      Logger.w(LogTags.ANALYTICS, "Invalid grain parameter for trend");
      throw new Error("Grain must be either 'day' or 'month'");
    }

    const params = new URLSearchParams();
    params.append('from', from);
    params.append('to', to);
    if (grain) params.append('grain', grain);
    
    const url = `/v1/analytics/trend?${params.toString()}`;
    Logger.d(LogTags.ANALYTICS, `Making API call to ${url} [${requestId}]`);
    
    const res = await apiClient.get<TrendPoint[]>(url);
    
    Logger.i(LogTags.ANALYTICS, `Trend data retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.ANALYTICS, `Response: ${res.data.length} trend points found`);

    // Validate response structure
    if (!Array.isArray(res.data)) {
      Logger.e(LogTags.ANALYTICS, `Invalid response: trend data is not an array [${requestId}]`);
      throw new Error("Invalid response from server: trend data should be an array");
    }

    // Log sample trend point if any
    if (res.data.length > 0) {
      Logger.d(LogTags.ANALYTICS, `Sample trend point: period: ${res.data[0].period || 'unknown'}, co2e_kg: ${res.data[0].co2e_kg || 'unknown'}`);
    }

    Logger.i(LogTags.ANALYTICS, `Trend data validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.ANALYTICS, `Get trend failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
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
