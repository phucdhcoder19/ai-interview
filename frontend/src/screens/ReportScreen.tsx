import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getReport, type Report } from '../api/interview'
import Waiting from '../components/Waiting'

function scoreColor(score: number) {
  if (score >= 75) return 'text-emerald-600 dark:text-emerald-400'
  if (score >= 60) return 'text-amber-600 dark:text-amber-400'
  return 'text-red-600 dark:text-red-400'
}

export default function ReportScreen() {
  const { sessionId = '' } = useParams()
  const [report, setReport] = useState<Report | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getReport(sessionId)
      .then(setReport)
      .catch((e: Error) => setError(e.message))
  }, [sessionId])

  if (error) {
    return (
      <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
        {error}
      </p>
    )
  }

  if (!report) {
    return <Waiting title="Loading your report" hint="One moment" />
  }

  return (
    <div className="space-y-10">
      <div className="text-center">
        <p className="text-sm text-slate-500 dark:text-slate-400">
          {report.skillName} · {report.difficulty}
        </p>
        {report.evaluateStatus === 'COMPLETED' && report.overallScore !== null ? (
          <p className={`font-display text-7xl font-bold ${scoreColor(report.overallScore)}`}>
            {report.overallScore}
            <span className="text-2xl text-slate-400">/100</span>
          </p>
        ) : (
          <p className="mt-4 rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-700 dark:bg-amber-950 dark:text-amber-300">
            {report.evaluateStatus === 'FAILED'
              ? 'Grading failed — your answers are saved. Try completing the session again later.'
              : 'This session has not been graded yet.'}
          </p>
        )}
        {report.overallFeedback && (
          <p className="mx-auto mt-4 max-w-xl text-slate-600 dark:text-slate-300">{report.overallFeedback}</p>
        )}
      </div>

      {(report.strengths.length > 0 || report.improvements.length > 0) && (
        <div className="grid gap-4 sm:grid-cols-2">
          <section className="rounded-xl border border-slate-200 p-5 dark:border-slate-800">
            <h2 className="font-display font-semibold text-emerald-600 dark:text-emerald-400">Strengths</h2>
            <ul className="mt-3 list-disc space-y-2 pl-5 text-sm text-slate-600 dark:text-slate-300">
              {report.strengths.map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ul>
          </section>
          <section className="rounded-xl border border-slate-200 p-5 dark:border-slate-800">
            <h2 className="font-display font-semibold text-amber-600 dark:text-amber-400">Room to improve</h2>
            <ul className="mt-3 list-disc space-y-2 pl-5 text-sm text-slate-600 dark:text-slate-300">
              {report.improvements.map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ul>
          </section>
        </div>
      )}

      <section className="space-y-4">
        <h2 className="font-display text-lg font-semibold">Question by question</h2>
        {report.answers.map((a) => (
          <article key={a.questionIndex} className="rounded-xl border border-slate-200 p-5 dark:border-slate-800">
            <div className="flex items-start justify-between gap-4">
              <p className="font-medium">{a.question}</p>
              {a.score !== null && (
                <span className={`font-display text-2xl font-bold ${scoreColor(a.score)}`}>{a.score}</span>
              )}
            </div>
            <p className="mt-3 border-l-2 border-slate-200 pl-3 text-sm text-slate-500 italic dark:border-slate-700 dark:text-slate-400">
              {a.userAnswer}
            </p>
            {a.feedback && <p className="mt-3 text-sm text-slate-600 dark:text-slate-300">{a.feedback}</p>}
            {a.referenceAnswer && (
              <details className="mt-3 rounded-lg bg-slate-50 p-3 text-sm dark:bg-slate-900">
                <summary className="cursor-pointer font-medium text-accent">Reference answer</summary>
                <p className="mt-2 whitespace-pre-line text-slate-600 dark:text-slate-300">{a.referenceAnswer}</p>
                {a.keyPoints.length > 0 && (
                  <ul className="mt-2 list-disc pl-5 text-slate-500 dark:text-slate-400">
                    {a.keyPoints.map((k, i) => (
                      <li key={i}>{k}</li>
                    ))}
                  </ul>
                )}
              </details>
            )}
          </article>
        ))}
      </section>

      <Link
        to="/setup"
        className="block w-full rounded-full bg-ink py-4 text-center text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-xl dark:bg-accent dark:hover:bg-accent-strong"
      >
        Practice again
      </Link>
    </div>
  )
}
