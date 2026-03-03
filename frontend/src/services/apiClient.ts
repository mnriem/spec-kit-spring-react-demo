import axios, { AxiosInstance, AxiosResponse } from 'axios'
import type { SuccessEnvelope, ErrorEnvelope } from '../types/api'

const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': import.meta.env.VITE_API_KEY ?? '',
  },
})

// Response interceptor: unwrap SuccessEnvelope data
apiClient.interceptors.response.use(
  (response: AxiosResponse<SuccessEnvelope<unknown>>) => {
    // Pass through the full response; callers can access .data.data
    return response
  },
  (error) => {
    const envelope: ErrorEnvelope | undefined = error.response?.data
    if (envelope) {
      const err = new Error(envelope.message ?? 'API error')
      ;(err as Error & { envelope: ErrorEnvelope }).envelope = envelope
      return Promise.reject(err)
    }
    return Promise.reject(error)
  }
)

export function unwrap<T>(response: AxiosResponse<SuccessEnvelope<T>>): T {
  return response.data.data
}

export default apiClient
