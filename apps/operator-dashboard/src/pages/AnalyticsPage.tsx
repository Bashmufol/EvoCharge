import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { api } from '../api/client'

const PEAK_HOURS = [
  { hour: '6am', sessions: 12 },
  { hour: '9am', sessions: 45 },
  { hour: '12pm', sessions: 38 },
  { hour: '3pm', sessions: 52 },
  { hour: '6pm', sessions: 78 },
  { hour: '9pm', sessions: 34 },
]

export function AnalyticsPage() {
  const { data: demand = [] } = useQuery({ queryKey: ['demand'], queryFn: api.getDemandByArea })
  const { data: summary } = useQuery({ queryKey: ['summary'], queryFn: api.getSummary })

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white sm:text-2xl">Demand Analytics</h1>
        <p className="text-sm text-slate-400">Insights for operators, investors, and policymakers</p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-white/5 bg-ev-card p-5">
          <h3 className="mb-4 font-semibold">Demand Score by Area</h3>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={demand} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#1a2540" />
              <XAxis type="number" tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <YAxis dataKey="area" type="category" width={90} tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: '#1a2540', border: 'none', borderRadius: 8 }} />
              <Bar dataKey="demandScore" fill="#00b8d4" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-2xl border border-white/5 bg-ev-card p-5">
          <h3 className="mb-4 font-semibold">Peak Charging Hours</h3>
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={PEAK_HOURS}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1a2540" />
              <XAxis dataKey="hour" tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: '#1a2540', border: 'none', borderRadius: 8 }} />
              <Line type="monotone" dataKey="sessions" stroke="#00e676" strokeWidth={2} dot={{ fill: '#00e676' }} />
            </LineChart>
          </ResponsiveContainer>
          <p className="mt-2 text-xs text-slate-500">Peak hour: {summary?.peakHour ?? 17}:00 WAT</p>
        </div>
      </div>

      <div className="rounded-2xl border border-white/5 bg-ev-card p-5">
        <h3 className="mb-4 font-semibold">Estimated Daily Sessions by Area</h3>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={demand}>
            <CartesianGrid strokeDasharray="3 3" stroke="#1a2540" />
            <XAxis
              dataKey="area"
              tick={{ fill: '#94a3b8', fontSize: 9 }}
              angle={-25}
              textAnchor="end"
              height={70}
              tickFormatter={(v, i) => {
                const d = demand[i]
                return d ? `${v}, ${d.city}` : v
              }}
            />
            <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
            <Tooltip contentStyle={{ background: '#1a2540', border: 'none', borderRadius: 8 }} />
            <Bar dataKey="estimatedDailySessions" fill="#00e676" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="rounded-2xl border border-ev-green/20 bg-ev-green/5 p-5">
        <h3 className="font-semibold text-ev-green">Investment Summary</h3>
        <p className="mt-2 text-sm text-slate-300">
          Network utilization at {summary?.utilizationPercent ?? 0}% with {summary?.offline ?? 0} offline stations.
          Areas flagged for unmet demand represent high-ROI deployment opportunities for investors and policymakers.
        </p>
      </div>
    </motion.div>
  )
}
