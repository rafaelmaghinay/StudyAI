import axios from 'axios'
import { Subject, ApiResponse } from '../types'

// Use Vite env var for API base, fallback to localhost for dev
const subjectEnv = (import.meta as any).env || {}
const API_BASE_URL = subjectEnv.VITE_API_URL || 'http://localhost:8080/api'

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

// Subject API calls
export const subjectService = {
  getAll: async (userId: string) => {
    try {
      const response = await api.get<ApiResponse<Subject[]>>(`/subjects/users/${userId}`)
      const subjects = response.data?.data
      
      // Ensure we always return an array
      if (!Array.isArray(subjects)) {
        console.warn('API returned invalid subjects format:', response.data)
        return []
      }
      
      return subjects
    } catch (error) {
      console.error('Failed to fetch subjects:', error)
      throw error
    }
  },

  getById: async (id: string) => {
    try {
      const response = await api.get<ApiResponse<Subject>>(`/subjects/${id}`)
      return response.data.data
    } catch (error) {
      console.error('Failed to fetch subject:', error)
      throw error
    }
  },

  create: async (data: Omit<Subject, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      // Validate required fields
      if (!data.userId) {
        throw new Error('User ID is required')
      }
      if (!data.name || data.name.trim() === '') {
        throw new Error('Subject name is required')
      }

      // Prepare payload - only send required fields
      const payload = {
        userId: data.userId,
        name: data.name.trim(),
        description: data.description || '',
      }

      const response = await api.post<ApiResponse<Subject>>('/subjects', payload)
      return response.data.data
    } catch (error) {
      console.error('Failed to create subject:', error)
      throw error
    }
  },

  update: async (
    id: string,
    data: Partial<Omit<Subject, 'id' | 'userId' | 'createdAt' | 'updatedAt'>>
  ) => {
    try {
      const response = await api.put<ApiResponse<Subject>>(`/subjects/${id}`, data)
      return response.data.data
    } catch (error) {
      console.error('Failed to update subject:', error)
      throw error
    }
  },

  delete: async (id: string) => {
    try {
      const response = await api.delete<ApiResponse<void>>(`/subjects/${id}`)
      return response.data.success
    } catch (error) {
      console.error('Failed to delete subject:', error)
      throw error
    }
  },
}
