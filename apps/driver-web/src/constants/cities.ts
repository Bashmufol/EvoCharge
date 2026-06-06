export type CityFilter = '' | 'Lagos' | 'Abuja' | 'Port Harcourt'

export const CITIES: { id: CityFilter; label: string }[] = [
  { id: '', label: 'All Cities' },
  { id: 'Lagos', label: 'Lagos' },
  { id: 'Abuja', label: 'Abuja' },
  { id: 'Port Harcourt', label: 'Port Harcourt' },
]

/** Map center as [lng, lat] for MapLibre */
export const CITY_CENTERS: Record<string, [number, number]> = {
  Lagos: [3.4219, 6.4281],
  Abuja: [7.4891, 9.0579],
  'Port Harcourt': [7.0134, 4.8156],
}

export const DEFAULT_CITY = 'Lagos'
