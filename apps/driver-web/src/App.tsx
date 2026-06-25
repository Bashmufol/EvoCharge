import { useQuery } from '@tanstack/react-query'
import { AnimatePresence, motion } from 'framer-motion'
import { Activity, Flame, LayoutDashboard, List, Map as MapIcon, Search, Zap } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { api } from './api/client'
import { AdvisorChat } from './components/AdvisorChat'
import { MapView } from './components/MapView'
import { RecommendPanel } from './components/RecommendPanel'
import { StationDetail } from './components/StationDetail'
import { CITIES, CITY_CENTERS, DEFAULT_CITY, type CityFilter } from './constants/cities'
import { OPERATOR_APP_URL } from './constants/navigation'
import { useNetworkPulse } from './hooks/useNetworkPulse'
import type { Station, StationStatus } from './types'

type Tab = 'map' | 'list' | 'advisor'

export default function App() {
  const [tab, setTab] = useState<Tab>('map')
  const [selected, setSelected] = useState<Station | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<StationStatus | ''>('')
  const [operatorFilter, setOperatorFilter] = useState('')
  const [cityFilter, setCityFilter] = useState<CityFilter>('')
  const [showHeat, setShowHeat] = useState(false)
  const [userLocation, setUserLocation] = useState<[number, number] | undefined>()
  const [mapCenter, setMapCenter] = useState<[number, number]>(CITY_CENTERS[DEFAULT_CITY])
  const [locating, setLocating] = useState(false)
  const [locationError, setLocationError] = useState<string | null>(null)

  useNetworkPulse()

  const { data: stations = [], isLoading } = useQuery({
    queryKey: userLocation
      ? ['stations', 'nearby', userLocation[0], userLocation[1], statusFilter, operatorFilter, search]
      : ['stations', statusFilter, operatorFilter, search, cityFilter],
    queryFn: async () => {
      if (userLocation) {
        const nearby = await api.getNearby(userLocation[1], userLocation[0], 25)
        return nearby.filter((s) => {
          if (statusFilter && s.status !== statusFilter) return false
          if (operatorFilter && s.operatorId !== operatorFilter) return false
          if (search) {
            const q = search.toLowerCase()
            const hay = `${s.name} ${s.area} ${s.city} ${s.address}`.toLowerCase()
            if (!hay.includes(q)) return false
          }
          return true
        })
      }
      return api.getStations({
        status: statusFilter || undefined,
        operator: operatorFilter || undefined,
        search: search || undefined,
        city: cityFilter || undefined,
      })
    },
    refetchInterval: 60_000,
  })

  const { data: operators = [] } = useQuery({
    queryKey: ['operators'],
    queryFn: api.getOperators,
  })

  const { data: demandAreas = [] } = useQuery({
    queryKey: ['demand', cityFilter],
    queryFn: () => api.getDemandByArea(cityFilter || undefined),
    enabled: showHeat,
  })

  useEffect(() => {
    if (userLocation) return
    if (cityFilter && CITY_CENTERS[cityFilter]) {
      setMapCenter(CITY_CENTERS[cityFilter])
    } else if (!cityFilter) {
      setMapCenter(CITY_CENTERS[DEFAULT_CITY])
    }
  }, [cityFilter, userLocation])

  const activeCenter = CITY_CENTERS[cityFilter || DEFAULT_CITY] ?? CITY_CENTERS[DEFAULT_CITY]
  const userLat = userLocation?.[1] ?? activeCenter[1]
  const userLng = userLocation?.[0] ?? activeCenter[0]

  const stats = useMemo(() => ({
    available: stations.filter((s) => s.status === 'AVAILABLE').length,
    total: stations.length,
  }), [stations])

  const recommendFilterKey = `${cityFilter}|${statusFilter}|${operatorFilter}|${search}`

  const handleNearMe = () => {
    if (!navigator.geolocation) {
      setLocationError('Location is not supported in this browser.')
      return
    }

    setLocating(true)
    setLocationError(null)
    setTab('map')

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lng = pos.coords.longitude
        const lat = pos.coords.latitude
        setLocating(false)
        setCityFilter('')
        setUserLocation([lng, lat])
        setMapCenter([lng, lat])
      },
      (err) => {
        setLocating(false)
        setUserLocation(undefined)
        setLocationError(
          err.code === err.PERMISSION_DENIED
            ? 'Location permission denied. Allow access in your browser settings and try again.'
            : 'Could not get your location. Check that location services are enabled.'
        )
      },
      { enableHighAccuracy: true, timeout: 15_000, maximumAge: 0 }
    )
  }

  const handleCityChange = (city: CityFilter) => {
    setCityFilter(city)
    setSelected(null)
    setUserLocation(undefined)
    setLocationError(null)
    if (city && CITY_CENTERS[city]) {
      setMapCenter(CITY_CENTERS[city])
    } else {
      setMapCenter(CITY_CENTERS[DEFAULT_CITY])
    }
  }

  return (
    <div className="flex h-full min-h-dvh flex-col bg-ev-dark">
      <header className="z-30 border-b border-white/5 bg-ev-surface/90 px-3 py-3 backdrop-blur-md sm:px-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-ev-green to-ev-cyan">
              <Zap className="h-5 w-5 text-ev-dark" />
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight text-white">EvoCharge</h1>
              <p className="text-[10px] text-slate-400">Nigeria's EV Charging Intelligence</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <a
              href={OPERATOR_APP_URL}
              className="flex items-center gap-1.5 rounded-full border border-white/10 bg-ev-card px-3 py-1.5 text-xs font-medium text-slate-300 transition hover:border-ev-cyan/30 hover:text-ev-cyan"
            >
              <LayoutDashboard className="h-3.5 w-3.5" />
              Operator
            </a>
            <div className="flex items-center gap-2 rounded-full bg-ev-green/10 px-3 py-1.5 text-xs">
              <Activity className="h-3.5 w-3.5 text-ev-green" />
              <span className="text-ev-green">{stats.available}/{stats.total} live</span>
            </div>
          </div>
        </div>

        <div className="mt-3 flex gap-1.5 overflow-x-auto pb-1">
          {CITIES.map(({ id, label }) => (
            <button
              key={label}
              onClick={() => handleCityChange(id)}
              className={`shrink-0 rounded-full px-3 py-1.5 text-xs font-semibold transition ${
                cityFilter === id
                  ? 'bg-ev-cyan text-ev-dark'
                  : 'bg-ev-card text-slate-300 hover:bg-white/10'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        <div className="mt-2 flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search area, station..."
              className="w-full rounded-xl border border-white/10 bg-ev-card py-2 pl-9 pr-3 text-sm outline-none focus:border-ev-green/40"
            />
          </div>
          <button
            onClick={handleNearMe}
            disabled={locating}
            className="shrink-0 rounded-xl border border-ev-cyan/30 bg-ev-cyan/10 px-3 text-xs font-medium text-ev-cyan disabled:opacity-50"
          >
            {locating ? 'Locating...' : 'Near me'}
          </button>
        </div>
        {locationError && (
          <p className="mt-1.5 text-xs text-ev-red">{locationError}</p>
        )}
        {userLocation && !locationError && (
          <p className="mt-1.5 text-xs text-ev-cyan">
            {stations.length > 0
              ? 'Showing chargers near your location'
              : 'No chargers within 25 km — pick a city above to browse the network'}
          </p>
        )}

        <div className="mt-2 flex gap-2 overflow-x-auto pb-1">
          {(['', 'AVAILABLE', 'BUSY', 'OFFLINE'] as const).map((s) => (
            <button
              key={s || 'all'}
              onClick={() => setStatusFilter(s)}
              className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium transition ${
                statusFilter === s
                  ? 'bg-ev-green text-ev-dark'
                  : 'bg-ev-card text-slate-300 hover:bg-white/10'
              }`}
            >
              {s || 'All Status'}
            </button>
          ))}
          <select
            value={operatorFilter}
            onChange={(e) => setOperatorFilter(e.target.value)}
            className="shrink-0 rounded-full border-0 bg-ev-card px-3 py-1 text-xs text-slate-300"
          >
            <option value="">All Operators</option>
            {operators.map((o) => (
              <option key={o.id} value={o.id}>{o.name}</option>
            ))}
          </select>
          {tab === 'map' && (
            <button
              onClick={() => setShowHeat(!showHeat)}
              className={`flex shrink-0 items-center gap-1 rounded-full px-3 py-1 text-xs font-medium ${
                showHeat ? 'bg-ev-red/20 text-ev-red' : 'bg-ev-card text-slate-300'
              }`}
            >
              <Flame className="h-3 w-3" /> Heat
            </button>
          )}
        </div>
      </header>

      <main className="relative flex-1 overflow-hidden">
        <AnimatePresence mode="wait">
          {tab === 'map' && (
            <motion.div key="map" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="absolute inset-0">
              {isLoading ? (
                <div className="flex h-full items-center justify-center text-slate-400">Loading network...</div>
              ) : (
                <MapView
                  stations={stations}
                  selectedId={selected?.id}
                  onSelect={setSelected}
                  demandAreas={demandAreas}
                  showHeat={showHeat}
                  center={mapCenter}
                  userLocation={userLocation}
                />
              )}
              <StationDetail station={selected} onClose={() => setSelected(null)} />
            </motion.div>
          )}

          {tab === 'list' && (
            <motion.div
              key="list"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 overflow-y-auto p-3 pb-24 sm:p-4"
            >
              <div className="mx-auto w-full max-w-5xl">
                <RecommendPanel
                  lat={userLat}
                  lng={userLng}
                  filterKey={recommendFilterKey}
                  onSelect={(station) => {
                    setSelected(station)
                    setTab('map')
                  }}
                />
              </div>
            </motion.div>
          )}

          {tab === 'advisor' && (
            <motion.div
              key="advisor"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 overflow-y-auto p-3 pb-24 sm:p-4"
            >
              <div className="mx-auto w-full max-w-3xl">
                <AdvisorChat lat={userLat} lng={userLng} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      <nav className="z-30 flex border-t border-white/5 bg-ev-surface px-2 py-2">
        {([
          { id: 'map' as Tab, icon: MapIcon, label: 'Map' },
          { id: 'list' as Tab, icon: List, label: 'Stations' },
          { id: 'advisor' as Tab, icon: Zap, label: 'Advisor' },
        ]).map(({ id, icon: Icon, label }) => (
          <button
            key={id}
            onClick={() => setTab(id)}
            className={`flex flex-1 flex-col items-center gap-0.5 rounded-xl py-2 text-xs transition ${
              tab === id ? 'bg-ev-green/10 text-ev-green' : 'text-slate-400 hover:text-white'
            }`}
          >
            <Icon className="h-5 w-5" />
            {label}
          </button>
        ))}
      </nav>
    </div>
  )
}
