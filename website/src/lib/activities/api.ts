import { apiClient } from '@/lib/axios/apiClient';
import { categorizeAxiosError } from '@/lib/errors';

// Types exported from this module for route handlers to import
export type GetActivitiesParams = {
  page?: number;
  page_size?: number;
  facility_id?: number;
  category?: string;
};

export type CreateActivityRequest = {
  facility_id: number;
  occurred_at: string; // ISO-8601
  category: string;
  unit?: string;
  value_numeric?: number;
  description?: string;
  source_id?: string;
};

export type Activity = {
  id: number;
  facility_id: number;
  occurred_at: string;
  category: string;
  unit?: string;
  value_numeric?: number;
  description?: string;
  source_id?: string;
};

export async function getActivities(params: GetActivitiesParams) {
  try {
    const res = await apiClient.get<Activity[]>('/v1/activities', { params });
    return res.data;
  } catch (err) {
    throw categorizeAxiosError(err);
  }
}

export async function createActivity(payload: CreateActivityRequest) {
  try {
    const res = await apiClient.post<Activity>('/v1/activities', payload);
    return res.data;
  } catch (err) {
    throw categorizeAxiosError(err);
  }
}
