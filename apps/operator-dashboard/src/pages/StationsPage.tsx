import { useQuery, useQueryClient } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { useEffect } from 'react'
import { api } from '../api/client'
import type { Station, StationStatus } from '../types'

const statusStyle: Record<StationStatus, string> = {
  AVAILABLE: 'text-ev-green bg-ev-green/10',
  BUSY: 'text-ev-amber bg-ev-amber/10',
  OFFLINE: 'text-ev-red bg-ev-red/10',
}

export function StationsPage() {
  const queryClient = useQueryClient()
  const { data: stations = [], isLoading } = useQuery({
    queryKey: ['stations'],
    queryFn: api.getStations,
    refetchInterval: 30_000,
  })

  useEffect(() => {
    const es = new EventSource(api.eventsUrl())
    es.addEventListener('status', () => {
      queryClient.invalidateQueries({ queryKey: ['stations'] })
      queryClient.invalidateQueries({ queryKey: ['summary'] })
    })
    return () => es.close()
  }, [queryClient])

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      <h1 className="text-xl font-bold text-white sm:text-2xl">Station Management</h1>
      <p className="mb-6 text-sm text-slate-400">Live status across all aggregated operators</p>

      <div className="overflow-hidden rounded-2xl border border-white/5 bg-ev-card">
        <div className="overflow-x-auto">
        <table className="min-w-[880px] w-full text-left text-sm">
          <thead className="border-b border-white/5 bg-ev-surface text-xs text-slate-400">
            <tr>
              <th className="px-4 py-3">Station</th>
              <th className="px-4 py-3">Operator</th>
              <th className="px-4 py-3">City</th>
              <th className="px-4 py-3 hidden md:table-cell">Area</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Wait</th>
              <th className="px-4 py-3 hidden lg:table-cell">Reliability</th>
              <th className="px-4 py-3 hidden lg:table-cell">Updated</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={8} className="px-4 py-8 text-center text-slate-400">Loading...</td></tr>
            ) : (
              stations.map((s: Station) => (
                <tr key={s.id} className="border-b border-white/5 hover:bg-white/[0.02]">
                  <td className="px-4 py-3 font-medium">{s.name}</td>
                  <td className="px-4 py-3 text-slate-400">{s.operatorName}</td>
                  <td className="px-4 py-3 text-slate-400">{s.city}</td>
                  <td className="px-4 py-3 text-slate-400 hidden md:table-cell">{s.area}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${statusStyle[s.status]}`}>
                      {s.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">{s.waitMinutes}m</td>
                  <td className="px-4 py-3 hidden lg:table-cell">{s.reliabilityScore}%</td>
                  <td className="px-4 py-3 text-xs text-slate-500 hidden lg:table-cell">
                    {new Date(s.lastUpdated).toLocaleTimeString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        </div>
      </div>
    </motion.div>
  )
}
