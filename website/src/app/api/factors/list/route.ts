import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import  type{ Factor } from "@/types/factors/factorstypes";
import { Logger,LogTags } from "@/lib/logger";

/** List all factors with optional filters */
export async function listFactors(params?: {
  category?: string;
  geography?: string;
  valid_on?: string; // ISO-8601
}): Promise<Factor[]> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.FACTORS, `Listing factors [${requestId}]`);
    Logger.d(LogTags.FACTORS, `Filter params - category: ${params?.category || 'all'}, geography: ${params?.geography || 'all'}, valid_on: ${params?.valid_on || 'any'}`);

    // Validate filter parameters
    if (params?.valid_on && !isValidISODate(params.valid_on)) {
      Logger.w(LogTags.FACTORS, "Invalid valid_on date format provided");
      throw new Error("valid_on must be a valid ISO-8601 date string");
    }

    const searchParams = new URLSearchParams();
    if (params?.category) searchParams.append('category', params.category);
    if (params?.geography) searchParams.append('geography', params.geography);
    if (params?.valid_on) searchParams.append('valid_on', params.valid_on);
    
    // Make API call
    const url = `/v1/factors${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
    Logger.d(LogTags.FACTORS, `Making API call to ${url} [${requestId}]`);
    
    const res = await apiClient.get<Factor[]>(url);
    
    Logger.i(LogTags.FACTORS, `Factors retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.FACTORS, `Response: ${res.data.length} factors found`);

    // Validate response structure
    if (!Array.isArray(res.data)) {
      Logger.e(LogTags.FACTORS, `Invalid response: data is not an array [${requestId}]`);
      throw new Error("Invalid response from server: factors data should be an array");
    }

    // Log sample factor details if any
    if (res.data.length > 0) {
      Logger.d(LogTags.FACTORS, `Sample factor: ${res.data[0].category}, Value: ${res.data[0].factor_value}, Units: ${res.data[0].unit_in} -> ${res.data[0].unit_out}, Geography: ${res.data[0].geography}`);
    }

    Logger.i(LogTags.FACTORS, `Factors list validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.FACTORS, `List factors failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.FACTORS, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.FACTORS, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Helper function to validate ISO date strings
function isValidISODate(dateString: string): boolean {
  const isoDateRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z?$/;
  if (!isoDateRegex.test(dateString)) {
    return false;
  }
  
  const date = new Date(dateString);
  return !isNaN(date.getTime());
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
