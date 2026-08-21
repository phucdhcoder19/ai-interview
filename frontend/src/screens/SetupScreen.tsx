import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createSession, listSkills, type Difficulty, type Skill } from '../api/interview'
import Waiting from '../components/Waiting'

const DIFFICULTIES: { value: Difficulty; label: string; hint: string }[] = [
  { value: 'junior', label: 'Junior', hint: '0–1 yrs' },
  { value: 'mid', label: 'Middle', hint: '1–3 yrs' },
  { value: 'senior', label: 'Senior', hint: '3+ yrs' },
]

const QUESTION_COUNTS = [3, 5, 8]

export default function SetupScreen() {
  const navigate = useNavigate()
  const [skills, setSkills] = useState<Skill[]>([])
  const [skillId, setSkillId] = useState('')
  const [difficulty, setDifficulty] = useState<Difficulty>('mid')
  const [questionCount, setQuestionCount] = useState(5)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    listSkills()
      .then((list) => {
        setSkills(list)
        if (list.length > 0) setSkillId(list[0].id)
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  async function start() {
    setCreating(true)
    setError('')
    try {
      const session = await createSession(skillId, difficulty, questionCount)
      navigate(`/interview/${session.sessionId}`)
    } catch (e) {
      setError((e as Error).message)
      setCreating(false)
    }
  }

  if (creating) {
    return <Waiting title="Preparing your questions" hint="The AI is writing a question set for this session — usually takes about 30 seconds" />
  }

  const selectedSkill = skills.find((s) => s.id === skillId)

  return (
    <div className="space-y-10">
      <div>
        <h1 className="font-display text-3xl font-bold tracking-tight">Set up your session</h1>
        <p className="mt-2 text-slate-500 dark:text-slate-400">Pick a track and level — the AI plays the interviewer.</p>
      </div>

      {error && (
        <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <section aria-labelledby="skill-label">
        <h2 id="skill-label" className="mb-3 text-sm font-medium text-slate-500 dark:text-slate-400">
          Interview track
        </h2>
        <div className="grid gap-3">
          {skills.map((skill) => (
            <label
              key={skill.id}
              className={`cursor-pointer rounded-xl border p-4 transition-all duration-200 ${
                skillId === skill.id
                  ? 'border-accent bg-accent-soft/40 dark:bg-accent/10'
                  : 'border-slate-200 hover:border-slate-300 dark:border-slate-800 dark:hover:border-slate-600'
              }`}
            >
              <input
                type="radio"
                name="skill"
                value={skill.id}
                checked={skillId === skill.id}
                onChange={() => setSkillId(skill.id)}
                className="sr-only"
              />
              <span className="font-display font-semibold">{skill.displayName}</span>
              <span className="mt-1 block text-sm text-slate-500 dark:text-slate-400">{skill.description}</span>
            </label>
          ))}
        </div>
        {selectedSkill && (
          <p className="mt-2 text-xs text-slate-400 dark:text-slate-500">
            Covers: {selectedSkill.categories.join(' · ')}
          </p>
        )}
      </section>

      <section aria-labelledby="difficulty-label">
        <h2 id="difficulty-label" className="mb-3 text-sm font-medium text-slate-500 dark:text-slate-400">
          Seniority level
        </h2>
        <div className="grid grid-cols-3 gap-3">
          {DIFFICULTIES.map((d) => (
            <button
              key={d.value}
              onClick={() => setDifficulty(d.value)}
              aria-pressed={difficulty === d.value}
              className={`rounded-xl border py-3 transition-all duration-200 ${
                difficulty === d.value
                  ? 'border-accent bg-accent text-white shadow-lg shadow-accent/20'
                  : 'border-slate-200 hover:border-slate-300 dark:border-slate-800 dark:hover:border-slate-600'
              }`}
            >
              <span className="font-display font-semibold">{d.label}</span>
              <span className={`block text-xs ${difficulty === d.value ? 'text-sky-100' : 'text-slate-400'}`}>
                {d.hint}
              </span>
            </button>
          ))}
        </div>
      </section>

      <section aria-labelledby="count-label">
        <h2 id="count-label" className="mb-3 text-sm font-medium text-slate-500 dark:text-slate-400">
          Number of questions
        </h2>
        <div className="grid grid-cols-3 gap-3">
          {QUESTION_COUNTS.map((n) => (
            <button
              key={n}
              onClick={() => setQuestionCount(n)}
              aria-pressed={questionCount === n}
              className={`rounded-xl border py-3 font-display font-semibold transition-all duration-200 ${
                questionCount === n
                  ? 'border-accent bg-accent text-white shadow-lg shadow-accent/20'
                  : 'border-slate-200 hover:border-slate-300 dark:border-slate-800 dark:hover:border-slate-600'
              }`}
            >
              {n} questions
            </button>
          ))}
        </div>
      </section>

      <button
        onClick={start}
        disabled={!skillId}
        className="w-full rounded-full bg-ink py-4 text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-xl disabled:opacity-40 dark:bg-accent dark:hover:bg-accent-strong"
      >
        Start interview
      </button>
    </div>
  )
}
