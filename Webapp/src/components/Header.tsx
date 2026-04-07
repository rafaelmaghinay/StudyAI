import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Header.css'

export default function Header() {
  const navigate = useNavigate()
  const { user, logout, isAuthenticated } = useAuth()
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    setIsMobileMenuOpen(false)
    navigate('/login')
  }

  const handleNavigate = (path: string) => {
    setIsMobileMenuOpen(false)
    navigate(path)
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="header-logo" onClick={() => setIsMobileMenuOpen(false)}>
          <span className="logo-icon">📚</span>
          <span className="logo-text">StudyAI</span>
        </Link>
        <nav className="header-nav">
          <Link to="/dashboard" className="nav-link">Dashboard</Link>
          <Link to="/subjects" className="nav-link">Subjects</Link>
          <Link to="/notes" className="nav-link">Notes</Link>
          <Link to="/quizzes" className="nav-link">Quizzes</Link>
          <Link to="/quiz-attempts" className="nav-link">Attempts</Link>
          <Link to="/performance" className="nav-link">Performance</Link>
          <Link to="/create-quiz" className="nav-link primary">
            New Quiz
          </Link>
        </nav>
        <button
          type="button"
          className="header-menu-toggle"
          onClick={() => setIsMobileMenuOpen((prev) => !prev)}
        >
          &#9776;
        </button>
        <div className="header-user">
          <span className="user-name">{user?.name}</span>
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </div>
      {isMobileMenuOpen && (
        <div className="header-mobile-menu">
          <button className="mobile-menu-item" onClick={() => handleNavigate('/dashboard')}>
            Dashboard
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/subjects')}>
            Subjects
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/notes')}>
            Notes
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/quizzes')}>
            Quizzes
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/quiz-attempts')}>
            Quiz Attempts
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/performance')}>
            Performance
          </button>
          <button className="mobile-menu-item" onClick={() => handleNavigate('/create-quiz')}>
            New Quiz
          </button>
          <button className="mobile-menu-item logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      )}
    </header>
  )
}
