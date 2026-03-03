import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { LatencyBucketEntry } from '../../types/api';

interface LatencyDistributionChartProps {
    data: LatencyBucketEntry[];
}

const LatencyDistributionChart: React.FC<LatencyDistributionChartProps> = ({ data }) => (
    <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="bucketMin" tickFormatter={(v: number) => `${v}ms`} />
            <YAxis />
            <Tooltip />
            <Bar dataKey="count" name="Interactions" fill="#722ed1" />
        </BarChart>
    </ResponsiveContainer>
);

export default LatencyDistributionChart;
