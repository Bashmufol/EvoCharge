import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Activity, BarChart3, LayoutDashboard } from 'lucide-react'
import { useState } from 'react'
import { Sidebar, type Page } from './components/Sidebar'
import { AnalyticsPage } from './pages/AnalyticsPage'
import { DashboardPage } from './pages/DashboardPage'
import { StationsPage } from './pages/StationsPage'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } },
})

function AppContent() {
  const [page, setPage] = useState<Page>('dashboard')
  const tabs: { id: Page; label: string; icon: typeof LayoutDashboard }[] = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'stations', label: 'Stations', icon: Activity },
    { id: 'analytics', label: 'Analytics', icon: BarChart3 },
  ]

  return (
    <div className="flex h-full min-h-dvh bg-ev-dark">
      <Sidebar page={page} onPage={setPage} />
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 border-b border-white/5 bg-ev-surface/95 px-3 py-2 backdrop-blur-md md:hidden">
          <div className="flex gap-1.5 overflow-x-auto pb-0.5">
            {tabs.map(({ id, label, icon: Icon }) => (
              <button
                key={id}
                onClick={() => setPage(id)}
                className={`flex shrink-0 items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium transition ${
                  page === id
                    ? 'bg-ev-green/10 text-ev-green'
                    : 'bg-ev-card text-slate-300'
                }`}
              >
                <Icon className="h-3.5 w-3.5" />
                {label}
              </button>
            ))}
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-4 md:p-8">
          <div className="mx-auto w-full max-w-7xl">
            {page === 'dashboard' && <DashboardPage />}
            {page === 'stations' && <StationsPage />}
            {page === 'analytics' && <AnalyticsPage />}
          </div>
        </main>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  )
}
