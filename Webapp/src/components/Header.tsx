import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Header.css'

export default function Header() {
  const navigate = useNavigate()
  const { user, logout, isAuthenticated } = useAuth()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="header-logo">
          <span className="logo-icon">📚</span>
          <span className="logo-text">StudyAI</span>
        </Link>
        <nav className="header-nav">
          <Link to="/" className="nav-link">Dashboard</Link>
          <Link to="/create-quiz" className="nav-link primary">
            New Quiz
          </Link>
        </nav>
        <div className="header-user">
          <span className="user-name">{user?.name}</span>
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </div>
    </header>
  )
}
