import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { FactorPreview } from "@/types/factors/factorstypes";
import {Logger, LogTags} from "@/lib/logger";

/** Get factor preview for specific category and time */
export async function previewFactors(params: {
  category: string;
  occurred_at: string; // ISO-8601
  geography?: string;
}): Promise<FactorPreview> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.FACTORS, `Getting factor preview [${requestId}]`);
    Logger.d(LogTags.FACTORS, `Preview params - category: ${params.category}, occurred_at: ${params.occurred_at}, geography: ${params.geography || 'default'}`);

    // Validate required parameters
    if (!params.category || params.category.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty category for preview");
      throw new Error("Category is required for factor preview");
    }

    if (!params.occurred_at || !isValidISODate(params.occurred_at)) {
      Logger.w(LogTags.FACTORS, "Missing or invalid occurred_at date for preview");
      throw new Error("occurred_at must be a valid ISO-8601 date string");
    }

    const searchParams = new URLSearchParams();
    searchParams.append('category', params.category);
    searchParams.append('occurred_at', params.occurred_at);
    if (params.geography) searchParams.append('geography', params.geography);

    // Construct API URL
    const url = `/v1/factors/preview?${searchParams.toString()}`;
    Logger.d(LogTags.FACTORS, `Making API call to ${url} [${requestId}]`);
    
    const res = await apiClient.get<FactorPreview>(url);
    
    Logger.i(LogTags.FACTORS, `Factor preview retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.FACTORS, `Preview data: category: ${res.data.category}, factor_value: ${res.data.factor_value}, geography: ${res.data.geography}`);

    // Validate response structure
    if (!res.data.category || typeof res.data.factor_value !== 'number' || !res.data.geography) {
      Logger.e(LogTags.FACTORS, `Invalid response: missing required preview data [${requestId}]`);
      throw new Error("Invalid response from server: incomplete factor preview data");
    }

    Logger.i(LogTags.FACTORS, `Factor preview validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.FACTORS, `Factor preview failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
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
