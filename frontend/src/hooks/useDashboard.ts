import { useQuery } from '@tanstack/react-query';
import {
    fetchSummary,
    fetchTimeline,
    fetchTokensByModel,
    fetchLatencyDistribution,
    fetchToolUsage,
    type DashboardFilters,
} from '../services/dashboardService';

export function useDashboard(filters: DashboardFilters) {
    const summary = useQuery({
        queryKey: ['dashboard', 'summary', filters],
        queryFn: () => fetchSummary(filters),
    });

    const timeline = useQuery({
        queryKey: ['dashboard', 'timeline', filters],
        queryFn: () => fetchTimeline(filters),
    });

    const tokensByModel = useQuery({
        queryKey: ['dashboard', 'tokensByModel', filters],
        queryFn: () => fetchTokensByModel(filters),
    });

    const latencyDistribution = useQuery({
        queryKey: ['dashboard', 'latency', filters],
        queryFn: () => fetchLatencyDistribution(filters),
    });

    const toolUsage = useQuery({
        queryKey: ['dashboard', 'toolUsage', filters],
        queryFn: () => fetchToolUsage(filters),
    });

    return { summary, timeline, tokensByModel, latencyDistribution, toolUsage };
}
