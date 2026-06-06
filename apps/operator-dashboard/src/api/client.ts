import type { AnalyticsSummary, DemandArea, Station } from '../types'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

async function fetchJson<T>(path: string): Promise<T> {
  const res = await fetch(`${API_URL}${path}`)
  if (!res.ok) throw new Error(`API error: ${res.status}`)
  return res.json()
}

export const api = {
  getStations: () => fetchJson<Station[]>('/api/v1/stations'),
  getSummary: () => fetchJson<AnalyticsSummary>('/api/v1/analytics/summary'),
  getDemandByArea: () => fetchJson<DemandArea[]>('/api/v1/analytics/demand-by-area'),
  eventsUrl: () => `${API_URL}/api/v1/events/stream`,
}
