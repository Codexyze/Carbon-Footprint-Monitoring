import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { CreateFactorRequest, Factor } from "@/types/factors/factorstypes";
import { Logger,LogTags } from "@/lib/logger";

/** Create a new factor */
export async function createFactor(payload: CreateFactorRequest): Promise<Factor> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.FACTORS, `Creating new factor [${requestId}]`);
    Logger.d(LogTags.FACTORS, `Factor data - category: ${payload.category}, geography: ${payload.geography || 'GLOBAL'}, factor_value: ${payload.factor_value}`);

    // Validate request payload
    if (!payload.category || payload.category.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty factor category");
      throw new Error("Factor category is required");
    }

    if (!payload.unit_in || payload.unit_in.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty factor unit_in");
      throw new Error("Factor unit_in is required");
    }

    if (!payload.unit_out || payload.unit_out.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty factor unit_out");
      throw new Error("Factor unit_out is required");
    }

    if (typeof payload.factor_value !== 'number' || payload.factor_value < 0) {
      Logger.w(LogTags.FACTORS, "Invalid factor value");
      throw new Error("Factor value must be a non-negative number");
    }

    if (!payload.vendor || payload.vendor.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty factor vendor");
      throw new Error("Factor vendor is required");
    }

    if (!payload.method || payload.method.trim().length === 0) {
      Logger.w(LogTags.FACTORS, "Missing or empty factor method");
      throw new Error("Factor method is required");
    }

    if (!payload.valid_from) {
      Logger.w(LogTags.FACTORS, "Missing factor valid_from date");
      throw new Error("Factor valid_from date is required");
    }

    if (!payload.valid_to) {
      Logger.w(LogTags.FACTORS, "Missing factor valid_to date");
      throw new Error("Factor valid_to date is required");
    }

    Logger.d(LogTags.FACTORS, `Making API call to /v1/factors [${requestId}]`);
    
    // Make API call
    const res = await apiClient.post<Factor>("/v1/factors", payload);
    
    Logger.i(LogTags.FACTORS, `Factor created successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.FACTORS, `Created factor: ${res.data.category}, ID: ${res.data.id}, Value: ${res.data.factor_value}, Units: ${res.data.unit_in} -> ${res.data.unit_out}`);

    // Validate response structure
    if (!res.data.id || !res.data.category || typeof res.data.factor_value !== 'number') {
      Logger.e(LogTags.FACTORS, `Invalid response: missing required factor data [${requestId}]`);
      throw new Error("Invalid response from server: incomplete factor data");
    }

    Logger.i(LogTags.FACTORS, `New factor validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.FACTORS, `Create factor failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
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
