import React from 'react';
import { Statistic, Card, Skeleton } from 'antd';
import type { StatisticProps } from 'antd';

interface MetricCardProps {
    title: string;
    value?: number | string | null;
    precision?: number;
    prefix?: string;
    suffix?: string;
    loading?: boolean;
    formatter?: StatisticProps['formatter'];
}

const MetricCard: React.FC<MetricCardProps> = ({
    title,
    value,
    precision,
    prefix,
    suffix,
    loading = false,
    formatter,
}) => {
    return (
        <Card>
            {loading ? (
                <Skeleton active paragraph={false} />
            ) : (
                <Statistic
                    title={title}
                    value={value ?? '-'}
                    precision={precision}
                    prefix={prefix}
                    suffix={suffix}
                    formatter={formatter}
                />
            )}
        </Card>
    );
};

export default MetricCard;
