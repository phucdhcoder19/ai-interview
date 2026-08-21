import { useEffect, useState } from 'react'

/**
 * Wait screen for slow model calls (question generation ~25s, grading ~20s).
 * Shows elapsed seconds so the app never looks frozen — a silent spinner
 * for 25 seconds reads as "broken" to every user.
 */
export default function Waiting({ title, hint }: { title: string; hint: string }) {
  const [seconds, setSeconds] = useState(0)

  useEffect(() => {
    const timer = setInterval(() => setSeconds((s) => s + 1), 1000)
    return () => clearInterval(timer)
  }, [])

  return (
    <div className="flex flex-col items-center gap-4 py-24 text-center" role="status" aria-live="polite">
      <div className="h-8 w-8 animate-spin rounded-full border-2 border-slate-200 border-t-accent dark:border-slate-700 dark:border-t-accent" />
      <div>
        <p className="font-display text-lg font-semibold">{title}</p>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          {hint} · {seconds}s
        </p>
      </div>
    </div>
  )
}
