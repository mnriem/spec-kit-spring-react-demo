import React from 'react';
import { Empty, Button, notification, Spin } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '../services/apiClient';
import type { SuccessEnvelope, SampleDataResponse } from '../types/api';

interface EmptyStateProps {
    description?: string;
    showLoadSampleData?: boolean;
}

const EmptyState: React.FC<EmptyStateProps> = ({
    description = 'No data available',
    showLoadSampleData = false,
}) => {
    const queryClient = useQueryClient();

    const mutation = useMutation({
        mutationFn: async () => {
            const res = await apiClient.post<SuccessEnvelope<SampleDataResponse>>('/admin/sample-data');
            return res.data.data;
        },
        onSuccess: (data) => {
            notification.success({
                message: 'Sample Data Loaded',
                description: `Loaded ${data.interactionCount} interactions across ${data.projectCount} projects.`,
            });
            queryClient.invalidateQueries();
        },
        onError: () => {
            notification.error({ message: 'Failed to load sample data' });
        },
    });

    return (
        <Empty description={description} style={{ margin: '48px auto' }}>
            {showLoadSampleData && (
                <Button
                    type="primary"
                    onClick={() => mutation.mutate()}
                    disabled={mutation.isPending}
                    icon={mutation.isPending ? <Spin size="small" /> : null}
                >
                    Load Sample Data
                </Button>
            )}
        </Empty>
    );
};

export default EmptyState;
