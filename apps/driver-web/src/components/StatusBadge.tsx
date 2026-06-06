import { motion } from 'framer-motion'
import type { StationStatus } from '../types'

const config: Record<StationStatus, { label: string; color: string; bg: string }> = {
  AVAILABLE: { label: 'Available', color: 'text-ev-green', bg: 'bg-ev-green/15' },
  BUSY: { label: 'Busy', color: 'text-ev-amber', bg: 'bg-ev-amber/15' },
  OFFLINE: { label: 'Offline', color: 'text-ev-red', bg: 'bg-ev-red/15' },
}

export function StatusBadge({ status }: { status: StationStatus }) {
  const c = config[status]
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${c.bg} ${c.color}`}>
      {status === 'AVAILABLE' && (
        <motion.span
          className="h-2 w-2 rounded-full bg-ev-green"
          animate={{ scale: [1, 1.4, 1], opacity: [1, 0.6, 1] }}
          transition={{ repeat: Infinity, duration: 2 }}
        />
      )}
      {status !== 'AVAILABLE' && <span className={`h-2 w-2 rounded-full ${status === 'BUSY' ? 'bg-ev-amber' : 'bg-ev-red'}`} />}
      {c.label}
    </span>
  )
}
