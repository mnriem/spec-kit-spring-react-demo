import React from 'react';
import { Drawer, Descriptions, Typography, Table, Spin, Tag } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { getInteraction } from '../../services/interactionsService';
import type { ToolCallResponse } from '../../types/api';

interface InteractionDetailDrawerProps {
    interactionId: string | null;
    onClose: () => void;
}

const toolCallColumns = [
    { title: 'Tool', dataIndex: 'toolName', key: 'toolName' },
    {
        title: 'Arguments',
        dataIndex: 'inputArguments',
        key: 'inputArguments',
        render: (v: unknown) => <pre style={{ fontSize: 12, margin: 0 }}>{JSON.stringify(v, null, 2)}</pre>,
    },
    {
        title: 'Output',
        dataIndex: 'output',
        key: 'output',
        render: (v: unknown) => <pre style={{ fontSize: 12, margin: 0 }}>{JSON.stringify(v, null, 2)}</pre>,
    },
];

const InteractionDetailDrawer: React.FC<InteractionDetailDrawerProps> = ({ interactionId, onClose }) => {
    const { data, isLoading } = useQuery({
        queryKey: ['interaction', interactionId],
        queryFn: () => getInteraction(interactionId!),
        enabled: !!interactionId,
    });

    return (
        <Drawer
            title="Interaction Detail"
            width={720}
            open={!!interactionId}
            onClose={onClose}
        >
            {isLoading && <Spin />}
            {data && (
                <>
                    <Descriptions bordered column={2} size="small" style={{ marginBottom: 16 }}>
                        <Descriptions.Item label="Model"><Tag>{data.model}</Tag></Descriptions.Item>
                        <Descriptions.Item label="Latency">{data.latencyMs} ms</Descriptions.Item>
                        <Descriptions.Item label="Tokens In">{data.tokensIn}</Descriptions.Item>
                        <Descriptions.Item label="Tokens Out">{data.tokensOut}</Descriptions.Item>
                        <Descriptions.Item label="Total Tokens">{data.totalTokens}</Descriptions.Item>
                        <Descriptions.Item label="Est. Cost">{data.estimatedCost ? `$${Number(data.estimatedCost).toFixed(4)}` : '-'}</Descriptions.Item>
                        <Descriptions.Item label="Started">{new Date(data.startedAt).toLocaleString()}</Descriptions.Item>
                        <Descriptions.Item label="Ended">{data.endedAt ? new Date(data.endedAt).toLocaleString() : '-'}</Descriptions.Item>
                        <Descriptions.Item label="Iteration" span={2}>{data.iterationName}</Descriptions.Item>
                        <Descriptions.Item label="Experiment" span={2}>{data.experimentName}</Descriptions.Item>
                        <Descriptions.Item label="Project" span={2}>{data.projectName}</Descriptions.Item>
                    </Descriptions>

                    <Typography.Title level={5}>Prompt</Typography.Title>
                    <pre
                        style={{
                            background: '#f5f5f5',
                            padding: 12,
                            borderRadius: 4,
                            overflow: 'auto',
                            maxHeight: 200,
                            fontSize: 13,
                            marginBottom: 16,
                        }}
                        aria-label="Interaction prompt"
                    >
                        {data.prompt}
                    </pre>

                    {data.responseMetadata && (
                        <>
                            <Typography.Title level={5}>Response Metadata</Typography.Title>
                            <pre style={{ background: '#f5f5f5', padding: 12, borderRadius: 4, overflow: 'auto', maxHeight: 150, fontSize: 12, marginBottom: 16 }}>
                                {JSON.stringify(data.responseMetadata, null, 2)}
                            </pre>
                        </>
                    )}

                    {data.toolCalls && data.toolCalls.length > 0 && (
                        <>
                            <Typography.Title level={5}>Tool Calls</Typography.Title>
                            <Table<ToolCallResponse>
                                dataSource={data.toolCalls}
                                columns={toolCallColumns}
                                rowKey="id"
                                size="small"
                                pagination={false}
                            />
                        </>
                    )}
                </>
            )}
        </Drawer>
    );
};

export default InteractionDetailDrawer;
