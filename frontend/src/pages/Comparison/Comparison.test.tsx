import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Comparison from './Comparison';

vi.mock('../../services/apiClient', () => ({
    default: {
        get: vi.fn().mockResolvedValue({ data: { data: [] } }),
    },
}));

vi.mock('../../services/comparisonService', () => ({
    fetchComparison: vi.fn().mockResolvedValue([
        {
            iterationId: 'aaaa-bbbb-cccc-dddd-eeee',
            iterationName: 'Iteration A',
            avgLatencyMs: 200,
            avgTokensIn: 100,
            avgTokensOut: 50,
            avgTotalTokens: 150,
            toolCallRate: 2.0,
            avgEstimatedCost: '0.01',
            interactionCount: 5,
        },
        {
            iterationId: 'ffff-0000-1111-2222-3333',
            iterationName: 'Iteration B',
            avgLatencyMs: 400,
            avgTokensIn: 200,
            avgTokensOut: 100,
            avgTotalTokens: 300,
            toolCallRate: 3.5,
            avgEstimatedCost: '0.02',
            interactionCount: 8,
        },
    ]),
}));

function renderComparison() {
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    return render(
        <QueryClientProvider client={qc}>
            <Comparison />
        </QueryClientProvider>
    );
}

describe('Comparison', () => {
    it('renders heading', () => {
        renderComparison();
        expect(screen.getByRole('heading', { name: /iteration comparison/i })).toBeTruthy();
    });

    it('renders project/experiment/iteration selects', () => {
        renderComparison();
        expect(screen.getAllByLabelText(/select project/i).length).toBeGreaterThan(0);
        expect(screen.getAllByLabelText(/select experiment/i).length).toBeGreaterThan(0);
        expect(screen.getAllByLabelText(/select iterations/i).length).toBeGreaterThan(0);
    });
});
