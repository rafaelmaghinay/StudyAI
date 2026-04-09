// Authentication context for the StudyAI frontend.
// Centralizes login/signup/logout flows and exposes user state to the app.

import React, { createContext, useContext, useState, useEffect } from 'react'

// Access Vite env safely (works in dev and build)
const authEnv = (import.meta as any).env || {}
const AUTH_API_BASE_URL = authEnv.VITE_API_URL || 'http://localhost:8080/api'

interface User {
  id: string
  email: string
  name: string
}

interface AuthContextType {
  user: User | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // Check if user is logged in on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser))
      } catch (error) {
        console.error('Failed to parse stored user', error)
        localStorage.removeItem('user')
      }
    }
    setIsLoading(false)
  }, [])

  const login = async (email: string, password: string) => {
    try {
      setIsLoading(true)
      
      const response = await fetch(`${AUTH_API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      const data = await response.json()

      if (!response.ok) {
        // Handle different error codes
        if (response.status === 401) {
          throw new Error('Invalid email or password. Please try again.')
        } else if (response.status === 400) {
          throw new Error(data.message || 'Invalid login information.')
        } else {
          throw new Error(data.message || 'Login failed.')
        }
      }

      // Extract from ApiResponse wrapper - data.data contains the actual response
      const authData = data.data
      const jwtToken = authData.jwtToken
      const userId = authData.userId
      const displayName = authData.displayName || email.split('@')[0]

      if (!jwtToken || !userId) {
        console.error('Invalid response:', data)
        throw new Error('Invalid response: missing token or user ID')
      }

      // Store JWT token
      localStorage.setItem('jwtToken', jwtToken)
      
      const user: User = {
        id: userId,
        email: authData.email || email,
        name: displayName,
      }
      
      setUser(user)
      localStorage.setItem('user', JSON.stringify(user))
    } catch (error) {
      console.error('Login error:', error)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const signup = async (name: string, email: string, password: string) => {
    try {
      setIsLoading(true)

      const response = await fetch(`${AUTH_API_BASE_URL}/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, displayName: name }),
      })

      const data = await response.json()

      if (!response.ok) {
        // Handle different error codes
        if (response.status === 409) {
          throw new Error('This email is already registered. Please log in instead.')
        } else if (response.status === 400) {
          throw new Error(data.message || 'Invalid signup information. Please check your input.')
        } else {
          throw new Error(data.message || 'Signup failed.')
        }
      }

      // Extract from ApiResponse wrapper - data.data contains the actual response
      const authData = data.data
      const jwtToken = authData.jwtToken
      const userId = authData.userId
      const displayName = authData.displayName || name

      if (!jwtToken || !userId) {
        console.error('Invalid response:', data)
        throw new Error('Invalid response: missing token or user ID')
      }

      // Store JWT token
      localStorage.setItem('jwtToken', jwtToken)
      
      const newUser: User = {
        id: userId,
        email: authData.email || email,
        name: displayName,
      }
      
      setUser(newUser)
      localStorage.setItem('user', JSON.stringify(newUser))
    } catch (error) {
      console.error('Signup error:', error)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        login,
        signup,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
