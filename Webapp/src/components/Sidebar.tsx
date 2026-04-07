import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Sidebar.css'

export default function Sidebar() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path: string) => {
    return location.pathname.startsWith(path)
  }

  return (
    <nav className="sidebar-navbar">
      <div className="sidebar-container">
        {/* Logo/Branding */}
        <div className="sidebar-brand">
          <span className="brand-icon">📚</span>
          <span className="brand-text">StudyAI</span>
        </div>

        {/* Navigation Links */}
        <div className="sidebar-nav">
          <button
            onClick={() => navigate('/dashboard')}
            className={`sidebar-link ${isActive('/dashboard') ? 'active' : ''}`}
          >
            <span className="nav-icon">📊</span>
            <span className="nav-text">Dashboard</span>
          </button>

          <button
            onClick={() => navigate('/subjects')}
            className={`sidebar-link ${isActive('/subjects') ? 'active' : ''}`}
          >
            <span className="nav-icon">📚</span>
            <span className="nav-text">Subjects</span>
          </button>

          <button
            onClick={() => navigate('/quizzes')}
            className={`sidebar-link ${isActive('/quizzes') ? 'active' : ''}`}
          >
            <span className="nav-icon">✏️</span>
            <span className="nav-text">Quizzes</span>
          </button>

          <button
            onClick={() => navigate('/quiz-attempts')}
            className={`sidebar-link ${isActive('/quiz-attempts') ? 'active' : ''}`}
          >
            <span className="nav-icon">📋</span>
            <span className="nav-text">Quiz Attempts</span>
          </button>

          <button
            onClick={() => navigate('/performance')}
            className={`sidebar-link ${isActive('/performance') ? 'active' : ''}`}
          >
            <span className="nav-icon">📈</span>
            <span className="nav-text">Performance</span>
          </button>
        </div>

        {/* User Info & Logout */}
        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">👤</div>
            <div className="user-details">
              <div className="user-email">{user?.email}</div>
            </div>
          </div>
          <button className="btn-logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}
