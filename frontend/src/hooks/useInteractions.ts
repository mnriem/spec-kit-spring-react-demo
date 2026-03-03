import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { listInteractions, type InteractionFilterState } from '../services/interactionsService';

export function useInteractions(filters: InteractionFilterState) {
    return useQuery({
        queryKey: ['interactions', filters],
        queryFn: () => listInteractions(filters),
        placeholderData: keepPreviousData,
    });
}
