export type StationStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE'
export type GridStatus = 'STABLE' | 'UNSTABLE' | 'OUTAGE' | 'GENERATOR'

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
  gridStatus: GridStatus
  evoScore: number
  lastUpdated: string
}

export interface Operator {
  id: string
  name: string
  logoUrl: string
  stationCount: number
  coverage: string
}

export interface RankedStation {
  station: Station
  evoScore: number
  distanceKm: number
  etaMinutes: number
  reason: string
}

export interface RecommendResponse {
  recommendations: RankedStation[]
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

export interface AdvisorResponse {
  answer: string
  stations: RankedStation[]
}

export interface StatusEvent {
  stationId: string
  status: StationStatus
  waitMinutes: number
  timestamp: string
}
