import React, { useState } from 'react';
import { Row, Col, Card, DatePicker, Typography, Spin } from 'antd';
import type { RangePickerProps } from 'antd/es/date-picker';
import type { Dayjs } from 'dayjs';
import { useDashboard } from '../../hooks/useDashboard';
import { useProjectContext } from '../../context/ProjectContext';
import MetricCard from '../../components/MetricCard';
import EmptyState from '../../components/EmptyState';
import TimelineChart from '../../components/charts/TimelineChart';
import TokensByModelChart from '../../components/charts/TokensByModelChart';
import LatencyDistributionChart from '../../components/charts/LatencyDistributionChart';
import ToolUsageChart from '../../components/charts/ToolUsageChart';

const { RangePicker } = DatePicker;
const { Title } = Typography;

const Dashboard: React.FC = () => {
    const { selectedProjectId } = useProjectContext();
    const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null);

    const from = dateRange?.[0]?.toISOString();
    const to = dateRange?.[1]?.toISOString();
    const filters = { projectId: selectedProjectId ?? undefined, from, to };

    const { summary, timeline, tokensByModel, latencyDistribution, toolUsage } = useDashboard(filters);

    const handleRangeChange: RangePickerProps['onChange'] = (dates) => {
        setDateRange(dates as [Dayjs | null, Dayjs | null] | null);
    };

    const noData = summary.data?.totalInteractions === 0;

    return (
        <div>
            <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
                <Col>
                    <Title level={3} style={{ margin: 0 }} aria-label="Dashboard">Dashboard</Title>
                </Col>
                <Col>
                    <RangePicker
                        onChange={handleRangeChange}
                        aria-label="Date range filter"
                    />
                </Col>
            </Row>

            <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col xs={24} sm={12} lg={6}>
                    <MetricCard
                        title="Total Interactions"
                        value={summary.data?.totalInteractions}
                        loading={summary.isLoading}
                    />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <MetricCard
                        title="Avg Latency"
                        value={summary.data?.avgLatencyMs ?? undefined}
                        suffix="ms"
                        precision={0}
                        loading={summary.isLoading}
                    />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <MetricCard
                        title="Total Tokens"
                        value={summary.data?.totalTokens ?? undefined}
                        loading={summary.isLoading}
                    />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <MetricCard
                        title="Estimated Cost"
                        value={summary.data?.totalEstimatedCost != null ? Number(summary.data.totalEstimatedCost) : undefined}
                        prefix="$"
                        precision={4}
                        loading={summary.isLoading}
                    />
                </Col>
            </Row>

            {noData && !summary.isLoading ? (
                <EmptyState
                    description="No interactions found. Ingest some data or adjust the date range."
                    showLoadSampleData
                />
            ) : (
                <Row gutter={[16, 16]}>
                    <Col xs={24} lg={12}>
                        <Card title="Interactions Over Time">
                            {timeline.isLoading ? <Spin /> : (
                                timeline.data && timeline.data.length > 0
                                    ? <TimelineChart data={timeline.data} />
                                    : <EmptyState />
                            )}
                        </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                        <Card title="Tokens by Model">
                            {tokensByModel.isLoading ? <Spin /> : (
                                tokensByModel.data && tokensByModel.data.length > 0
                                    ? <TokensByModelChart data={tokensByModel.data} />
                                    : <EmptyState />
                            )}
                        </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                        <Card title="Latency Distribution">
                            {latencyDistribution.isLoading ? <Spin /> : (
                                latencyDistribution.data && latencyDistribution.data.length > 0
                                    ? <LatencyDistributionChart data={latencyDistribution.data} />
                                    : <EmptyState />
                            )}
                        </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                        <Card title="Tool Usage">
                            {toolUsage.isLoading ? <Spin /> : (
                                toolUsage.data && toolUsage.data.length > 0
                                    ? <ToolUsageChart data={toolUsage.data} />
                                    : <EmptyState />
                            )}
                        </Card>
                    </Col>
                </Row>
            )}
        </div>
    );
};

export default Dashboard;
