import apiClient from './apiClient';
import type { SuccessEnvelope, IterationMetrics } from '../types/api';

export async function fetchComparison(iterationIds: string[]): Promise<IterationMetrics[]> {
    const params = new URLSearchParams();
    iterationIds.forEach((id) => params.append('iterationIds', id));
    const res = await apiClient.get<SuccessEnvelope<IterationMetrics[]>>(
        `/comparison?${params.toString()}`
    );
    return res.data.data;
}
