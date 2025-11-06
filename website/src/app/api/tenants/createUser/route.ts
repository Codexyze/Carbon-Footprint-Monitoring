import{apiClient}from"@/lib/axios/apiClient";
import{categorizeAxiosError}from"@/lib/errors";
import type{CreateUserRequest,TenantUser}from "@/types/tenants/tenantstypes";
import { Logger,LogTags } from "@/lib/logger";


/** Create a tenant user */
export async function createUser(payload: CreateUserRequest): Promise<TenantUser> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.TENANTS, `Creating new tenant user [${requestId}]`);
    Logger.d(LogTags.TENANTS, `User data - email: ${Logger.maskEmail(payload.email)}, role: ${payload.role || 'default'}`);

    // Validate request payload
    if (!payload.email || payload.email.trim().length === 0) {
      Logger.w(LogTags.TENANTS, "Missing or empty user email");
      throw new Error("User email is required");
    }

    if (!payload.email.includes('@')) {
      Logger.w(LogTags.TENANTS, "Invalid email format provided");
      throw new Error("Valid email address is required");
    }

    if (!payload.password || payload.password.trim().length === 0) {
      Logger.w(LogTags.TENANTS, "Missing or empty user password");
      throw new Error("User password is required");
    }

    if (payload.password.length < 6) {
      Logger.w(LogTags.TENANTS, "User password too short");
      throw new Error("User password must be at least 6 characters long");
    }

    Logger.d(LogTags.TENANTS, `Making API call to /v1/tenants/users [${requestId}]`);
    
    const res = await apiClient.post<TenantUser>('/v1/tenants/users', payload);
    
    Logger.i(LogTags.TENANTS, `Tenant user created successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.TENANTS, `Created user: ${Logger.maskEmail(res.data.email)}, Role: ${res.data.role}, ID: ${res.data.id}`);

    // Validate response structure
    if (!res.data.id || !res.data.email) {
      Logger.e(LogTags.TENANTS, `Invalid response: missing required user data [${requestId}]`);
      throw new Error("Invalid response from server: incomplete user data");
    }

    Logger.i(LogTags.TENANTS, `New tenant user validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.TENANTS, `Create user failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
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
