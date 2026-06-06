import { motion } from 'framer-motion'
import { Bot, Send } from 'lucide-react'
import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { api } from '../api/client'

const SUGGESTIONS = [
  'Find a fast charger near Victoria Island with less than 10 min wait',
  'I have low battery, where is the nearest available station?',
  'Best CCS2 charger in Lekki right now',
]

export function AdvisorChat({ lat, lng }: { lat: number; lng: number }) {
  const [query, setQuery] = useState('')
  const [answer, setAnswer] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: (q: string) => api.advise({ query: q, lat, lng }),
    onSuccess: (data) => setAnswer(data.answer),
  })

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-2xl border border-ev-cyan/20 bg-ev-surface p-4"
    >
      <div className="flex items-center gap-2">
        <Bot className="h-5 w-5 text-ev-cyan" />
        <h3 className="font-semibold text-white">AI Charge Advisor</h3>
        <span className="rounded-full bg-ev-cyan/10 px-2 py-0.5 text-[10px] text-ev-cyan">Bedrock</span>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {SUGGESTIONS.map((s) => (
          <button
            key={s}
            onClick={() => { setQuery(s); mutation.mutate(s) }}
            className="rounded-lg border border-white/5 bg-ev-card px-2 py-1 text-left text-[11px] text-slate-300 hover:border-ev-cyan/30"
          >
            {s}
          </button>
        ))}
      </div>

      <div className="mt-3 flex gap-2">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && query && mutation.mutate(query)}
          placeholder="Ask anything about charging in Lagos..."
          className="flex-1 rounded-xl border border-white/10 bg-ev-card px-3 py-2 text-sm outline-none focus:border-ev-cyan/50"
        />
        <button
          onClick={() => query && mutation.mutate(query)}
          disabled={mutation.isPending}
          className="rounded-xl bg-ev-cyan p-2.5 text-ev-dark hover:opacity-90 disabled:opacity-50"
        >
          <Send className="h-5 w-5" />
        </button>
      </div>

      {mutation.isPending && (
        <p className="mt-3 animate-pulse text-sm text-slate-400">Advisor is thinking...</p>
      )}

      {answer && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="mt-3 rounded-xl bg-ev-card p-3 text-sm leading-relaxed text-slate-200"
        >
          {answer}
        </motion.div>
      )}
    </motion.div>
  )
}
