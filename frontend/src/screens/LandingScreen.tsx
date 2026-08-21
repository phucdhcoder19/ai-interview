import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Reveal, RevealGroup, fadeUp } from '../components/motion'

/* Coderbyte-style pill CTA pair used across sections. */
function CtaPair({ secondary }: { secondary: string }) {
  return (
    <div className="mt-8 flex flex-wrap items-center gap-5">
      <Link
        to="/setup"
        className="rounded-full bg-ink px-8 py-4 text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-xl dark:bg-accent dark:hover:bg-accent-strong"
      >
        Start a session →
      </Link>
      <Link
        to="/setup"
        className="text-xs font-bold tracking-[0.15em] text-violet uppercase transition-colors duration-200 hover:text-accent"
      >
        {secondary}
      </Link>
    </div>
  )
}

/* Colored chips like Coderbyte's skill pills — each topic gets a brand-ish icon color. */
type Topic = { label: string; color: string; glyph: string }

const TOPICS_ROW_1: Topic[] = [
  { label: 'HashMap internals', color: '#f97316', glyph: '{ }' },
  { label: 'ThreadPoolExecutor', color: '#ef4444', glyph: '≡' },
  { label: 'ConcurrentHashMap', color: '#f59e0b', glyph: '⇄' },
  { label: 'JVM memory', color: '#8b5cf6', glyph: '▣' },
  { label: 'Garbage collection', color: '#10b981', glyph: '♻' },
  { label: 'Bean lifecycle', color: '#22c55e', glyph: '❀' },
  { label: 'Spring IoC', color: '#16a34a', glyph: '↺' },
  { label: 'AOP proxies', color: '#06b6d4', glyph: '◎' },
  { label: 'Virtual threads', color: '#3b82f6', glyph: '∿' },
  { label: 'Class loading', color: '#ec4899', glyph: '▲' },
]
const TOPICS_ROW_2: Topic[] = [
  { label: '@Transactional', color: '#22c55e', glyph: '✓' },
  { label: 'Propagation levels', color: '#14b8a6', glyph: '⇶' },
  { label: 'MySQL indexes', color: '#0ea5e9', glyph: '⌕' },
  { label: 'B+Tree', color: '#6366f1', glyph: 'ᛉ' },
  { label: 'MVCC', color: '#a855f7', glyph: '⧉' },
  { label: 'Deadlocks', color: '#ef4444', glyph: '⊗' },
  { label: 'Covering indexes', color: '#f59e0b', glyph: '▤' },
  { label: 'Slow queries', color: '#f97316', glyph: '◔' },
  { label: 'Auto-configuration', color: '#10b981', glyph: '⚙' },
  { label: 'Connection pools', color: '#3b82f6', glyph: '≋' },
]

function TopicMarquee({ topics, reverse }: { topics: Topic[]; reverse?: boolean }) {
  const row = [...topics, ...topics]
  return (
    <div className="overflow-hidden" aria-hidden="true">
      <motion.div
        className="flex w-max gap-4 py-2.5"
        animate={{ x: reverse ? ['-50%', '0%'] : ['0%', '-50%'] }}
        transition={{ repeat: Infinity, ease: 'linear', duration: 36 }}
      >
        {row.map((t, i) => (
          <span
            key={i}
            className="flex items-center gap-2.5 rounded-full bg-white py-3 pr-6 pl-3 text-sm font-bold whitespace-nowrap text-ink shadow-md shadow-slate-900/5 dark:bg-slate-900 dark:text-slate-200"
          >
            <span
              className="flex h-7 w-7 items-center justify-center rounded-full text-sm text-white"
              style={{ backgroundColor: t.color }}
            >
              {t.glyph}
            </span>
            {t.label}
          </span>
        ))}
      </motion.div>
    </div>
  )
}

/* Track cards with colored diamond icons — Coderbyte's role card grid. */
const TRACKS = [
  { name: 'Java Backend', color: '#10b981', glyph: '</>', ready: true },
  { name: 'Frontend', color: '#3b82f6', glyph: '▤', ready: false },
  { name: 'System design', color: '#f97316', glyph: '⧉', ready: false },
]

