// Top-level application shell and route configuration for StudyAI.
// Controls layout (sidebar/header) and public vs protected routes.

import './App.css'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'

// Pages
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import SubjectsPage from './pages/SubjectsPage'
import SubjectDetailPage from './pages/SubjectDetailPage'
import NotesPage from './pages/NotesPage'
import NoteDetailPage from './pages/NoteDetailPage'
import CreateQuizPage from './pages/CreateQuizPage'
import QuizzesPage from './pages/QuizzesPage'
import QuizAttemptsPage from './pages/QuizAttemptsPage'
import QuizPage from './pages/QuizPage'
import QuizResultsPage from './pages/QuizResultsPage'
import PerformancePage from './pages/PerformancePage'
import NotFoundPage from './pages/NotFoundPage'

// Components
import Sidebar from './components/Sidebar'
import Header from './components/Header'
import ProtectedRoute from './components/ProtectedRoute'
import ErrorBoundary from './components/common/ErrorBoundary'

export default function App() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    )
  }

  return (
    <ErrorBoundary>
      <div className="app">
        {isAuthenticated && <Sidebar />}
        {isAuthenticated && <Header />}
        <main className={isAuthenticated ? 'main-content' : ''}>
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />

            {/* Protected routes */}
            <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            
            {/* Subject routes */}
            <Route path="/subjects" element={<ProtectedRoute><SubjectsPage /></ProtectedRoute>} />
            <Route path="/subjects/:subjectId" element={<ProtectedRoute><SubjectDetailPage /></ProtectedRoute>} />
            
            {/* Note routes */}
            <Route path="/notes" element={<ProtectedRoute><NotesPage /></ProtectedRoute>} />
            <Route path="/notes/:noteId" element={<ProtectedRoute><NoteDetailPage /></ProtectedRoute>} />
            
            {/* Quiz routes */}
            <Route path="/quizzes" element={<ProtectedRoute><QuizzesPage /></ProtectedRoute>} />
            <Route path="/create-quiz" element={<ProtectedRoute><CreateQuizPage /></ProtectedRoute>} />
            <Route path="/quiz-attempts" element={<ProtectedRoute><QuizAttemptsPage /></ProtectedRoute>} />
            <Route path="/quiz/:quizId/take" element={<ProtectedRoute><QuizPage /></ProtectedRoute>} />
            <Route path="/quiz/:quizId/results/:attemptId" element={<ProtectedRoute><QuizResultsPage /></ProtectedRoute>} />
            
            {/* Performance/Analytics */}
            <Route path="/performance" element={<ProtectedRoute><PerformancePage /></ProtectedRoute>} />
            
            {/* 404 Not Found */}
            <Route path="/404" element={<NotFoundPage />} />
            <Route path="*" element={<Navigate to={isAuthenticated ? '/404' : '/login'} replace />} />
          </Routes>
        </main>
      </div>
    </ErrorBoundary>
  )
}
