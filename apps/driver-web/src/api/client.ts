import type {
  AdvisorResponse,
  DemandArea,
  Operator,
  RecommendResponse,
  Station,
  StationStatus,
} from '../types'

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
      throw new Error('API returned HTML instead of JSON. Check CloudFront /api routing and CORS on the API.')
    }
    throw new Error(`Expected JSON but got ${contentType || 'unknown content type'}`)
  }
  return JSON.parse(body) as T
}

export const api = {
  getStations: (params?: {
    operator?: string
    status?: StationStatus
    connector?: string
    search?: string
    city?: string
  }) => {
    const q = new URLSearchParams()
    if (params?.operator) q.set('operator', params.operator)
    if (params?.status) q.set('status', params.status)
    if (params?.connector) q.set('connector', params.connector)
    if (params?.search) q.set('search', params.search)
    if (params?.city) q.set('city', params.city)
    const qs = q.toString()
    return fetchJson<Station[]>(`/api/v1/stations${qs ? `?${qs}` : ''}`)
  },

  getStation: (id: string) => fetchJson<Station>(`/api/v1/stations/${id}`),

  getOperators: () => fetchJson<Operator[]>('/api/v1/operators'),

  getNearby: (lat: number, lng: number, radius = 10) =>
    fetchJson<Station[]>(`/api/v1/stations/nearby?lat=${lat}&lng=${lng}&radius=${radius}`),

  recommend: (body: { lat: number; lng: number; batteryPercent: number; connectorType: string }) =>
    fetchJson<RecommendResponse>('/api/v1/recommend', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  getDemandByArea: (city?: string) =>
    fetchJson<DemandArea[]>(
      `/api/v1/analytics/demand-by-area${city ? `?city=${encodeURIComponent(city)}` : ''}`
    ),

  advise: (body: { query: string; lat: number; lng: number }) =>
    fetchJson<AdvisorResponse>('/api/v1/advisor', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  eventsUrl: () => `${API_URL}/api/v1/events/stream`,
}
