import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { completeSession, getCurrentQuestion, submitAnswer, type Question } from '../api/interview'
import Waiting from '../components/Waiting'

export default function InterviewScreen() {
  const { sessionId = '' } = useParams()
  const navigate = useNavigate()
  const [question, setQuestion] = useState<Question | null>(null)
  const [answer, setAnswer] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [finishing, setFinishing] = useState(false)
  const [error, setError] = useState('')

  const loadQuestion = useCallback(() => {
    getCurrentQuestion(sessionId)
      .then(setQuestion)
      .catch((e: Error) => setError(e.message))
  }, [sessionId])

  useEffect(loadQuestion, [loadQuestion])

  async function submit() {
    if (!question || !answer.trim()) return
    setSubmitting(true)
    setError('')
    try {
      const session = await submitAnswer(sessionId, question.index, answer.trim())
      setAnswer('')
      if (session.currentQuestionIndex >= session.totalQuestions) {
        // Last question — finish and grade in one go (grading is synchronous and slow).
        setFinishing(true)
        await completeSession(sessionId)
        navigate(`/report/${sessionId}`)
        return
      }
      setQuestion(null)
      loadQuestion()
    } catch (e) {
      setError((e as Error).message)
      setFinishing(false)
    } finally {
      setSubmitting(false)
    }
  }

  if (finishing) {
    return <Waiting title="Grading your session" hint="The AI is reviewing all your answers — usually takes about 30 seconds" />
  }

  if (error && !question) {
    return (
      <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
        {error}
      </p>
    )
  }

  if (!question) {
    return <Waiting title="Loading question" hint="One moment" />
  }

  const progress = ((question.index + 1) / question.total) * 100

  return (
    <div className="space-y-8">
      <div>
        <div className="flex items-baseline justify-between">
          <p className="text-sm font-medium text-slate-500 dark:text-slate-400">
            Question {question.index + 1} of {question.total}
          </p>
          <span className="rounded-full bg-accent-soft px-3 py-1 text-xs font-medium text-accent-strong dark:bg-accent/15 dark:text-sky-300">
            {question.category}
          </span>
        </div>
        <div
          role="progressbar"
          aria-valuenow={question.index + 1}
          aria-valuemin={0}
          aria-valuemax={question.total}
          className="mt-2 h-1 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800"
        >
          <div className="h-full bg-accent transition-all duration-250" style={{ width: `${progress}%` }} />
        </div>
      </div>

      <h1 className="font-display text-xl leading-relaxed font-semibold">{question.question}</h1>

      {question.followUps.length > 0 && (
        <details className="rounded-xl border border-slate-200 p-4 text-sm dark:border-slate-800">
          <summary className="cursor-pointer font-medium text-slate-500 dark:text-slate-400">
            Follow-up questions — answer them for extra depth
          </summary>
          <ul className="mt-2 list-disc space-y-1 pl-5 text-slate-600 dark:text-slate-300">
            {question.followUps.map((f, i) => (
              <li key={i}>{f}</li>
            ))}
          </ul>
        </details>
      )}

      {error && (
        <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <div className="space-y-3">
        <textarea
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          rows={8}
          placeholder="Type your answer…"
          aria-label="Your answer"
          className="w-full resize-y rounded-xl border border-slate-200 bg-white p-4 transition-colors duration-200 focus:border-accent focus:ring-2 focus:ring-accent/20 focus:outline-none dark:border-slate-800 dark:bg-slate-900"
        />
        <button
          onClick={submit}
          disabled={submitting || !answer.trim()}
          className="w-full rounded-full bg-ink py-4 text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-xl disabled:opacity-40 dark:bg-accent dark:hover:bg-accent-strong"
        >
          {submitting
            ? 'Submitting…'
            : question.index + 1 === question.total
              ? 'Submit & finish session'
              : 'Submit & next question'}
        </button>
      </div>
    </div>
  )
}
