import React, { useState } from 'react';
import { Alert, Card, Col, Row, Select, Spin, Table, Typography } from 'antd';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
    RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { useComparison } from '../../hooks/useComparison';
import type { ExperimentResponse, IterationResponse, ProjectResponse, SuccessEnvelope } from '../../types/api';
import apiClient from '../../services/apiClient';

const { Title } = Typography;

const Comparison: React.FC = () => {
    const [selectedProject, setSelectedProject] = useState<string | undefined>();
    const [selectedExperiment, setSelectedExperiment] = useState<string | undefined>();
    const [selectedIterationIds, setSelectedIterationIds] = useState<string[]>([]);

    const projects = useQuery<ProjectResponse[]>({
        queryKey: ['projects'],
        queryFn: (): Promise<ProjectResponse[]> =>
            apiClient.get<SuccessEnvelope<ProjectResponse[]>>('/projects').then(r => r.data.data),
    });

    const experiments = useQuery<ExperimentResponse[]>({
        queryKey: ['experiments', selectedProject],
        queryFn: (): Promise<ExperimentResponse[]> =>
            apiClient.get<SuccessEnvelope<ExperimentResponse[]>>(`/projects/${selectedProject}/experiments`).then(r => r.data.data),
        enabled: !!selectedProject,
    });

    const iterations = useQuery<IterationResponse[]>({
        queryKey: ['iterations', selectedExperiment],
        queryFn: (): Promise<IterationResponse[]> =>
            apiClient.get<SuccessEnvelope<IterationResponse[]>>(`/experiments/${selectedExperiment}/iterations`).then(r => r.data.data),
        enabled: !!selectedExperiment,
    });

    const { data: metrics, isLoading } = useComparison(selectedIterationIds);

    const columns = [
        { title: 'Iteration', dataIndex: 'iterationName', key: 'iterationName' },
        { title: 'Avg Latency (ms)', dataIndex: 'avgLatencyMs', key: 'avgLatencyMs', render: (v: number | null) => v?.toFixed(0) ?? '-' },
        { title: 'Avg Tokens In', dataIndex: 'avgTokensIn', key: 'avgTokensIn', render: (v: number | null) => v?.toFixed(0) ?? '-' },
        { title: 'Avg Tokens Out', dataIndex: 'avgTokensOut', key: 'avgTokensOut', render: (v: number | null) => v?.toFixed(0) ?? '-' },
        { title: 'Tool Call Rate', dataIndex: 'toolCallRate', key: 'toolCallRate', render: (v: number | null) => v?.toFixed(2) ?? '-' },
        { title: 'Est. Cost', dataIndex: 'avgEstimatedCost', key: 'avgEstimatedCost', render: (v: string | null) => v ? `$${Number(v).toFixed(4)}` : '-' },
        { title: 'Interactions', dataIndex: 'interactionCount', key: 'interactionCount' },
    ];

    const barData = metrics?.map((m) => ({
        name: m.iterationName,
        avgLatencyMs: m.avgLatencyMs ?? 0,
        avgTokensIn: m.avgTokensIn ?? 0,
        avgTokensOut: m.avgTokensOut ?? 0,
    }));

    const radarData = metrics && metrics.length >= 2 ? [
        { metric: 'Latency', ...Object.fromEntries(metrics.map(m => [m.iterationName, m.avgLatencyMs ?? 0])) },
        { metric: 'Tokens In', ...Object.fromEntries(metrics.map(m => [m.iterationName, m.avgTokensIn ?? 0])) },
        { metric: 'Tokens Out', ...Object.fromEntries(metrics.map(m => [m.iterationName, m.avgTokensOut ?? 0])) },
        { metric: 'Tool Rate', ...Object.fromEntries(metrics.map(m => [m.iterationName, (m.toolCallRate ?? 0) * 100])) },
    ] : [];

    const RADAR_COLORS = ['#1677ff', '#52c41a', '#fa8c16', '#eb2f96'];

    return (
        <div>
            <Title level={3}>Iteration Comparison</Title>

            <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col xs={24} sm={8}>
                    <Select
                        style={{ width: '100%' }}
                        placeholder="Select Project"
                        loading={projects.isLoading}
                        onChange={(v) => { setSelectedProject(v); setSelectedExperiment(undefined); setSelectedIterationIds([]); }}
                        options={projects.data?.map((p) => ({ label: p.name, value: p.id })) ?? []}
                        aria-label="Select Project"
                    />
                </Col>
                <Col xs={24} sm={8}>
                    <Select
                        style={{ width: '100%' }}
                        placeholder="Select Experiment"
                        loading={experiments.isLoading}
                        disabled={!selectedProject}
                        onChange={(v) => { setSelectedExperiment(v); setSelectedIterationIds([]); }}
                        options={experiments.data?.map((e) => ({ label: e.name, value: e.id })) ?? []}
                        aria-label="Select Experiment"
                    />
                </Col>
                <Col xs={24} sm={8}>
                    <Select
                        mode="multiple"
                        style={{ width: '100%' }}
                        placeholder="Select Iterations (2+)"
                        loading={iterations.isLoading}
                        disabled={!selectedExperiment}
                        onChange={setSelectedIterationIds}
                        options={iterations.data?.map((i) => ({ label: i.name, value: i.id })) ?? []}
                        aria-label="Select Iterations"
                    />
                </Col>
            </Row>

            {selectedIterationIds.length === 1 && (
                <Alert
                    type="warning"
                    message="Select at least 2 iterations to compare"
                    style={{ marginBottom: 16 }}
                    showIcon
                />
            )}

            {isLoading && <Spin />}

            {metrics && metrics.length >= 2 && (
                <>
                    <Table
                        dataSource={metrics}
                        columns={columns}
                        rowKey="iterationId"
                        pagination={false}
                        style={{ marginBottom: 24 }}
                    />

                    <Row gutter={[16, 16]}>
                        <Col xs={24} lg={12}>
                            <Card title="Latency & Tokens Comparison">
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={barData}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="name" />
                                        <YAxis />
                                        <Tooltip />
                                        <Legend />
                                        <Bar dataKey="avgLatencyMs" name="Avg Latency (ms)" fill="#1677ff" />
                                        <Bar dataKey="avgTokensIn" name="Avg Tokens In" fill="#52c41a" />
                                        <Bar dataKey="avgTokensOut" name="Avg Tokens Out" fill="#fa8c16" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </Card>
                        </Col>
                        <Col xs={24} lg={12}>
                            <Card title="Multi-Metric Radar">
                                <ResponsiveContainer width="100%" height={300}>
                                    <RadarChart data={radarData}>
                                        <PolarGrid />
                                        <PolarAngleAxis dataKey="metric" />
                                        <PolarRadiusAxis />
                                        {metrics.map((m, idx) => (
                                            <Radar
                                                key={m.iterationId}
                                                name={m.iterationName}
                                                dataKey={m.iterationName}
                                                stroke={RADAR_COLORS[idx % RADAR_COLORS.length]}
                                                fill={RADAR_COLORS[idx % RADAR_COLORS.length]}
                                                fillOpacity={0.3}
                                            />
                                        ))}
                                        <Legend />
                                        <Tooltip />
                                    </RadarChart>
                                </ResponsiveContainer>
                            </Card>
                        </Col>
                    </Row>
                </>
            )}
        </div>
    );
};

export default Comparison;
