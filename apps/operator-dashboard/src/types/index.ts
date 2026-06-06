export type StationStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE'

export interface Station {
  id: string
  name: string
  operatorId: string
  operatorName: string
  lat: number
  lng: number
  address: string
  city: string
  area: string
  status: StationStatus
  connectors: string[]
  powerKw: number
  waitMinutes: number
  reliabilityScore: number
  gridStatus: string
  evoScore: number
  lastUpdated: string
}

export interface AnalyticsSummary {
  totalStations: number
  available: number
  busy: number
  offline: number
  utilizationPercent: number
  avgWaitMinutes: number
  peakHour: number
  byOperator: Record<string, number>
}

export interface DemandArea {
  area: string
  city: string
  lat: number
  lng: number
  stationCount: number
  demandScore: number
  unmetDemand: boolean
  estimatedDailySessions: number
}
