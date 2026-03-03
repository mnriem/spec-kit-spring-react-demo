import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Cell, ResponsiveContainer } from 'recharts';
import type { ToolUsageEntry } from '../../types/api';

const COLORS = ['#1677ff', '#52c41a', '#fa8c16', '#eb2f96', '#722ed1', '#13c2c2'];

interface ToolUsageChartProps {
    data: ToolUsageEntry[];
}

const ToolUsageChart: React.FC<ToolUsageChartProps> = ({ data }) => (
    <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" />
            <YAxis type="category" dataKey="toolName" width={120} />
            <Tooltip />
            <Bar dataKey="usageCount" name="Usage Count">
                {data.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
            </Bar>
        </BarChart>
    </ResponsiveContainer>
);

export default ToolUsageChart;
