import { apiClient, setAuthToken } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import type { LoginRequest, TokenResponse } from "@/lib/auth/api";
import { Logger, LogTags } from "@/lib/logger";

export function login(token:string){
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try{
        Logger.i(LogTags.LOGIN, `Setting auth token in localStorage [${requestId}]`);
        Logger.d(LogTags.LOGIN, `Token length: ${token.length}, starts with: ${token.slice(0, 10)}...`);
        
        localStorage.setItem("token",token);
        setAuthToken(token);
        
        Logger.i(LogTags.LOGIN, `Auth token set successfully [${requestId}]`);
        
    }catch(err){
        Logger.e(LogTags.LOGIN, `Error setting token in localStorage [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
        
        if (err instanceof Error) {
            Logger.e(LogTags.LOGIN, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
        }
        
        console.error("Error in setting token in local storage", err);
    }
}

export async function loginApi(credentials: { email: string; password: string }): Promise<TokenResponse> {
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
        Logger.i(LogTags.LOGIN, `Starting login API request [${requestId}]`);
        Logger.d(LogTags.LOGIN, `Login request for user: ${Logger.maskEmail(credentials.email)}`);

        // Validate credentials before making request
        if (!credentials.email || !credentials.password) {
            Logger.w(LogTags.LOGIN, "Missing email or password in login credentials");
            throw new Error("Email and password are required");
        }

        if (!credentials.email.includes('@')) {
            Logger.w(LogTags.LOGIN, "Invalid email format provided");
            throw new Error("Valid email address is required");
        }

        // Make API call to login endpoint
        Logger.d(LogTags.LOGIN, `Making login API call to /v1/auth/login [${requestId}]`);
        
        const res = await apiClient.post<TokenResponse>("/v1/auth/login", credentials);
        
        Logger.i(LogTags.LOGIN, `Login API call successful [${requestId}] - Status: ${res.status}`);
        Logger.d(LogTags.LOGIN, `Response: access_token: ${res.data.access_token ? 'present' : 'missing'}, token_type: ${res.data.token_type}`);

        // Validate response structure
        if (!res.data.access_token) {
            Logger.e(LogTags.LOGIN, `Invalid response: missing access_token [${requestId}]`);
            throw new Error("Invalid response from server: missing access_token");
        }

        Logger.i(LogTags.LOGIN, `Login API completed successfully for user: ${Logger.maskEmail(credentials.email)} [${requestId}]`);
        return res.data;
        
    } catch (err) {
        Logger.e(LogTags.LOGIN, `Login API failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
        
        if (err instanceof Error) {
            Logger.e(LogTags.LOGIN, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
        }
        
        const categorizedError = categorizeAxiosError(err);
        Logger.e(LogTags.LOGIN, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
        throw categorizedError;
    }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
