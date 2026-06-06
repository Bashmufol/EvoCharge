import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { AlertTriangle, Battery, CheckCircle, Clock, Zap } from 'lucide-react'
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { api } from '../api/client'
import { KpiCard } from '../components/KpiCard'

const STATUS_COLORS = ['#00e676', '#ffb300', '#ff5252']

export function DashboardPage() {
  const { data: summary } = useQuery({ queryKey: ['summary'], queryFn: api.getSummary, refetchInterval: 30_000 })
  const { data: demand = [] } = useQuery({ queryKey: ['demand'], queryFn: api.getDemandByArea })

  const statusData = summary
    ? [
        { name: 'Available', value: summary.available },
        { name: 'Busy', value: summary.busy },
        { name: 'Offline', value: summary.offline },
      ]
    : []

  const operatorData = summary
    ? Object.entries(summary.byOperator).map(([name, count]) => ({ name, count }))
    : []

  const unmetAreas = demand.filter((d) => d.unmetDemand)

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Network Overview</h1>
        <p className="text-sm text-slate-400">Multi-operator charging intelligence across Lagos</p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard title="Total Stations" value={summary?.totalStations ?? '—'} icon={Zap} />
        <KpiCard title="Available" value={summary?.available ?? '—'} icon={CheckCircle} color="text-ev-green" />
        <KpiCard title="Utilization" value={`${summary?.utilizationPercent ?? 0}%`} icon={Battery} color="text-ev-amber" />
        <KpiCard title="Avg Wait" value={`${summary?.avgWaitMinutes ?? 0}m`} icon={Clock} color="text-ev-cyan" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-white/5 bg-ev-card p-5">
          <h3 className="mb-4 font-semibold">Status Distribution</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={50} outerRadius={80}>
                {statusData.map((_, i) => (
                  <Cell key={i} fill={STATUS_COLORS[i]} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ background: '#1a2540', border: 'none', borderRadius: 8 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-2xl border border-white/5 bg-ev-card p-5">
          <h3 className="mb-4 font-semibold">Stations by Operator</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={operatorData}>
              <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: '#1a2540', border: 'none', borderRadius: 8 }} />
              <Bar dataKey="count" fill="#00e676" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {unmetAreas.length > 0 && (
        <div className="rounded-2xl border border-ev-red/20 bg-ev-red/5 p-5">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-ev-red" />
            <h3 className="font-semibold text-ev-red">Unmet Demand Areas</h3>
          </div>
          <div className="mt-3 grid gap-2 md:grid-cols-3">
            {unmetAreas.map((a) => (
              <div key={a.area} className="rounded-xl bg-ev-card p-3">
                <p className="font-medium">{a.area}</p>
                <p className="text-xs text-slate-400">{a.stationCount} stations · demand {a.demandScore}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </motion.div>
  )
}
