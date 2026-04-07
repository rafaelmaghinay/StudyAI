import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { quizService } from '../services/quizService'
import { QuizAttempt } from '../types'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import LoadingSpinner from '../components/common/LoadingSpinner'
import './QuizAttempts.css'

export default function QuizAttemptsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [attempts, setAttempts] = useState<QuizAttempt[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (user?.id) {
      loadAttempts()
    }
  }, [user?.id])

  const loadAttempts = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      setError(null)
      const userAttempts = await quizService.getAttempts(user.id)
      // Sort by date, most recent first
      const sorted = (userAttempts || []).sort(
        (a, b) => new Date(b.completedAt).getTime() - new Date(a.completedAt).getTime()
      )
      setAttempts(sorted)
    } catch (err) {
      console.error('Error loading quiz attempts:', err)
      setError('Failed to load quiz attempts. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleViewResults = (attemptId: string, quizId: string) => {
    navigate(`/quiz/${quizId}/results/${attemptId}`)
  }

  const handleRetakeQuiz = (quizId: string) => {
    navigate(`/quiz/${quizId}/take`)
  }

  const handleDeleteAttempt = async (attemptId: string) => {
    if (!window.confirm('Are you sure you want to delete this quiz attempt? This action cannot be undone.')) {
      return
    }

    try {
      await quizService.deleteAttempt(attemptId)
      // Remove the deleted attempt from the list
      setAttempts((prev) => prev.filter((attempt) => attempt.id !== attemptId))
    } catch (err) {
      console.error('Error deleting quiz attempt:', err)
      setError('Failed to delete quiz attempt. Please try again.')
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'score-excellent'
    if (score >= 60) return 'score-good'
    if (score >= 40) return 'score-fair'
    return 'score-poor'
  }

  if (loading) {
    return <LoadingSpinner message="Loading quiz attempts..." />
  }

  return (
    <div className="attempts-container">
      <div className="attempts-header">
        <div>
          <h1>Quiz Attempts</h1>
          <p>View your quiz attempt history and results</p>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {attempts.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-content">
            <h2>No quiz attempts yet</h2>
            <p>Start taking quizzes to see your attempt history!</p>
            <Button
              onClick={() => navigate('/quizzes')}
              variant="primary"
            >
              Go to Quizzes
            </Button>
          </div>
        </Card>
      ) : (
        <div className="attempts-list">
          {attempts.map((attempt) => (
            <Card key={attempt.id} className="attempt-card">
              <div className="attempt-card-content">
                <div className="attempt-info">
                  <h3 className="attempt-title">Quiz Attempt</h3>
                  <p className="attempt-date">{formatDate(attempt.completedAt)}</p>
                  <p className="attempt-stats">
                    {attempt.correctAnswers} / {attempt.totalQuestions} questions correct
                  </p>
                </div>

                <div className={`attempt-score ${getScoreColor(attempt.score)}`}>
                  <div className="score-number">{Math.round(attempt.score)}%</div>
                  <div className="score-label">Score</div>
                </div>
              </div>

              <div className="attempt-meta">
                <span>⏱️ Time: {attempt.timeSpentSeconds ? `${Math.round(attempt.timeSpentSeconds / 60)}m` : 'N/A'}</span>
                <span>✅ Correct: {attempt.correctAnswers} / {attempt.totalQuestions}</span>
              </div>

              <div className="attempt-actions">
                <Button
                  onClick={() => handleViewResults(attempt.id, attempt.quizId)}
                  variant="primary"
                  size="sm"
                >
                  View Results
                </Button>
                <Button
                  onClick={() => handleRetakeQuiz(attempt.quizId)}
                  variant="secondary"
                  size="sm"
                >
                  Retake Quiz
                </Button>
                <Button
                  onClick={() => handleDeleteAttempt(attempt.id)}
                  variant="danger"
                  size="sm"
                >
                  Delete
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
