import {apiClient} from "@/lib/axios/apiClient";
import {categorizeAxiosError} from "@/lib/errors";
import type {ReportPeriod} from "@/types/reports/reportstypes";
import {Logger, LogTags} from "@/lib/logger";

/** Get emissions report as CSV for the specified period */
export async function getReportPeriod(from: string, to: string): Promise<string> {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  try {
    Logger.i(LogTags.REPORTS, `Getting period report [${requestId}]`);
    Logger.d(LogTags.REPORTS, `Report params - from: ${from}, to: ${to}`);

    // Validate date parameters
    if (!from || !to) {
      Logger.w(LogTags.REPORTS, "Missing date parameters for period report");
      throw new Error("Both 'from' and 'to' dates are required");
    }

    if (!isValidDateString(from) || !isValidDateString(to)) {
      Logger.w(LogTags.REPORTS, "Invalid date format for period report");
      throw new Error("Dates must be in valid format (YYYY-MM-DD or ISO-8601)");
    }

    const fromDate = new Date(from);
    const toDate = new Date(to);
    
    if (fromDate >= toDate) {
      Logger.w(LogTags.REPORTS, "Invalid date range for period report: from date is not before to date");
      throw new Error("'from' date must be before 'to' date");
    }

    // Check if date range is reasonable (not too large)
    const daysDiff = Math.ceil((toDate.getTime() - fromDate.getTime()) / (1000 * 60 * 60 * 24));
    if (daysDiff > 365) {
      Logger.w(LogTags.REPORTS, `Large date range requested: ${daysDiff} days`);
      Logger.i(LogTags.REPORTS, `Proceeding with large date range report [${requestId}]`);
    }

    const params = new URLSearchParams();
    params.append('from', from);
    params.append('to', to);
    
    const url = `/v1/reports/period?${params.toString()}`;
    Logger.d(LogTags.REPORTS, `Making API call to ${url} [${requestId}]`);
    Logger.d(LogTags.REPORTS, `Request headers: Accept: text/csv [${requestId}]`);
    
    // API returns CSV as text/csv
    const res = await apiClient.get<string>(url, {
      headers: {
        'Accept': 'text/csv'
      }
    });
    
    Logger.i(LogTags.REPORTS, `Period report retrieved successfully [${requestId}] - Status: ${res.status}`);
    
    // Validate response
    if (typeof res.data !== 'string') {
      Logger.e(LogTags.REPORTS, `Invalid response: report data is not a string [${requestId}]`);
      throw new Error("Invalid response from server: report should be CSV text");
    }

    const csvLines = res.data.split('\n').filter(line => line.trim().length > 0);
    Logger.d(LogTags.REPORTS, `CSV report contains ${csvLines.length} lines [${requestId}]`);
    
    if (csvLines.length === 0) {
      Logger.w(LogTags.REPORTS, `Empty CSV report received [${requestId}]`);
    } else if (csvLines.length === 1) {
      Logger.w(LogTags.REPORTS, `CSV report contains only headers (no data rows) [${requestId}]`);
    } else {
      Logger.d(LogTags.REPORTS, `CSV report contains ${csvLines.length - 1} data rows [${requestId}]`);
    }

    // Log sample of CSV content (first line only for security)
    if (csvLines.length > 0) {
      Logger.d(LogTags.REPORTS, `CSV headers: ${csvLines[0].substring(0, 100)}${csvLines[0].length > 100 ? '...' : ''} [${requestId}]`);
    }

    Logger.i(LogTags.REPORTS, `Period report validated successfully [${requestId}]`);
    return res.data;
    
  } catch (err) {
    Logger.e(LogTags.REPORTS, `Get period report failed [${requestId}]: ${err instanceof Error ? err.message : String(err)}`);
    
    if (err instanceof Error) {
      Logger.e(LogTags.REPORTS, `Error stack trace [${requestId}]: ${Logger.getStackTraceString(err)}`);
    }
    
    const categorizedError = categorizeAxiosError(err);
    Logger.e(LogTags.REPORTS, `Categorized error [${requestId}]: ${categorizedError.constructor.name} - ${categorizedError.message}`);
    throw categorizedError;
  }
}

// Helper function to validate date strings
function isValidDateString(dateString: string): boolean {
  // Accept both YYYY-MM-DD and ISO-8601 formats
  const dateOnlyRegex = /^\d{4}-\d{2}-\d{2}$/;
  const isoDateRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z?$/;
  
  if (!dateOnlyRegex.test(dateString) && !isoDateRegex.test(dateString)) {
    return false;
  }
  
  const date = new Date(dateString);
  return !isNaN(date.getTime());
}

// Minimal route handlers to satisfy Next.js route type validation for helper modules.
export const GET = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
export const POST = async () => new Response(JSON.stringify({ error: 'not-implemented' }), { status: 501 });
