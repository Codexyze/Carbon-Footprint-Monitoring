import {apiClient} from "@/lib/axios/apiClient";
import {categorizeAxiosError} from "@/lib/errors";
import type {IngestRequest, IngestResponse} from "@/types/ingest/ingesttypes";
import {Logger, LogTags} from "@/lib/logger";

/** Upload a CSV file to the upload-csv endpoint. Returns the same ingest response shape. */
export async function uploadCsv(file: File | Blob): Promise<IngestResponse> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.INGEST, `Uploading CSV file [${requestId}]`);
    
    // Validate file input
    if (!file) {
      Logger.w(LogTags.INGEST, "No file provided for CSV upload");
      throw new Error("File is required for CSV upload");
    }

    if (file.size === 0) {
      Logger.w(LogTags.INGEST, "Empty file provided for CSV upload");
      throw new Error("File cannot be empty");
    }

    // Check file size (warn if large, error if too large)
    const fileSizeMB = file.size / (1024 * 1024);
    if (fileSizeMB > 50) {
      Logger.e(LogTags.INGEST, `File too large for CSV upload: ${fileSizeMB.toFixed(2)} MB`);
      throw new Error("File size cannot exceed 50MB");
    } else if (fileSizeMB > 10) {
      Logger.w(LogTags.INGEST, `Large file for CSV upload: ${fileSizeMB.toFixed(2)} MB`);
    }

    // Log file details
    if (file instanceof File) {
      Logger.d(LogTags.INGEST, `File details - name: ${file.name}, size: ${fileSizeMB.toFixed(2)} MB, type: ${file.type || 'unknown'}`);
      
      // Validate file type
      if (file.type && !file.type.includes('csv') && !file.type.includes('text')) {
        Logger.w(LogTags.INGEST, `Unexpected file type: ${file.type}`);
      }
      
      // Validate file extension
      if (file.name && !file.name.toLowerCase().endsWith('.csv')) {
        Logger.w(LogTags.INGEST, `File does not have .csv extension: ${file.name}`);
      }
    } else {
      Logger.d(LogTags.INGEST, `Blob details - size: ${fileSizeMB.toFixed(2)} MB`);
    }

    Logger.d(LogTags.INGEST, `Creating FormData for file upload [${requestId}]`);
    const fd = new FormData();
    fd.append("file", file);
    
    Logger.d(LogTags.INGEST, `Making API call to /v1/ingest/upload-csv [${requestId}]`);
    
    const res = await apiClient.post<IngestResponse>("/v1/ingest/upload-csv", fd);
    
    Logger.i(LogTags.INGEST, `CSV uploaded successfully [${requestId}] - Status: ${res.status}`);
    Logger.d(LogTags.INGEST, `Response: created_events: ${res.data.created_events}, skipped_duplicates: ${res.data.skipped_duplicates}, created_emissions: ${res.data.created_emissions}`);

    // Validate response structure
    if (typeof res.data !== 'object' || res.data === null) {
      Logger.e(LogTags.INGEST, `Invalid response: data is not an object [${requestId}]`);
      throw new Error("Invalid response from server: response should be an object");
    }

    if (typeof res.data.created_events !== 'number') {
      Logger.e(LogTags.INGEST, `Invalid response: missing or invalid created_events field [${requestId}]`);
      throw new Error("Invalid response from server: missing created_events field");
    }

    if (res.data.created_events === 0 && res.data.skipped_duplicates === 0) {
      Logger.w(LogTags.INGEST, `No events created or skipped from CSV - possible processing issue [${requestId}]`);
    } else {
      Logger.i(LogTags.INGEST, `CSV upload processed ${res.data.created_events} events, skipped ${res.data.skipped_duplicates} duplicates [${requestId}]`);
    }

    Logger.i(LogTags.INGEST, `CSV upload validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.INGEST, `Upload CSV failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.INGEST, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.INGEST, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });