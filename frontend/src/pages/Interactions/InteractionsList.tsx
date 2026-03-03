import React, { useState } from 'react';
import { Col, DatePicker, Input, Row, Select, Table, Typography } from 'antd';
import type { TablePaginationConfig } from 'antd/es/table';
import type { InteractionSummaryResponse } from '../../types/api';
import { useInteractions } from '../../hooks/useInteractions';
import InteractionDetailDrawer from './InteractionDetailDrawer';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const MODELS = ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'claude-3-5-sonnet-20241022', 'claude-3-haiku-20240307', 'gemini-1.5-pro', 'gemini-1.5-flash'];

const InteractionsList: React.FC = () => {
    const [model, setModel] = useState<string | undefined>();
    const [dateRange, setDateRange] = useState<[string, string] | null>(null);
    const [minLatency, setMinLatency] = useState<number | undefined>();
    const [maxLatency, setMaxLatency] = useState<number | undefined>();
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(20);
    const [selectedId, setSelectedId] = useState<string | null>(null);

    const filters = {
        model,
        from: dateRange?.[0],
        to: dateRange?.[1],
        minLatencyMs: minLatency,
        maxLatencyMs: maxLatency,
        page,
        size: pageSize,
    };

    const { data, isLoading } = useInteractions(filters);

    const columns = [
        {
            title: 'Started',
            dataIndex: 'startedAt',
            key: 'startedAt',
            sorter: true,
            render: (v: string) => new Date(v).toLocaleString(),
        },
        { title: 'Model', dataIndex: 'model', key: 'model' },
        { title: 'Experiment', dataIndex: 'experimentName', key: 'experimentName' },
        { title: 'Iteration', dataIndex: 'iterationName', key: 'iterationName' },
        { title: 'Latency (ms)', dataIndex: 'latencyMs', key: 'latencyMs', sorter: true },
        { title: 'Total Tokens', dataIndex: 'totalTokens', key: 'totalTokens', sorter: true },
        { title: 'Tool Calls', dataIndex: 'toolCallCount', key: 'toolCallCount' },
    ];

    const handleTableChange = (pagination: TablePaginationConfig) => {
        setPage((pagination.current ?? 1) - 1);
        setPageSize(pagination.pageSize ?? 20);
    };

    return (
        <div>
            <Title level={3}>Interactions</Title>

            <Row gutter={[12, 12]} style={{ marginBottom: 16 }}>
                <Col xs={24} sm={6}>
                    <Select
                        style={{ width: '100%' }}
                        placeholder="Filter by Model"
                        allowClear
                        options={MODELS.map((m) => ({ label: m, value: m }))}
                        onChange={setModel}
                        aria-label="Filter by model"
                    />
                </Col>
                <Col xs={24} sm={10}>
                    <RangePicker
                        style={{ width: '100%' }}
                        onChange={(_, strs) => setDateRange(strs[0] && strs[1] ? [strs[0], strs[1]] : null)}
                        aria-label="Filter by date range"
                    />
                </Col>
                <Col xs={12} sm={4}>
                    <Input
                        placeholder="Min latency"
                        type="number"
                        onChange={(e) => setMinLatency(e.target.value ? Number(e.target.value) : undefined)}
                        aria-label="Minimum latency"
                    />
                </Col>
                <Col xs={12} sm={4}>
                    <Input
                        placeholder="Max latency"
                        type="number"
                        onChange={(e) => setMaxLatency(e.target.value ? Number(e.target.value) : undefined)}
                        aria-label="Maximum latency"
                    />
                </Col>
            </Row>

            <Table<InteractionSummaryResponse>
                dataSource={data?.content ?? []}
                columns={columns}
                rowKey="id"
                loading={isLoading}
                pagination={{
                    current: page + 1,
                    pageSize,
                    total: data?.totalElements ?? 0,
                    showSizeChanger: true,
                }}
                onChange={handleTableChange}
                onRow={(record) => ({
                    onClick: () => setSelectedId(record.id),
                    style: { cursor: 'pointer' },
                })}
            />

            <InteractionDetailDrawer
                interactionId={selectedId}
                onClose={() => setSelectedId(null)}
            />
        </div>
    );
};

export default InteractionsList;
