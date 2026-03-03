import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import { ProjectProvider } from './context/ProjectContext'
import { Spin } from 'antd'

const Dashboard = lazy(() => import('./pages/Dashboard/Dashboard'))
const Comparison = lazy(() => import('./pages/Comparison/Comparison'))
const Interactions = lazy(() => import('./pages/Interactions/InteractionsList'))

const LoadingFallback = () => (
  <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}>
    <Spin size="large" />
  </div>
)

export default function App() {
  return (
    <ProjectProvider>
      <BrowserRouter>
        <Layout>
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/comparison" element={<Comparison />} />
              <Route path="/interactions" element={<Interactions />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </Layout>
      </BrowserRouter>
    </ProjectProvider>
  )
}
