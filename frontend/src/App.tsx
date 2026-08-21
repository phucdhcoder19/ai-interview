import { useEffect, useState } from 'react'
import { Link, Route, Routes, useLocation } from 'react-router-dom'
import LandingScreen from './screens/LandingScreen'
import SetupScreen from './screens/SetupScreen'
import InterviewScreen from './screens/InterviewScreen'
import ReportScreen from './screens/ReportScreen'

/* Light-first (Coderbyte-style): default is light unless the user chose dark. */
function useDarkMode() {
  const [dark, setDark] = useState(() => localStorage.getItem('theme-v2') === 'dark')

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
    localStorage.setItem('theme-v2', dark ? 'dark' : 'light')
  }, [dark])

  return { dark, toggle: () => setDark((d) => !d) }
}

export default function App() {
  const { dark, toggle } = useDarkMode()
  const { pathname } = useLocation()
  const onLanding = pathname === '/'

  return (
    <div className="min-h-screen">
      {/* Gradient announcement banner — Coderbyte's signature top strip */}
      {onLanding && (
        <a
          href="https://github.com/phucdhcoder19/ai-interview"
          target="_blank"
          rel="noreferrer"
          className="brand-gradient block py-2.5 text-center text-sm font-medium text-white"
        >
          Built in the open with Spring AI &amp; Gemini —{' '}
          <span className="font-bold underline underline-offset-2">see the source →</span>
        </a>
      )}

      <header className="sticky top-0 z-10 border-b border-slate-100 bg-white/90 backdrop-blur-md dark:border-slate-800 dark:bg-slate-950/90">
        <div className="mx-auto flex h-[72px] max-w-6xl items-center justify-between px-6">
          <Link to="/" className="font-display text-xl font-extrabold tracking-tight text-ink dark:text-white">
            AI<span className="text-accent"> Interview</span>
          </Link>
          <nav className="flex items-center gap-1 sm:gap-5">
            {onLanding && (
              <>
                <a
                  href="#features"
                  className="hidden text-xs font-bold tracking-[0.15em] text-ink uppercase transition-colors duration-200 hover:text-accent sm:block dark:text-slate-300"
                >
                  Features
                </a>
                <a
                  href="#how-it-works"
                  className="hidden text-xs font-bold tracking-[0.15em] text-ink uppercase transition-colors duration-200 hover:text-accent sm:block dark:text-slate-300"
                >
                  How it works
                </a>
                <Link
                  to="/setup"
                  className="hidden text-xs font-bold tracking-[0.15em] text-violet uppercase transition-colors duration-200 hover:text-accent sm:block"
                >
                  Free practice
                </Link>
              </>
            )}
            <button
              onClick={toggle}
              aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}
              className="rounded-md p-2 text-slate-400 transition-colors duration-200 hover:text-ink dark:hover:text-white"
            >
              {dark ? '☀' : '☾'}
            </button>
            <Link
              to="/setup"
              className="rounded-full bg-ink px-6 py-3 text-xs font-bold tracking-[0.15em] text-white uppercase transition-all duration-200 hover:bg-ink/90 hover:shadow-lg dark:bg-accent dark:hover:bg-accent-strong"
            >
              Start a session
            </Link>
          </nav>
        </div>
      </header>

      <Routes>
        <Route path="/" element={<LandingScreen />} />
        <Route
          path="/setup"
          element={
            <main className="mx-auto max-w-3xl px-6 py-12">
              <SetupScreen />
            </main>
          }
        />
        <Route
          path="/interview/:sessionId"
          element={
            <main className="mx-auto max-w-3xl px-6 py-12">
              <InterviewScreen />
            </main>
          }
        />
        <Route
          path="/report/:sessionId"
          element={
            <main className="mx-auto max-w-3xl px-6 py-12">
              <ReportScreen />
            </main>
          }
        />
      </Routes>

      {/* Multi-column footer, Coderbyte style: light gray, logo, link columns, social. */}
      <footer className="bg-slate-50 dark:bg-slate-900/40">
        <div className="mx-auto max-w-6xl px-6 py-16">
          <div className="grid gap-12 lg:grid-cols-[1.2fr_1fr_1fr_1fr_auto]">
            <div>
              <p className="font-display text-xl font-extrabold tracking-tight text-ink dark:text-white">
                AI<span className="text-accent"> Interview</span>
              </p>
              <p className="mt-3 max-w-xs text-sm text-slate-500 dark:text-slate-400">
                Practice technical interviews with an AI interviewer that scores like the real thing.
              </p>
            </div>

            <nav aria-label="Product">
              <p className="font-display text-lg font-bold text-ink dark:text-white">Product</p>
              <ul className="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
                <li><a href="/#features" className="transition-colors hover:text-accent">Interviews</a></li>
                <li><a href="/#features" className="transition-colors hover:text-accent">Assessments</a></li>
                <li><a href="/#features" className="transition-colors hover:text-accent">Upskilling</a></li>
                <li><a href="/#how-it-works" className="transition-colors hover:text-accent">Topics</a></li>
              </ul>
            </nav>

            <nav aria-label="Get started">
              <p className="font-display text-lg font-bold text-ink dark:text-white">Get started</p>
              <ul className="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
                <li><Link to="/setup" className="transition-colors hover:text-accent">Start a session</Link></li>
                <li><a href="/#how-it-works" className="transition-colors hover:text-accent">How it works</a></li>
                <li>
                  <a
                    href="https://github.com/phucdhcoder19/ai-interview"
                    target="_blank"
                    rel="noreferrer"
                    className="transition-colors hover:text-accent"
                  >
                    Source code
                  </a>
                </li>
              </ul>
            </nav>

            <nav aria-label="Resources">
              <p className="font-display text-lg font-bold text-ink dark:text-white">Resources</p>
              <ul className="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
                <li>
                  <a href="https://docs.spring.io/spring-ai/reference/" target="_blank" rel="noreferrer" className="transition-colors hover:text-accent">
                    Spring AI docs
                  </a>
                </li>
                <li>
                  <a href="https://ai.google.dev/" target="_blank" rel="noreferrer" className="transition-colors hover:text-accent">
                    Gemini API
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Snailclimb/interview-guide" target="_blank" rel="noreferrer" className="transition-colors hover:text-accent">
                    Reference project
                  </a>
                </li>
              </ul>
            </nav>

            <a
              href="https://github.com/phucdhcoder19/ai-interview"
              target="_blank"
              rel="noreferrer"
              aria-label="GitHub repository"
              className="text-slate-400 transition-colors hover:text-ink dark:hover:text-white"
            >
              <svg viewBox="0 0 24 24" className="h-6 w-6 fill-current" aria-hidden="true">
                <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.91.58.11.79-.25.79-.56 0-.27-.01-1.17-.02-2.12-3.2.7-3.87-1.36-3.87-1.36-.52-1.33-1.28-1.68-1.28-1.68-1.04-.71.08-.7.08-.7 1.15.08 1.76 1.19 1.76 1.19 1.03 1.76 2.69 1.25 3.35.96.1-.75.4-1.25.72-1.54-2.55-.29-5.24-1.28-5.24-5.68 0-1.26.45-2.28 1.19-3.09-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.18 1.18a11.1 11.1 0 0 1 5.8 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.81 1.19 1.83 1.19 3.09 0 4.41-2.69 5.38-5.25 5.67.41.35.77 1.05.77 2.12 0 1.53-.01 2.76-.01 3.14 0 .31.21.68.8.56A10.52 10.52 0 0 0 23.5 12C23.5 5.65 18.35.5 12 .5Z" />
              </svg>
            </a>
          </div>

          <p className="mt-12 border-t border-slate-200 pt-6 text-sm text-slate-400 dark:border-slate-800 dark:text-slate-500">
            © 2026 AI Interview — a learning project built with Spring AI &amp; Gemini.
          </p>
        </div>
      </footer>
    </div>
  )
}
