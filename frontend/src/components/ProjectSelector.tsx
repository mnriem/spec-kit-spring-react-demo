import { Select } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { useProjectContext } from '../context/ProjectContext'
import apiClient, { unwrap } from '../services/apiClient'
import type { PagedResponse, ProjectResponse, SuccessEnvelope } from '../types/api'

export default function ProjectSelector() {
  const { selectedProjectId, setSelectedProjectId } = useProjectContext()

  const { data: projects, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      const res = await apiClient.get<SuccessEnvelope<PagedResponse<ProjectResponse>>>(
        '/projects?size=100'
      )
      return unwrap(res).content
    },
  })

  const options = [
    { value: '', label: 'All Projects' },
    ...(projects ?? []).map((p) => ({ value: p.id, label: p.name })),
  ]

  return (
    <Select
      style={{ minWidth: 240 }}
      value={selectedProjectId ?? ''}
      loading={isLoading}
      showSearch
      optionFilterProp="label"
      options={options}
      placeholder="All Projects"
      onChange={(val) => setSelectedProjectId(val || null)}
    />
  )
}
