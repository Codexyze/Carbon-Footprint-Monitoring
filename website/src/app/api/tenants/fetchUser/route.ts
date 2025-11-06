import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { TenantUser } from "@/types/tenants/tenantstypes";
import { Logger, LogTags } from "@/lib/logger";

/** Fetch tenant users */
export async function fetchUsers(): Promise<TenantUser[]> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.TENANTS, `Fetching tenant users list [${requestId}]`);
    Logger.d(LogTags.TENANTS, `Making API call to /v1/tenants/users [${requestId}]`);
    
    const res = await apiClient.get<TenantUser[]>('/v1/tenants/users');
    
    Logger.i(LogTags.TENANTS, `Tenant users retrieved successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.TENANTS, `Response: ${res.data.length} users found`);

    // Validate response structure
    if (!Array.isArray(res.data)) {
      Logger.e(LogTags.TENANTS, `Invalid response: data is not an array [${requestId}]`);
      throw new Error("Invalid response from server: users data should be an array");
    }

    // Log user details if any (with email masking)
    if (res.data.length > 0) {
      Logger.d(LogTags.TENANTS, `Sample user: ${Logger.maskEmail(res.data[0].email || 'unknown')}, Role: ${res.data[0].role || 'unknown'}`);
    }

    Logger.i(LogTags.TENANTS, `Tenant users list validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.TENANTS, `Fetch users failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.TENANTS, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.TENANTS, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
