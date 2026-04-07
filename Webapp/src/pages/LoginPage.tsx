import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Login.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, signup } = useAuth()

  const [isSigningUp, setIsSigningUp] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError(null)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.email || !formData.password) {
      setError('Please fill in all fields')
      return
    }

    if (isSigningUp) {
      if (!formData.name) {
        setError('Please enter your name')
        return
      }
      if (formData.password !== formData.confirmPassword) {
        setError('Passwords do not match')
        return
      }
      if (formData.password.length < 6) {
        setError('Password must be at least 6 characters')
        return
      }
    }

    try {
      setLoading(true)
      setError(null)

      if (isSigningUp) {
        await signup(formData.name, formData.email, formData.password)
      } else {
        await login(formData.email, formData.password)
      }

      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const toggleMode = () => {
    setIsSigningUp(!isSigningUp)
    setError(null)
    setFormData({ name: '', email: '', password: '', confirmPassword: '' })
  }

  return (
    <div className="login-wrapper">
      <div className="login-container">
        <div className="login-header">
          <span className="login-logo-icon">📚</span>
          <h1>StudyAI</h1>
          <p>Master Your Learning</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          {error && <div className="login-error">{error}</div>}

          {isSigningUp && (
            <div className="form-group">
              <label htmlFor="name">Full Name</label>
              <input
                type="text"
                id="name"
                name="name"
                placeholder="John Doe"
                value={formData.name}
                onChange={handleChange}
                required={isSigningUp}
              />
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              placeholder="you@example.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          {isSigningUp && (
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                placeholder="••••••••"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          )}

          <button type="submit" disabled={loading} className="login-button">
            {loading ? (
              <>
                <span className="spinner"></span>
                {isSigningUp ? 'Creating Account...' : 'Logging in...'}
              </>
            ) : isSigningUp ? (
              'Sign Up'
            ) : (
              'Login'
            )}
          </button>
        </form>

        <div className="login-footer">
          <p>
            {isSigningUp ? 'Already have an account?' : "Don't have an account?"}
            <button
              type="button"
              onClick={toggleMode}
              className="toggle-button"
              disabled={loading}
            >
              {isSigningUp ? 'Login' : 'Sign Up'}
            </button>
          </p>
        </div>

        <div className="demo-notice">
          <p>💡 Demo Mode: Use any email and password to get started</p>
        </div>
      </div>
    </div>
  )
}
