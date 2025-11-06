import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { SignupRequest, TokenResponse } from "@/lib/auth/api";
import { Logger, LogTags } from "@/lib/logger";


export async function signup(data:SignupRequest): Promise<TokenResponse>{
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
        Logger.i(LogTags.SIGNUP, `Starting signup request [${requestId}]`);
        Logger.d(LogTags.SIGNUP, `Signup request for user: ${Logger.maskEmail(data.email)}, org: ${data.org_name}`);

        // Validate request data
        if (!data.email || !data.password || !data.org_name) {
            Logger.w(LogTags.SIGNUP, "Missing required fields in signup request");
            throw new Error("All required fields must be provided");
        }

        if (!data.email.includes('@')) {
            Logger.w(LogTags.SIGNUP, "Invalid email format provided");
            throw new Error("Valid email address is required");
        }

        if (data.password.length < 6) {
            Logger.w(LogTags.SIGNUP, "Password too short");
            throw new Error("Password must be at least 6 characters long");
        }

        // Make API call
        Logger.d(LogTags.SIGNUP, `Making signup API call to /v1/auth/signup [${requestId}]`);
        
        const response = await apiClient.post<TokenResponse>("/v1/auth/signup", data);
        
        Logger.i(LogTags.SIGNUP, `Signup API call successful [${requestId}] - Status: ${response.status}`);
        Logger.d(LogTags.SIGNUP, `Response: access_token: ${response.data.access_token ? 'present' : 'missing'}, token_type: ${response.data.token_type}`);

        // Validate response structure
        if (!response.data.access_token) {
            Logger.e(LogTags.SIGNUP, `Invalid response: missing access_token [${requestId}]`);
            throw new Error("Invalid response from server: missing access_token");
        }

        Logger.i(LogTags.SIGNUP, `Signup completed successfully for user: ${Logger.maskEmail(data.email)} [${requestId}]`);
        return response.data;
        
    } catch (err) {
        Logger.e(LogTags.SIGNUP, `Signup failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
        
        if (err instanceof Error) {
            Logger.e(LogTags.SIGNUP, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
        }
        
        const categorizedError = categorizeAxiosError(err);
        Logger.e(LogTags.SIGNUP, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
        throw categorizedError;
    }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
