import { Activity, ArrowLeft, BarChart3, LayoutDashboard, Zap } from 'lucide-react'
import { DRIVER_APP_URL } from '../constants/navigation'

export type Page = 'dashboard' | 'stations' | 'analytics'

export function Sidebar({ page, onPage }: { page: Page; onPage: (p: Page) => void }) {
  const items: { id: Page; label: string; icon: typeof Zap }[] = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'stations', label: 'Stations', icon: Activity },
    { id: 'analytics', label: 'Analytics', icon: BarChart3 },
  ]

  return (
    <aside className="hidden w-56 shrink-0 flex-col border-r border-white/5 bg-ev-surface p-4 md:flex">
      <div className="mb-8 flex items-center gap-2">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-ev-green to-ev-cyan">
          <Zap className="h-5 w-5 text-ev-dark" />
        </div>
        <div>
          <p className="font-bold text-white">EvoCharge</p>
          <p className="text-[10px] text-slate-400">Operator Console</p>
        </div>
      </div>

      <nav className="space-y-1">
        {items.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => onPage(id)}
            className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition ${
              page === id
                ? 'bg-ev-green/10 font-medium text-ev-green'
                : 'text-slate-400 hover:bg-white/5 hover:text-white'
            }`}
          >
            <Icon className="h-4 w-4" />
            {label}
          </button>
        ))}
      </nav>

      <a
        href={DRIVER_APP_URL}
        className="mb-3 flex items-center gap-2 rounded-xl border border-white/10 px-3 py-2.5 text-sm text-slate-400 transition hover:border-ev-green/30 hover:text-ev-green"
      >
        <ArrowLeft className="h-4 w-4" />
        Driver app
      </a>

      <div className="mt-auto rounded-xl border border-ev-cyan/20 bg-ev-cyan/5 p-3">
        <p className="text-xs font-medium text-ev-cyan">Network Pulse</p>
        <p className="mt-1 text-[10px] text-slate-400">Live status updates via EventBridge + SSE</p>
      </div>
    </aside>
  )
}
