import { AnimatePresence, motion } from 'framer-motion'
import { Clock, MapPin, Shield, X, Zap } from 'lucide-react'
import type { Station } from '../types'
import { EvoScoreRing } from './EvoScoreRing'
import { StatusBadge } from './StatusBadge'

export function StationDetail({
  station,
  onClose,
}: {
  station: Station | null
  onClose: () => void
}) {
  return (
    <AnimatePresence>
      {station && (
        <motion.div
          initial={{ y: '100%' }}
          animate={{ y: 0 }}
          exit={{ y: '100%' }}
          transition={{ type: 'spring', damping: 28, stiffness: 300 }}
          className="absolute inset-x-0 bottom-16 z-20 mx-3 rounded-2xl border border-white/10 bg-ev-surface/95 p-4 shadow-2xl backdrop-blur-md sm:p-5 md:inset-x-auto md:right-4 md:bottom-4 md:mx-0 md:w-[420px]"
        >
          <div className="flex items-start justify-between gap-3">
            <div className="flex gap-4">
              <EvoScoreRing score={station.evoScore || station.reliabilityScore} />
              <div>
                <h2 className="text-lg font-bold text-white">{station.name}</h2>
                <p className="text-sm text-slate-400">{station.operatorName} · {station.city}</p>
                <div className="mt-2">
                  <StatusBadge status={station.status} />
                </div>
              </div>
            </div>
            <button onClick={onClose} className="rounded-lg p-1 hover:bg-white/10">
              <X className="h-5 w-5 text-slate-400" />
            </button>
          </div>

          <p className="mt-4 flex items-start gap-2 text-sm text-slate-300">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-ev-cyan" />
            {station.address}
          </p>

          <div className="mt-4 grid grid-cols-2 gap-2.5 sm:gap-3">
            <div className="rounded-lg bg-ev-card p-3">
              <div className="flex items-center gap-1 text-xs text-slate-400">
                <Zap className="h-3.5 w-3.5" /> Power
              </div>
              <p className="mt-1 font-semibold">{station.powerKw} kW</p>
            </div>
            <div className="rounded-lg bg-ev-card p-3">
              <div className="flex items-center gap-1 text-xs text-slate-400">
                <Clock className="h-3.5 w-3.5" /> Wait
              </div>
              <p className="mt-1 font-semibold">{station.waitMinutes} min</p>
            </div>
            <div className="rounded-lg bg-ev-card p-3">
              <div className="flex items-center gap-1 text-xs text-slate-400">
                <Shield className="h-3.5 w-3.5" /> Reliability
              </div>
              <p className="mt-1 font-semibold">{station.reliabilityScore}%</p>
            </div>
            <div className="rounded-lg bg-ev-card p-3">
              <div className="text-xs text-slate-400">Grid</div>
              <p className="mt-1 font-semibold text-ev-cyan">{station.gridStatus}</p>
            </div>
          </div>

          <div className="mt-4">
            <p className="text-xs text-slate-400">Connectors</p>
            <div className="mt-2 flex flex-wrap gap-2">
              {station.connectors.map((c) => (
                <span key={c} className="rounded-md bg-ev-green/10 px-2 py-1 text-xs text-ev-green">
                  {c}
                </span>
              ))}
            </div>
          </div>

          <a
            href={`https://www.google.com/maps/dir/?api=1&destination=${station.lat},${station.lng}`}
            target="_blank"
            rel="noreferrer"
            className="mt-4 block w-full rounded-xl bg-ev-green py-3 text-center text-sm font-semibold text-ev-dark transition hover:bg-ev-green/90"
          >
            Navigate to Station
          </a>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
