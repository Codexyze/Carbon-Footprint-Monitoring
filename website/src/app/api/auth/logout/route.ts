import { apiClient } from "@/lib/axios/apiClient";
import { categorizeAxiosError } from "@/lib/errors";
import { setAuthToken } from "@/lib/axios/apiClient";
import { Logger, LogTags } from "@/lib/logger";


export function logout(){
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try{
        Logger.i(LogTags.AUTH, `Starting logout process [${requestId}]`);
        Logger.d(LogTags.AUTH, `Removing auth token from localStorage [${requestId}]`);
        
        localStorage.removeItem("token");
        setAuthToken(undefined);
        
        Logger.i(LogTags.AUTH, `Logout completed successfully [${requestId}]`);
        
    }catch(err){
        Logger.e(LogTags.AUTH, `Error during logout [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
        
        if (err instanceof Error) {
            Logger.e(LogTags.AUTH, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
        }
        
        console.error("Error while removing token", err);
        
        // Don't throw error for logout - best effort cleanup
        Logger.w(LogTags.AUTH, `Logout completed with warnings [${requestId}]`);
    }
}

    // Minimal route handlers to satisfy Next.js route type validation for helper modules.
    export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
    export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });