import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { api } from '../api/client'
import type { Station, StatusEvent } from '../types'

export function useNetworkPulse() {
  const queryClient = useQueryClient()

  useEffect(() => {
    const es = new EventSource(api.eventsUrl())

    es.addEventListener('status', (event) => {
      const data: StatusEvent = JSON.parse(event.data)
      queryClient.setQueriesData<Station[]>(
        { queryKey: ['stations'] },
        (old) => {
          if (!old) return old
          return old.map((s) =>
            s.id === data.stationId
              ? { ...s, status: data.status, waitMinutes: data.waitMinutes, lastUpdated: data.timestamp }
              : s
          )
        }
      )
    })

    return () => es.close()
  }, [queryClient])
}
