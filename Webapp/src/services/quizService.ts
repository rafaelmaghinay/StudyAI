import axios from 'axios'
import { Quiz, QuizAttempt, UserAnswer, ApiResponse, Question } from '../types'

// Use Vite env var for API base, fallback to localhost for dev
const quizEnv = (import.meta as any).env || {}
const API_BASE_URL = quizEnv.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwtToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Quiz API calls
export const quizService = {
  getAll: async (userId: string) => {
    const response = await api.get<ApiResponse<Quiz[]>>(`/quizzes/users/${userId}`)
    return response.data.data
  },

  getById: async (id: string) => {
    const response = await api.get<ApiResponse<Quiz>>(`/quizzes/${id}`)
    return response.data.data
  },

  create: async (
    noteIds: string[],
    config: {
      title: string
      description?: string
      difficulty?: 'easy' | 'medium' | 'hard'
      questionCount?: number
      subjectId?: string
    },
    userId: string
  ) => {
    const response = await api.post<ApiResponse<Quiz>>('/quizzes', {
      userId,
      noteIds,
      ...config,
    })
    return response.data.data
  },

  update: async (id: string, data: Partial<Quiz>) => {
    const response = await api.put<ApiResponse<Quiz>>(`/quizzes/${id}`, data)
    return response.data.data
  },

  delete: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/quizzes/${id}`)
    return response.data.success
  },

  getAttempts: async (userId: string) => {
    const response = await api.get<ApiResponse<QuizAttempt[]>>(`/quiz-attempts/users/${userId}`)
    return response.data.data
  },

  startAttempt: async (quizId: string) => {
    const response = await api.post<ApiResponse<QuizAttempt>>('/quiz-attempts', {
      quizId,
    })
    return response.data.data
  },

  submitAttempt: async (attemptId: string, answers: UserAnswer[]) => {
    // Transform answers to match backend format (AnswerSubmission[])
    const formattedAnswers = answers.map(answer => ({
      questionId: answer.questionId,
      userAnswer: answer.userAnswer,
      timeSpentSeconds: answer.timeSpentSeconds
    }))
    
    const response = await api.put<ApiResponse<QuizAttempt>>(
      `/quiz-attempts/${attemptId}/submit`,
      {
        answers: formattedAnswers,
      }
    )
    return response.data.data
  },

  getAttemptAnswers: async (attemptId: string) => {
    const response = await api.get<ApiResponse<UserAnswer[]>>(
      `/quiz-attempts/${attemptId}/answers`
    )
    return response.data.data
  },

  getAttemptById: async (attemptId: string) => {
    const response = await api.get<ApiResponse<QuizAttempt>>(
      `/quiz-attempts/${attemptId}`
    )
    return response.data.data
  },

  getQuestions: async (quizId: string) => {
    const response = await api.get<ApiResponse<Question[]>>(`/quizzes/${quizId}/questions`)
    return response.data.data
  },

  deleteAttempt: async (attemptId: string) => {
    const response = await api.delete<ApiResponse<void>>(
      `/quiz-attempts/${attemptId}`
    )
    return response.data.success
  },
}
