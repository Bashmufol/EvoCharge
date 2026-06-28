import type { AnalyticsSummary, DemandArea, Station } from '../types'

// Production always uses same-origin /api (CloudFront → ALB). Never bake localhost into prod bundles.
const API_URL = import.meta.env.PROD
  ? ''
  : (import.meta.env.VITE_API_URL ?? '')

async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  const body = await res.text()
  if (!res.ok) {
    throw new Error(body.trim() || `Request failed (${res.status})`)
  }
  const contentType = res.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json') && !contentType.includes('+json')) {
    const preview = body.trimStart().slice(0, 40)
    if (preview.startsWith('<!') || preview.startsWith('<html')) {
      throw new Error('API returned HTML instead of JSON. Check CloudFront /api routing and CORS.')
    }
    throw new Error(`Expected JSON but got ${contentType || 'unknown content type'}`)
  }
  return JSON.parse(body) as T
}

export const api = {
  getStations: () => fetchJson<Station[]>('/api/v1/stations'),
  getSummary: () => fetchJson<AnalyticsSummary>('/api/v1/analytics/summary'),
  getDemandByArea: () => fetchJson<DemandArea[]>('/api/v1/analytics/demand-by-area'),
  eventsUrl: () => `${API_URL}/api/v1/events/stream`,
}
