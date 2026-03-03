import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TimelineDataPoint } from '../../types/api';

interface TimelineChartProps {
    data: TimelineDataPoint[];
}

const TimelineChart: React.FC<TimelineChartProps> = ({ data }) => (
    <ResponsiveContainer width="100%" height={300}>
        <AreaChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis
                dataKey="bucket"
                tickFormatter={(v: string) => v.slice(0, 10)}
                tick={{ fontSize: 12 }}
            />
            <YAxis allowDecimals={false} />
            <Tooltip labelFormatter={(v: string) => v.slice(0, 10)} />
            <Area type="monotone" dataKey="interactionCount" name="Interactions" stroke="#1677ff" fill="#bae0ff" />
        </AreaChart>
    </ResponsiveContainer>
);

export default TimelineChart;
