import { motion } from 'framer-motion'
import { Battery, MapPin, Zap } from 'lucide-react'
import type { Station } from '../types'
import { StatusBadge } from './StatusBadge'

export function StationCard({
  station,
  onClick,
  selected,
}: {
  station: Station
  onClick: () => void
  selected?: boolean
}) {
  return (
    <motion.button
      layout
      onClick={onClick}
      whileHover={{ scale: 1.01 }}
      whileTap={{ scale: 0.99 }}
      className={`w-full rounded-xl border p-4 text-left transition-colors ${
        selected
          ? 'border-ev-green/50 bg-ev-green/5'
          : 'border-white/5 bg-ev-card hover:border-ev-cyan/30'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <h3 className="truncate font-semibold text-white">{station.name}</h3>
          <p className="mt-0.5 text-xs text-slate-400">{station.operatorName} · {station.city}</p>
        </div>
        <StatusBadge status={station.status} />
      </div>
      <div className="mt-3 flex flex-wrap gap-3 text-xs text-slate-300">
        <span className="flex items-center gap-1">
          <MapPin className="h-3.5 w-3.5 text-ev-cyan" />
          {station.area}
        </span>
        <span className="flex items-center gap-1">
          <Zap className="h-3.5 w-3.5 text-ev-green" />
          {station.powerKw} kW
        </span>
        <span className="flex items-center gap-1">
          <Battery className="h-3.5 w-3.5 text-ev-amber" />
          {station.waitMinutes}m wait
        </span>
      </div>
    </motion.button>
  )
}