export default function LandingScreen() {
  return (
    <>
      {/* ===== Hero — centered, Coderbyte style ===== */}
      <section className="mx-auto max-w-4xl px-6 pt-20 pb-12 text-center">
        <motion.div variants={fadeUp} initial="hidden" animate="show">
          <p className="text-xs font-bold tracking-[0.2em] text-ink uppercase dark:text-slate-300">
            AI interview practice for{' '}
            <span className="rounded-md bg-accent-soft px-2 py-1 text-accent dark:bg-accent/15">Java Backend</span>
          </p>
          <h1 className="mt-6 font-display text-5xl leading-[1.05] font-extrabold tracking-tight text-ink sm:text-7xl dark:text-white">
            Practice <span className="text-gradient">any interview</span> quickly, honestly, and for free
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-lg text-slate-500 dark:text-slate-400">
            Interview, assess, and upskill with an AI interviewer —{' '}
            <strong className="font-bold text-ink dark:text-white">unlimited</strong> sessions, instant written
            feedback.
          </p>
          <div className="mt-9 flex flex-wrap items-center justify-center gap-5">
            <Link
              to="/setup"
              className="rounded-full bg-ink px-9 py-4 text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-xl dark:bg-accent dark:hover:bg-accent-strong"
            >
              Start practicing
            </Link>
            <a
              href="#how-it-works"
              className="text-xs font-bold tracking-[0.15em] text-violet uppercase transition-colors duration-200 hover:text-accent"
            >
              Or see how it works →
            </a>
          </div>
        </motion.div>

        {/* App-window mockup under hero */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2, ease: 'easeOut' }}
          className="mx-auto mt-14 max-w-3xl overflow-hidden rounded-2xl border border-slate-200 bg-white text-left shadow-2xl shadow-slate-900/10 dark:border-slate-800 dark:bg-slate-900"
        >
          <div className="flex items-center gap-5 border-b border-slate-100 px-6 py-3 text-xs font-medium text-slate-400 dark:border-slate-800">
            <span className="font-display font-extrabold text-ink dark:text-white">AI</span>
            <span className="font-semibold text-ink dark:text-white">Session</span>
            <span>Reports</span>
            <span>Tracks</span>
          </div>
          <div className="p-6">
            <div className="flex items-center justify-between">
              <h3 className="font-display text-xl font-bold text-ink dark:text-white">Java Backend — Middle</h3>
              <span className="rounded-full bg-accent-soft px-3 py-1 text-xs font-bold text-accent dark:bg-accent/15">
                Question 2 of 5
              </span>
            </div>
            <p className="mt-3 text-slate-600 dark:text-slate-300">
              A public method without <code className="font-semibold text-accent">@Transactional</code> calls a{' '}
              <code className="font-semibold text-accent">REQUIRES_NEW</code> method in the same class. Does a new
              transaction start? Why?
            </p>
            <div className="mt-5 grid gap-4 border-t border-slate-100 pt-5 sm:grid-cols-[1fr_auto] dark:border-slate-800">
              <div className="space-y-2.5">
                {[
                  { label: 'Accuracy', pct: 90 },
                  { label: 'Depth', pct: 72 },
                  { label: 'Clarity', pct: 85 },
                ].map((row, i) => (
                  <div key={row.label} className="flex items-center gap-3">
                    <span className="w-20 text-xs font-semibold text-slate-500 dark:text-slate-400">{row.label}</span>
                    <div className="h-2 flex-1 overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                      <motion.div
                        className="brand-gradient h-full rounded-full"
                        initial={{ width: 0 }}
                        animate={{ width: `${row.pct}%` }}
                        transition={{ duration: 0.9, delay: 0.7 + i * 0.15, ease: 'easeOut' }}
                      />
                    </div>
                    <span className="w-8 text-right text-xs font-bold text-ink dark:text-white">{row.pct}</span>
                  </div>
                ))}
              </div>
              <div className="flex items-center justify-center">
                <div className="flex h-20 w-20 items-center justify-center rounded-full border-4 border-accent font-display text-2xl font-extrabold text-ink dark:text-white">
                  8/10
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </section>

      {/* ===== Trust strip ===== */}
      <Reveal>
        <div className="mx-auto max-w-5xl px-6 py-10 text-center">
          <p className="text-xs font-bold tracking-[0.2em] text-slate-400 uppercase">
            Built with production-grade tools
          </p>
          <div className="mt-6 flex flex-wrap items-center justify-center gap-x-12 gap-y-4 font-display text-lg font-bold text-slate-400 dark:text-slate-500">
            <span>Spring AI</span>
            <span>Gemini</span>
            <span>PostgreSQL</span>
            <span>pgvector</span>
            <span>React</span>
          </div>
        </div>
      </Reveal>

      {/* ===== Gradient mega headline ===== */}
      <Reveal className="mx-auto max-w-4xl px-6 py-20 text-center">
        <h2 className="font-display text-4xl leading-tight font-extrabold tracking-tight text-ink sm:text-5xl dark:text-white">
          All-in-one <span className="text-gradient">interview practice</span> with feedback you can't get from a
          study guide.
        </h2>
      </Reveal>

      {/* ===== Alternating feature sections ===== */}
      <div id="features" className="scroll-mt-24">
        {/* 1. Interviews — text left, visual right */}
        <section className="mx-auto grid max-w-6xl items-center gap-12 px-6 py-16 lg:grid-cols-2">
          <Reveal>
            <h3 className="font-display text-4xl font-extrabold text-ink sm:text-5xl dark:text-white">Interviews</h3>
            <p className="mt-3 text-lg font-bold text-slate-700 dark:text-slate-300">
              Real questions with real follow-ups
            </p>
            <ul className="mt-6 space-y-3 text-slate-600 dark:text-slate-400">
              <li className="flex gap-3"><span className="text-accent">●</span> Question sets generated per session from a curated knowledge outline</li>
              <li className="flex gap-3"><span className="text-accent">●</span> Allocated across Java core, Spring, MySQL and project experience</li>
              <li className="flex gap-3"><span className="text-accent">●</span> Follow-ups that push past memorized definitions into real understanding</li>
            </ul>
            <CtaPair secondary="Preview an interview" />
          </Reveal>
          <Reveal>
            <div className="space-y-3 rounded-2xl bg-slate-50 p-6 dark:bg-slate-900/50">
              {['Explain how ThreadPoolExecutor handles 120 concurrent tasks…', 'Why did ConcurrentHashMap drop segment locks in Java 8?', 'Your @Transactional method silently loses its transaction. Why?'].map(
                (q, i) => (
                  <div
                    key={i}
                    className="rounded-xl border border-slate-200 bg-white p-4 text-sm font-medium text-ink shadow-sm dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200"
                  >
                    <span className="mr-2 rounded bg-accent-soft px-1.5 py-0.5 text-xs font-bold text-accent dark:bg-accent/15">
                      {['JAVA', 'JAVA', 'SPRING'][i]}
                    </span>
                    {q}
                  </div>
                ),
              )}
            </div>
          </Reveal>
        </section>

        {/* 2. Assessments — visual left, text right */}
        <section className="bg-slate-50 dark:bg-slate-900/40">
          <div className="mx-auto grid max-w-6xl items-center gap-12 px-6 py-16 lg:grid-cols-2">
            <Reveal className="order-2 lg:order-1">
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xl shadow-slate-900/5 dark:border-slate-700 dark:bg-slate-900">
                <div className="flex items-center justify-between">
                  <p className="font-display font-bold text-ink dark:text-white">Session report</p>
                  <span className="text-xs font-semibold text-slate-400">Java Backend · Senior</span>
                </div>
                <div className="mt-4 space-y-3">
                  {[
                    { label: 'Accuracy', w: '40%', note: 'weight 40%' },
                    { label: 'Depth', w: '25%', note: 'weight 25%' },
                    { label: 'Completeness', w: '20%', note: 'weight 20%' },
                    { label: 'Clarity', w: '15%', note: 'weight 15%' },
                  ].map((r) => (
                    <div key={r.label} className="flex items-center gap-3 text-sm">
                      <span className="w-28 font-semibold text-ink dark:text-slate-200">{r.label}</span>
                      <div className="h-2 flex-1 rounded-full bg-slate-100 dark:bg-slate-800">
                        <div className="brand-gradient h-full rounded-full" style={{ width: r.w }} />
                      </div>
                      <span className="w-20 text-right text-xs text-slate-400">{r.note}</span>
                    </div>
                  ))}
                </div>
                <div className="mt-5 flex items-center gap-3 border-t border-slate-100 pt-4 dark:border-slate-800">
                  <span className="rounded-md bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-600 dark:bg-emerald-950 dark:text-emerald-400">
                    PASS
                  </span>
                  <span className="rounded-md bg-red-50 px-2.5 py-1 text-xs font-bold text-red-500 dark:bg-red-950 dark:text-red-400">
                    GAP: self-invocation
                  </span>
                </div>
              </div>
            </Reveal>
            <Reveal className="order-1 lg:order-2">
              <h3 className="font-display text-4xl font-extrabold text-ink sm:text-5xl dark:text-white">Assessments</h3>
              <p className="mt-3 text-lg font-bold text-slate-700 dark:text-slate-300">
                Graded like a real interviewer would
              </p>
              <ul className="mt-6 space-y-3 text-slate-600 dark:text-slate-400">
                <li className="flex gap-3"><span className="text-accent">●</span> Accuracy 40% · depth 25% · completeness 20% · clarity 15%</li>
                <li className="flex gap-3"><span className="text-accent">●</span> Written feedback that names exactly what was missing or wrong</li>
                <li className="flex gap-3"><span className="text-accent">●</span> Pass thresholds mapped to junior, middle and senior expectations</li>
              </ul>
              <CtaPair secondary="See a sample report" />
            </Reveal>
          </div>
        </section>

        {/* 3. Upskilling — text left, visual right */}
        <section className="mx-auto grid max-w-6xl items-center gap-12 px-6 py-16 lg:grid-cols-2">
          <Reveal>
            <h3 className="font-display text-4xl font-extrabold text-ink sm:text-5xl dark:text-white">Upskilling</h3>
            <p className="mt-3 text-lg font-bold text-slate-700 dark:text-slate-300">
              Every session doubles as a study guide
            </p>
            <ul className="mt-6 space-y-3 text-slate-600 dark:text-slate-400">
              <li className="flex gap-3"><span className="text-accent">●</span> A detailed reference answer and key points for every question</li>
              <li className="flex gap-3"><span className="text-accent">●</span> Strengths and gaps summarized across the whole session</li>
              <li className="flex gap-3"><span className="text-accent">●</span> Topic fingerprints guarantee your next session never repeats a question</li>
            </ul>
            <CtaPair secondary="Read a reference answer" />
          </Reveal>
          <Reveal>
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xl shadow-slate-900/5 dark:border-slate-700 dark:bg-slate-900">
              <p className="text-xs font-bold tracking-widest text-violet uppercase">Reference answer</p>
              <p className="mt-3 text-sm leading-relaxed text-slate-600 dark:text-slate-300">
                No — Spring transactions rely on proxies. A call from inside the same class bypasses the proxy, so{' '}
                <code className="font-semibold text-accent">REQUIRES_NEW</code> never triggers…
              </p>
              <div className="mt-4 space-y-1.5 border-t border-slate-100 pt-4 dark:border-slate-800">
                {['Proxy-based AOP mechanics', 'Self-invocation pitfall', 'Fix: split the bean or inject itself'].map(
                  (k) => (
                    <p key={k} className="flex gap-2 text-sm font-medium text-ink dark:text-slate-200">
                      <span className="text-emerald-500">✓</span> {k}
                    </p>
                  ),
                )}
              </div>
            </div>
          </Reveal>
        </section>
      </div>

      {/* ===== Stats — gradient numbers ===== */}
      <section className="bg-slate-50 dark:bg-slate-900/40">
        <RevealGroup className="mx-auto grid max-w-5xl gap-10 px-6 py-16 text-center sm:grid-cols-3">
          {[
            { value: '4', label: 'knowledge areas per track' },
            { value: '3–20', label: 'questions per session' },
            { value: '0', label: 'repeated questions, ever' },
          ].map((s) => (
            <motion.div key={s.label} variants={fadeUp}>
              <p className="text-gradient font-display text-6xl font-extrabold">{s.value}</p>
              <p className="mt-2 font-semibold text-ink dark:text-slate-300">{s.label}</p>
            </motion.div>
          ))}
        </RevealGroup>
      </section>

      {/* ===== Topic chips marquee — on gray like Coderbyte's skills band ===== */}
      <section id="how-it-works" className="scroll-mt-24 bg-slate-50 py-20 dark:bg-slate-900/40">
        <Reveal className="mx-auto max-w-3xl px-6 text-center">
          <h2 className="font-display text-4xl leading-tight font-extrabold tracking-tight text-ink sm:text-5xl dark:text-white">
            Real-world topics, <span className="text-gradient">not trivia</span>.
          </h2>
          <p className="mx-auto mt-4 max-w-xl text-slate-500 dark:text-slate-400">
            Questions come from a curated outline of what interviewers actually ask — and every answer gets scored.
          </p>
        </Reveal>
        <div className="relative mt-10">
          <TopicMarquee topics={TOPICS_ROW_1} />
          <TopicMarquee topics={TOPICS_ROW_2} reverse />
          <div className="absolute inset-0 flex items-center justify-center">
            <Link
              to="/setup"
              className="rounded-full bg-ink px-8 py-4 text-xs font-bold tracking-[0.15em] text-white uppercase shadow-xl shadow-slate-900/20 transition-all duration-200 hover:bg-ink/90 hover:scale-105 dark:bg-accent"
            >
              See all topics →
            </Link>
          </div>
        </div>
      </section>

      {/* ===== Track cards — colored diamond icons like Coderbyte's role grid ===== */}
      <section className="mx-auto max-w-6xl px-6 py-20">
        <Reveal className="text-center">
          <h2 className="font-display text-4xl leading-tight font-extrabold tracking-tight text-ink sm:text-5xl dark:text-white">
            Pick a track. <span className="text-gradient">More on the way.</span>
          </h2>
        </Reveal>
        <RevealGroup className="mt-12 grid gap-6 sm:grid-cols-3">
          {TRACKS.map((t) => (
            <motion.article
              key={t.name}
              variants={fadeUp}
              whileHover={t.ready ? { y: -6 } : undefined}
              className={`rounded-2xl bg-white p-8 shadow-lg shadow-slate-900/5 dark:bg-slate-900 ${
                t.ready ? '' : 'opacity-70'
              }`}
            >
              <span
                className="flex h-16 w-16 rotate-45 items-center justify-center rounded-2xl"
                style={{ backgroundColor: t.color }}
              >
                <span className="-rotate-45 font-display text-lg font-extrabold text-white">{t.glyph}</span>
              </span>
              <h3 className="mt-6 font-display text-2xl font-extrabold text-ink dark:text-white">{t.name}</h3>
              {t.ready ? (
                <div className="mt-5 space-y-2">
                  <Link to="/setup" className="block text-sm font-bold text-ink transition-colors hover:text-accent dark:text-slate-200">
                    Preview interview <span aria-hidden="true">→</span>
                  </Link>
                  <Link to="/setup" className="block text-sm font-bold text-ink transition-colors hover:text-accent dark:text-slate-200">
                    Start a session <span aria-hidden="true">→</span>
                  </Link>
                </div>
              ) : (
                <p className="mt-5 inline-block rounded-full bg-slate-100 px-3 py-1 text-xs font-bold tracking-widest text-slate-400 uppercase dark:bg-slate-800">
                  Coming soon
                </p>
              )}
            </motion.article>
          ))}
        </RevealGroup>
      </section>

      {/* ===== Final CTA ===== */}
      <section className="bg-ink dark:bg-slate-900">
        <Reveal className="mx-auto max-w-4xl px-6 py-20 text-center">
          <h2 className="font-display text-4xl font-extrabold tracking-tight text-white">
            Your next interview starts here
          </h2>
          <p className="mx-auto mt-4 max-w-md text-slate-300">
            Five questions. Honest scores. A clear picture of what to study next.
          </p>
          <Link
            to="/setup"
            className="brand-gradient mt-9 inline-block rounded-full px-10 py-4 text-xs font-bold tracking-[0.15em] text-white uppercase shadow-lg shadow-black/30 transition-transform duration-200 hover:scale-105"
          >
            Start a free session →
          </Link>
        </Reveal>
      </section>
    </>
  )
}
