import { motion } from 'framer-motion'
import { Battery, Sparkles } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { api } from '../api/client'
import type { RankedStation, Station } from '../types'
import { EvoScoreRing } from './EvoScoreRing'
import { StatusBadge } from './StatusBadge'

export function RecommendPanel({
  lat,
  lng,
  filterKey,
  onSelect,
}: {
  lat: number
  lng: number
  /** Changes when header filters change — clears stale recommendation results. */
  filterKey: string
  onSelect: (station: Station) => void
}) {
  const [battery, setBattery] = useState(35)
  const [connector, setConnector] = useState('CCS2')
  const [results, setResults] = useState<RankedStation[]>([])

  useEffect(() => {
    setResults([])
  }, [filterKey, lat, lng])

  const mutation = useMutation({
    mutationFn: () =>
      api.recommend({ lat, lng, batteryPercent: battery, connectorType: connector }),
    onSuccess: (data) => setResults(data.recommendations),
  })

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl border border-ev-green/20 bg-ev-surface p-4"
    >
      <div className="flex items-center gap-2">
        <Sparkles className="h-5 w-5 text-ev-green" />
        <h3 className="font-semibold text-white">EvoScore Recommendations</h3>
      </div>
      <p className="mt-2 text-xs text-slate-400">
        Use the filters above, then tap Find Best Charger to rank nearby stations. Browse all stations on the Map tab.
      </p>

      <div className="mt-4 space-y-3">
        <div>
          <label className="flex items-center justify-between text-xs text-slate-400">
            <span className="flex items-center gap-1"><Battery className="h-3.5 w-3.5" /> Battery Level</span>
            <span className="text-ev-green">{battery}%</span>
          </label>
          <input
            type="range"
            min={5}
            max={95}
            value={battery}
            onChange={(e) => setBattery(Number(e.target.value))}
            className="mt-1 w-full accent-ev-green"
          />
        </div>

        <select
          value={connector}
          onChange={(e) => setConnector(e.target.value)}
          className="w-full rounded-lg border border-white/10 bg-ev-card px-3 py-2 text-sm"
        >
          <option value="CCS2">CCS2 (Fast)</option>
          <option value="Type2">Type 2</option>
          <option value="CHAdeMO">CHAdeMO</option>
        </select>

        <button
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
          className="w-full rounded-xl bg-gradient-to-r from-ev-green to-ev-cyan py-3 text-sm font-bold text-ev-dark transition hover:opacity-90 disabled:opacity-50"
        >
          {mutation.isPending ? 'Analyzing network...' : 'Find Best Charger'}
        </button>
      </div>

      {results.length > 0 && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mt-4 space-y-2">
          <p className="text-xs font-medium uppercase tracking-wide text-ev-green">Your top picks</p>
          {results.map((r, i) => (
            <motion.button
              key={r.station.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: i * 0.1 }}
              onClick={() => onSelect(r.station)}
              className="flex w-full items-center gap-3 rounded-xl border border-white/5 bg-ev-card p-3 text-left hover:border-ev-green/30"
            >
              <EvoScoreRing score={r.evoScore} size={48} />
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{r.station.name}</p>
                <p className="text-xs text-slate-400">{r.distanceKm} km · {r.etaMinutes} min drive</p>
                <StatusBadge status={r.station.status} />
              </div>
            </motion.button>
          ))}
        </motion.div>
      )}
    </motion.div>
  )
}
