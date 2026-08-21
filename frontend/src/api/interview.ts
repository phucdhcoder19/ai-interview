import { get, post } from './request'

/* Các kiểu dưới đây phản chiếu DTO ở modules/interview/model/dto phía backend. */

export interface Skill {
  id: string
  displayName: string
  description: string
  categories: string[]
}

export type Difficulty = 'junior' | 'mid' | 'senior'

export interface Session {
  sessionId: string
  skillId: string
  skillName: string
  difficulty: Difficulty
  totalQuestions: number
  currentQuestionIndex: number
  status: 'IN_PROGRESS' | 'COMPLETED'
}

export interface Question {
  index: number
  total: number
  question: string
  category: string
  followUps: string[]
}

export interface AnswerReport {
  questionIndex: number
  category: string
  question: string
  userAnswer: string
  score: number | null
  feedback: string | null
  keyPoints: string[]
  referenceAnswer: string | null
}

export interface Report {
  sessionId: string
  skillName: string
  difficulty: Difficulty
  evaluateStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  overallScore: number | null
  overallFeedback: string | null
  strengths: string[]
  improvements: string[]
  answers: AnswerReport[]
}

export const listSkills = () => get<Skill[]>('/api/interview/skills')

export const createSession = (skillId: string, difficulty: Difficulty, questionCount: number) =>
  post<Session>('/api/interview/sessions', { skillId, difficulty, questionCount })

export const getCurrentQuestion = (sessionId: string) =>
  get<Question>(`/api/interview/sessions/${sessionId}/question`)

export const submitAnswer = (sessionId: string, questionIndex: number, answer: string) =>
  post<Session>(`/api/interview/sessions/${sessionId}/answers`, { questionIndex, answer })

/** Kết thúc + chấm điểm cả phiên — backend chấm đồng bộ nên chờ ~20-30 giây. */
export const completeSession = (sessionId: string) =>
  post<Session>(`/api/interview/sessions/${sessionId}/complete`)

export const getReport = (sessionId: string) => get<Report>(`/api/interview/sessions/${sessionId}/report`)
