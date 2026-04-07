import axios from 'axios'

// Access Vite env in a type-safe way even if vite/client types aren't loaded
const apiEnv = (import.meta as any).env || {}
const API_BASE_URL = apiEnv.VITE_API_URL || 'http://localhost:8080/api'
console.log('API_BASE_URL (frontend):', API_BASE_URL)

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor to add JWT token to all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // Don't set Content-Type for FormData requests - let browser/axios handle it
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor to handle 401 responses (unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear auth data and redirect to login
      localStorage.removeItem('jwtToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Types
interface Subject {
  id: string
  userId: string
  name: string
  description: string
  createdAt: string
}

interface Quiz {
  id: string
  userId: string
  subjectId: string
  title: string
  description: string
  totalQuestions: number
  createdAt: string
  questions: Question[]
}

interface Question {
  id: string
  quizId: string
  text: string
  options: string[]
  correctAnswer: string
  explanation: string
  order: number
}

interface QuizAttempt {
  id: string
  userId: string
  quizId: string
  score: number
  answers: Record<string, string>
  completedAt: string
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

// Subjects API
export const getSubjects = async (userId: string) => {
  const response = await api.get<ApiResponse<Subject[]>>(`/subjects/user/${userId}`)
  return response.data.data
}

// Quizzes API
export const createQuiz = async (quizData: Omit<Quiz, 'id' | 'createdAt' | 'totalQuestions'> & { questions?: any[] }) => {
  const response = await api.post<ApiResponse<Quiz>>('/quizzes', quizData)
  return response.data.data
}

export const getQuiz = async (id: string) => {
  const response = await api.get<ApiResponse<Quiz>>(`/quizzes/${id}`)
  return response.data.data
}

export const getQuizzes = async (userId?: string) => {
  if (userId) {
    const response = await api.get<ApiResponse<Quiz[]>>(`/quizzes/user/${userId}`)
    return response.data.data
  }
  const response = await api.get<ApiResponse<Quiz[]>>('/quizzes')
  return response.data.data
}

export const deleteQuiz = async (id: string) => {
  const response = await api.delete<ApiResponse<void>>(`/quizzes/${id}`)
  return response.data.success
}

// Quiz Attempts API
export const getUserQuizAttempts = async (userId: string) => {
  const response = await api.get<ApiResponse<QuizAttempt[]>>(`/quiz-attempts/user/${userId}`)
  return response.data.data
}
