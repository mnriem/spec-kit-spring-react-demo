import React from 'react'
import { Layout as AntLayout, Menu, Typography } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  DashboardOutlined,
  SwapOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons'
import ProjectSelector from './ProjectSelector'

const { Sider, Header, Content } = AntLayout
const { Title } = Typography

interface LayoutProps {
  children: React.ReactNode
}

const menuItems = [
  { key: '/', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/comparison', icon: <SwapOutlined />, label: 'Comparison' },
  { key: '/interactions', icon: <UnorderedListOutlined />, label: 'Interactions' },
]

export default function Layout({ children }: LayoutProps) {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" breakpoint="lg" collapsedWidth="0">
        <div style={{ padding: '16px', textAlign: 'center' }}>
          <Title level={5} style={{ color: '#fff', margin: 0 }}>LLM Analytics</Title>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <AntLayout>
        <Header style={{
          background: '#fff',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'flex-end',
          gap: 16,
          boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
        }}>
          <span style={{ color: '#666' }}>Project:</span>
          <ProjectSelector />
        </Header>
        <Content style={{ padding: 24, background: '#f0f2f5' }}>
          {children}
        </Content>
      </AntLayout>
    </AntLayout>
  )
}
