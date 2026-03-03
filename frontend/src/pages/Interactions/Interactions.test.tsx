import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import InteractionsList from './InteractionsList';

vi.mock('../../services/interactionsService', () => ({
    listInteractions: vi.fn().mockResolvedValue({
        content: [
            {
                id: 'id-1',
                iterationId: 'iter-1',
                iterationName: 'Baseline',
                experimentId: 'exp-1',
                experimentName: 'Exp A',
                projectId: 'proj-1',
                projectName: 'Test Project',
                model: 'gpt-4o',
                tokensIn: 500,
                tokensOut: 250,
                totalTokens: 750,
                latencyMs: 300,
                tokensPerSecond: null,
                estimatedCost: '0.01',
                toolCallCount: 2,
                startedAt: '2026-03-01T12:00:00Z',
                endedAt: '2026-03-01T12:00:00.3Z',
                createdAt: '2026-03-01T12:00:01Z',
            },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 20,
        number: 0,
    }),
    getInteraction: vi.fn(),
}));

function renderList() {
    const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    return render(
        <QueryClientProvider client={qc}>
            <InteractionsList />
        </QueryClientProvider>
    );
}

describe('InteractionsList', () => {
    it('renders heading', () => {
        renderList();
        expect(screen.getByRole('heading', { name: /interactions/i })).toBeTruthy();
    });

    it('renders filter toolbar selects', () => {
        renderList();
        expect(screen.getAllByLabelText(/filter by model/i).length).toBeGreaterThan(0);
    });

    it('shows table data after load', async () => {
        renderList();
        expect(await screen.findByText('gpt-4o')).toBeTruthy();
        expect(screen.getByText('Baseline')).toBeTruthy();
    });
});
