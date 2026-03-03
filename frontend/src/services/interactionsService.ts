import apiClient from './apiClient';
import type {
    SuccessEnvelope,
    InteractionSummaryResponse,
    InteractionDetailResponse,
    PagedResponse,
} from '../types/api';

export interface InteractionFilterState {
    model?: string;
    experimentId?: string;
    iterationId?: string;
    projectId?: string;
    from?: string;
    to?: string;
    minLatencyMs?: number;
    maxLatencyMs?: number;
    page?: number;
    size?: number;
    sort?: string;
}

export async function listInteractions(
    params: InteractionFilterState
): Promise<PagedResponse<InteractionSummaryResponse>> {
    const res = await apiClient.get<SuccessEnvelope<PagedResponse<InteractionSummaryResponse>>>(
        '/interactions',
        { params }
    );
    return res.data.data;
}

export async function getInteraction(id: string): Promise<InteractionDetailResponse> {
    const res = await apiClient.get<SuccessEnvelope<InteractionDetailResponse>>(
        `/interactions/${id}`
    );
    return res.data.data;
}
