import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { TokensByModelEntry } from '../../types/api';

interface TokensByModelChartProps {
    data: TokensByModelEntry[];
}

const TokensByModelChart: React.FC<TokensByModelChartProps> = ({ data }) => (
    <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="model" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="totalTokensIn" name="Tokens In" fill="#1677ff" />
            <Bar dataKey="totalTokensOut" name="Tokens Out" fill="#52c41a" />
        </BarChart>
    </ResponsiveContainer>
);

export default TokensByModelChart;
