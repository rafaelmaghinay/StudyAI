export interface User {
  id: string
  email: string
  displayName?: string
  createdAt: string
}

export interface Subject {
  id: string
  userId: string
  name: string
  description?: string
  colorTag?: string
  icon?: string
  createdAt: string
  updatedAt: string
  notesCount?: number
  quizzesCount?: number
}

export interface Note {
  id: string
  userId: string
  subjectId: string
  title: string
  s3Key?: string
  extractedTextS3Key?: string
  fileType?: string
  extractedText?: string
  fileSizeKb?: number
  fileSize?: number
  createdAt: string
  updatedAt: string
  downloadUrl?: string
  quizzes?: Quiz[]
}

export interface Quiz {
  id: string
  userId: string
  title: string
  description?: string
  totalQuestions: number
  difficultyLevel?: 'easy' | 'medium' | 'hard'
  createdAt: string
  updatedAt: string
  questions?: Question[]
  notes?: Note[]
}

export interface Question {
  id: string
  quizId: string
  questionText: string
  questionType: 'multiple_choice' | 'true_false' | 'short_answer'
  options?: string[]
  correctAnswer: string
  explanation?: string
  orderIndex: number
  createdAt: string
}

export interface QuizAttempt {
  id: string
  userId: string
  quizId: string
  score: number
  totalQuestions: number
  correctAnswers: number
  startedAt: string
  completedAt: string
  timeSpentSeconds?: number
  createdAt: string
}

export interface UserAnswer {
  id: string
  quizAttemptId: string
  questionId: string
  userAnswer: string
  isCorrect: boolean
  timeSpentSeconds?: number
  answeredAt: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}
