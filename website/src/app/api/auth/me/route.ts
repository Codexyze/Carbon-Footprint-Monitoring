import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { MeResponse, TokenResponse } from "@/lib/auth/api";
import { Logger, LogTags } from "@/lib/logger";

export async function me(): Promise<MeResponse> {
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
        Logger.i(LogTags.AUTH, `Getting current user [${requestId}]`);
        Logger.d(LogTags.AUTH, `Making API call to /v1/auth/me [${requestId}]`);
        
        // Make API call
        const res = await apiClient.get<MeResponse>("/v1/auth/me");
        
        Logger.i(LogTags.AUTH, `Current user retrieved successfully [${requestId}] - Status: ${res.status}`);
        Logger.d(LogTags.AUTH, `User: ${res.data.email}, Role: ${res.data.role}, Org: ${res.data.org.name}`);

        // Validate response structure
        if (!res.data.email || !res.data.role || !res.data.org) {
            Logger.e(LogTags.AUTH, `Invalid response: missing required user data [${requestId}]`);
            throw new Error("Invalid response from server: incomplete user data");
        }

        Logger.i(LogTags.AUTH, `Current user data validated successfully [${requestId}]`);
        return res.data;
        
    } catch (err) {
        Logger.e(LogTags.AUTH, `Get current user failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
        
        if (err instanceof Error) {
            Logger.e(LogTags.AUTH, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
        }
        
        const categorizedError = categorizeAxiosError(err);
        Logger.e(LogTags.AUTH, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
        throw categorizedError;
    }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
