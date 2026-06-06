import { useEffect, useRef } from 'react'
import maplibregl from 'maplibre-gl'
import type { DemandArea, Station, StationStatus } from '../types'

const STATUS_COLORS: Record<StationStatus, string> = {
  AVAILABLE: '#00e676',
  BUSY: '#ffb300',
  OFFLINE: '#ff5252',
}

function getTileUrl(): string {
  const region = import.meta.env.VITE_AWS_REGION ?? 'us-east-1'
  const mapName = import.meta.env.VITE_MAP_NAME
  const apiKey = import.meta.env.VITE_LOCATION_API_KEY
  if (mapName && apiKey) {
    return `https://maps.geo.${region}.amazonaws.com/maps/v0/maps/${mapName}/tiles/{z}/{x}/{y}?key=${apiKey}`
  }
  return 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
}

export function MapView({
  stations,
  selectedId,
  onSelect,
  demandAreas,
  showHeat,
  center,
}: {
  stations: Station[]
  selectedId?: string
  onSelect: (s: Station) => void
  demandAreas?: DemandArea[]
  showHeat?: boolean
  center?: [number, number]
}) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<maplibregl.Map | null>(null)
  const markersRef = useRef<Map<string, maplibregl.Marker>>(new Map())

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: {
        version: 8,
        sources: {
          basemap: {
            type: 'raster',
            tiles: [getTileUrl()],
            tileSize: 256,
            attribution: import.meta.env.VITE_MAP_NAME
              ? '© Amazon Location Service'
              : '© OpenStreetMap contributors',
          },
        },
        layers: [{ id: 'basemap', type: 'raster', source: 'basemap' }],
      },
      center: center ?? [3.4219, 6.4281],
      zoom: 11,
    })

    map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-right')
    mapRef.current = map

    return () => {
      markersRef.current.forEach((m) => m.remove())
      markersRef.current.clear()
      map.remove()
      mapRef.current = null
    }
  }, [])

  useEffect(() => {
    const map = mapRef.current
    if (!map) return

    const existing = markersRef.current
    const stationIds = new Set(stations.map((s) => s.id))

    existing.forEach((marker, id) => {
      if (!stationIds.has(id)) {
        marker.remove()
        existing.delete(id)
      }
    })

    stations.forEach((station) => {
      const color = STATUS_COLORS[station.status]
      const isSelected = station.id === selectedId
      const el = document.createElement('button')
      el.className = 'station-marker'
      el.innerHTML = `<div style="
        width:${isSelected ? 22 : 16}px;height:${isSelected ? 22 : 16}px;
        background:${color};border:2px solid white;border-radius:50%;
        box-shadow:0 0 ${station.status === 'AVAILABLE' ? 12 : 6}px ${color};
        cursor:pointer;transition:all 0.3s;
      "></div>`
      el.onclick = () => onSelect(station)

      const existingMarker = existing.get(station.id)
      if (existingMarker) {
        existingMarker.setLngLat([station.lng, station.lat])
        existingMarker.getElement().innerHTML = el.innerHTML
      } else {
        const marker = new maplibregl.Marker({ element: el })
          .setLngLat([station.lng, station.lat])
          .addTo(map)
        existing.set(station.id, marker)
      }
    })
  }, [stations, selectedId, onSelect])

  useEffect(() => {
    const map = mapRef.current
    if (!map || !map.isStyleLoaded()) return

    if (map.getLayer('heat-circles')) map.removeLayer('heat-circles')
    if (map.getSource('heat')) map.removeSource('heat')

    if (showHeat && demandAreas?.length) {
      const features = demandAreas.map((a) => ({
        type: 'Feature' as const,
        properties: { score: a.demandScore, unmet: a.unmetDemand },
        geometry: {
          type: 'Point' as const,
          coordinates: [a.lng, a.lat],
        },
      }))

      map.addSource('heat', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features },
      })

      map.addLayer({
        id: 'heat-circles',
        type: 'circle',
        source: 'heat',
        paint: {
          'circle-radius': ['*', ['get', 'score'], 0.8],
          'circle-color': [
            'case',
            ['get', 'unmet'],
            'rgba(255, 82, 82, 0.35)',
            'rgba(0, 230, 118, 0.25)',
          ],
          'circle-stroke-width': 1,
          'circle-stroke-color': 'rgba(0, 184, 212, 0.5)',
        },
      })
    }
  }, [showHeat, demandAreas])

  useEffect(() => {
    if (center && mapRef.current) {
      mapRef.current.flyTo({ center, zoom: 13, duration: 1200 })
    }
  }, [center])

  return <div ref={containerRef} className="h-full w-full" />
}
