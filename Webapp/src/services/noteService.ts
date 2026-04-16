import axios from 'axios'
import { Note, ApiResponse } from '../types'

// Use Vite env var for API base, fallback to localhost for dev
const noteEnv = (import.meta as any).env || {}
const API_BASE_URL = noteEnv.VITE_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add auth token to requests and handle FormData
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwtToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  
  // Don't set Content-Type for FormData requests - let browser/axios handle it
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  
  return config
})

// Note API calls
export const noteService = {
  getBySubject: async (subjectId: string) => {
    const response = await api.get<ApiResponse<Note[]>>(`/notes/subjects/${subjectId}`)
    return response.data.data
  },

  getByUser: async (userId: string) => {
    const response = await api.get<ApiResponse<Note[]>>(`/notes/users/${userId}`)
    return response.data.data
  },

  getById: async (id: string) => {
    const response = await api.get<ApiResponse<Note>>(`/notes/${id}`)
    return response.data.data
  },

  upload: async (data: FormData) => {
    // Don't set Content-Type header - let axios/browser handle it automatically
    // The browser will set the correct boundary
    const response = await api.post<ApiResponse<Note>>('/notes/upload', data)
    return response.data.data
  },

  update: async (id: string, data: Partial<Omit<Note, 'id' | 'userId' | 'createdAt'>>) => {
    const response = await api.put<ApiResponse<Note>>(`/notes/${id}`, data)
    return response.data.data
  },

  delete: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/notes/${id}`)
    return response.data.success
  },

  getStatus: async (noteId: string) => {
    const response = await api.get<ApiResponse<Note>>(`/notes/${noteId}/status`)
    return response.data.data
  },

  getDownloadUrl: async (noteId: string) => {
    const response = await api.get<ApiResponse<any>>(`/notes/${noteId}/download-url`)
    return response.data.data
  },

  deleteWithFile: async (id: string) => {
    const response = await api.delete<ApiResponse<void>>(`/notes/${id}/delete-with-file`)
    return response.data.success
  },
}
