import { motion } from 'framer-motion'
import type { LucideIcon } from 'lucide-react'

export function KpiCard({
  title,
  value,
  subtitle,
  icon: Icon,
  color = 'text-ev-green',
}: {
  title: string
  value: string | number
  subtitle?: string
  icon: LucideIcon
  color?: string
}) {
  return (
    <motion.div
      whileHover={{ y: -2 }}
      className="rounded-2xl border border-white/5 bg-ev-card p-5"
    >
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-400">{title}</p>
        <Icon className={`h-5 w-5 ${color}`} />
      </div>
      <p className="mt-2 text-3xl font-bold text-white">{value}</p>
      {subtitle && <p className="mt-1 text-xs text-slate-500">{subtitle}</p>}
    </motion.div>
  )
}
