import apiClient from './apiClient';
import type {
    DashboardSummaryResponse,
    TimelineDataPoint,
    TokensByModelEntry,
    LatencyBucketEntry,
    ToolUsageEntry,
    SuccessEnvelope,
} from '../types/api';

export interface DashboardFilters {
    projectId?: string;
    from?: string;
    to?: string;
}

function buildParams(filters: DashboardFilters, extra?: Record<string, string | number>) {
    const params: Record<string, string | number> = {};
    if (filters.projectId) params.projectId = filters.projectId;
    if (filters.from) params.from = filters.from;
    if (filters.to) params.to = filters.to;
    return { ...params, ...extra };
}

export async function fetchSummary(filters: DashboardFilters): Promise<DashboardSummaryResponse> {
    const res = await apiClient.get<SuccessEnvelope<DashboardSummaryResponse>>(
        '/dashboard/summary',
        { params: buildParams(filters) }
    );
    return res.data.data;
}

export async function fetchTimeline(filters: DashboardFilters, granularity = 'day'): Promise<TimelineDataPoint[]> {
    const res = await apiClient.get<SuccessEnvelope<TimelineDataPoint[]>>(
        '/dashboard/timeline',
        { params: buildParams(filters, { granularity }) }
    );
    return res.data.data;
}

export async function fetchTokensByModel(filters: DashboardFilters): Promise<TokensByModelEntry[]> {
    const res = await apiClient.get<SuccessEnvelope<TokensByModelEntry[]>>(
        '/dashboard/tokens-by-model',
        { params: buildParams(filters) }
    );
    return res.data.data;
}

export async function fetchLatencyDistribution(filters: DashboardFilters, buckets = 10): Promise<LatencyBucketEntry[]> {
    const res = await apiClient.get<SuccessEnvelope<LatencyBucketEntry[]>>(
        '/dashboard/latency-distribution',
        { params: buildParams(filters, { buckets }) }
    );
    return res.data.data;
}

export async function fetchToolUsage(filters: DashboardFilters, limit = 20): Promise<ToolUsageEntry[]> {
    const res = await apiClient.get<SuccessEnvelope<ToolUsageEntry[]>>(
        '/dashboard/tool-usage',
        { params: buildParams(filters, { limit }) }
    );
    return res.data.data;
}
