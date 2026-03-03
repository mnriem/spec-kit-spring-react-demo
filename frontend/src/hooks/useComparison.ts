import { useQuery } from '@tanstack/react-query';
import { fetchComparison } from '../services/comparisonService';

export function useComparison(iterationIds: string[]) {
    return useQuery({
        queryKey: ['comparison', iterationIds],
        queryFn: () => fetchComparison(iterationIds),
        enabled: iterationIds.length >= 2,
    });
}
