import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
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

  return (
    <div className="flex h-full">
      <Sidebar page={page} onPage={setPage} />
      <main className="flex-1 overflow-y-auto p-6 md:p-8">
        {page === 'dashboard' && <DashboardPage />}
        {page === 'stations' && <StationsPage />}
        {page === 'analytics' && <AnalyticsPage />}
      </main>
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
