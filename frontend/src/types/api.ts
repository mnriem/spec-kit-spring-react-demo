// API Types mirroring contracts/openapi.yaml
// Uses camelCase to match Spring Boot Jackson default serialization

export interface SuccessEnvelope<T> {
  data: T
  timestamp: string
}

export interface ErrorEnvelope {
  error: string
  message: string
  fieldErrors?: FieldError[]
  timestamp: string
}

export interface FieldError {
  field: string
  message: string
}

export interface ProjectResponse {
  id: string
  name: string
  description: string | null
  createdAt: string
  experimentCount?: number
  interactionCount?: number
}

export interface CreateProjectRequest {
  name: string
  description?: string | null
}

export interface ExperimentResponse {
  id: string
  projectId: string
  name: string
  description: string | null
  createdAt: string
  iterationCount?: number
}

export interface IterationResponse {
  id: string
  experimentId: string
  name: string
  description: string | null
  createdAt: string
}

export interface ToolCallRequest {
  toolName: string
  inputArguments?: Record<string, unknown> | null
  output?: Record<string, unknown> | null
  calledAt?: string | null
}

export interface ToolCallResponse {
  id: string
  toolName: string
  inputArguments: Record<string, unknown> | null
  output: Record<string, unknown> | null
  sequenceOrder: number
  calledAt: string | null
}

export interface CreateInteractionRequest {
  projectName: string
  projectDescription?: string | null
  experimentName: string
  iterationName: string
  model: string
  prompt?: string | null
  responseMetadata?: Record<string, unknown> | null
  tokensIn: number
  tokensOut: number
  startedAt: string
  endedAt: string
  toolsCalled?: ToolCallRequest[] | null
}

export interface InteractionSummaryResponse {
  id: string
  iterationId: string
  iterationName?: string
  experimentId?: string
  experimentName?: string
  projectId?: string
  projectName?: string
  model: string
  tokensIn: number
  tokensOut: number
  totalTokens: number
  latencyMs: number
  tokensPerSecond: number | null
  estimatedCost: string | null
  toolCallCount: number
  startedAt: string
  endedAt: string
  createdAt: string
}

export interface InteractionDetailResponse extends InteractionSummaryResponse {
  prompt: string | null
  responseMetadata: Record<string, unknown> | null
  toolCalls: ToolCallResponse[]
}

export interface DashboardSummaryResponse {
  totalInteractions: number
  avgLatencyMs: number | null
  totalTokens: number | null
  totalEstimatedCost: number | null
}

export interface TimelineDataPoint {
  bucket: string
  interactionCount: number
  avgLatencyMs: number | null
  totalTokens: number | null
}

export interface TokensByModelEntry {
  model: string
  totalTokensIn: number
  totalTokensOut: number
}

export interface LatencyBucketEntry {
  bucket: string
  count: number
}

export interface ToolUsageEntry {
  toolName: string
  usageCount: number
  avgPerInteraction: number | null
}

export interface IterationMetrics {
  iterationId: string
  iterationName: string
  avgLatencyMs: number | null
  avgTokensIn: number | null
  avgTokensOut: number | null
  avgTotalTokens: number | null
  toolCallRate: number | null
  avgEstimatedCost: string | null
  interactionCount: number
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface SampleDataResponse {
  projectCount: number
  experimentCount: number
  iterationCount: number
  interactionCount: number
  message?: string
}
