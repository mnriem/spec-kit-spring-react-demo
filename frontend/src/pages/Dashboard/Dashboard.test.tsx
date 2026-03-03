import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Dashboard from './Dashboard';

vi.mock('../../context/ProjectContext', () => ({
    useProjectContext: () => ({ selectedProjectId: null, setSelectedProjectId: vi.fn() }),
}));

vi.mock('../../services/dashboardService', () => ({
    fetchSummary: vi.fn().mockResolvedValue({
        totalInteractions: 5,
        avgLatencyMs: 320,
        totalTokens: 4500,
        totalCost: '0.09',
    }),
    fetchTimeline: vi.fn().mockResolvedValue([
        { bucket: '2026-03-01', count: 3, avgLatencyMs: 200, totalTokens: 900 },
    ]),
    fetchTokensByModel: vi.fn().mockResolvedValue([
        { model: 'gpt-4o', tokensIn: 1000, tokensOut: 500 },
    ]),
    fetchLatencyDistribution: vi.fn().mockResolvedValue([
        { bucket: '0-500ms', count: 4 },
    ]),
    fetchToolUsage: vi.fn().mockResolvedValue([
        { toolName: 'search', usageCount: 7, avgPerInteraction: null },
    ]),
}));

function renderDashboard() {
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    return render(
        <QueryClientProvider client={qc}>
            <Dashboard />
        </QueryClientProvider>
    );
}

describe('Dashboard', () => {
    it('renders heading', () => {
        renderDashboard();
        expect(screen.getByRole('heading', { name: /dashboard/i })).toBeTruthy();
    });

    it('shows metric cards', async () => {
        renderDashboard();
        expect(await screen.findByText('Total Interactions')).toBeTruthy();
        expect(screen.getByText('Avg Latency')).toBeTruthy();
        expect(screen.getByText('Total Tokens')).toBeTruthy();
        expect(screen.getByText('Estimated Cost')).toBeTruthy();
    });

    it('shows chart sections after data loads', async () => {
        renderDashboard();
        expect(await screen.findByText('Interactions Over Time')).toBeTruthy();
        expect(screen.getByText('Tokens by Model')).toBeTruthy();
        expect(screen.getByText('Latency Distribution')).toBeTruthy();
        expect(screen.getByText('Tool Usage')).toBeTruthy();
    });
});
