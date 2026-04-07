import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { quizService } from '../services/quizService'
import { Quiz } from '../types'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import LoadingSpinner from '../components/common/LoadingSpinner'
import './Quizzes.css'

export default function QuizzesPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [quizzes, setQuizzes] = useState<Quiz[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (user?.id) {
      loadQuizzes()
    }
  }, [user?.id])

  const loadQuizzes = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      setError(null)
      const userQuizzes = await quizService.getAll(user.id)
      setQuizzes(userQuizzes || [])
    } catch (err) {
      console.error('Error loading quizzes:', err)
      setError('Failed to load quizzes. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleTakeQuiz = (quizId: string) => {
    navigate(`/quiz/${quizId}/take`)
  }

  const handleCreateQuiz = () => {
    navigate('/create-quiz')
  }

  const handleDeleteQuiz = async (quizId: string) => {
    if (window.confirm('Are you sure you want to delete this quiz?')) {
      try {
        await quizService.delete(quizId)
        setQuizzes(quizzes.filter(q => q.id !== quizId))
      } catch (err) {
        console.error('Error deleting quiz:', err)
        setError('Failed to delete quiz. Please try again.')
      }
    }
  }

  if (loading) {
    return <LoadingSpinner message="Loading quizzes..." />
  }

  return (
    <div className="quizzes-container">
      <div className="quizzes-header">
        <div>
          <h1>My Quizzes</h1>
          <p>View and take your generated quizzes</p>
        </div>
        <Button onClick={handleCreateQuiz} variant="primary">
          ✏️ Create New Quiz
        </Button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {quizzes.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-content">
            <h2>No quizzes yet</h2>
            <p>Create your first quiz from your notes to get started!</p>
            <Button onClick={handleCreateQuiz} variant="primary">
              Create Quiz
            </Button>
          </div>
        </Card>
      ) : (
        <div className="quizzes-grid">
          {quizzes.map((quiz) => (
            <Card key={quiz.id} className="quiz-card">
              <div className="quiz-card-header">
                <h3>{quiz.title}</h3>
                <span className="quiz-badge">{quiz.totalQuestions} Q</span>
              </div>
              
              {quiz.description && (
                <p className="quiz-description">{quiz.description}</p>
              )}

              <div className="quiz-meta">
                <span>📅 {new Date(quiz.createdAt).toLocaleDateString()}</span>
                <span>❓ {quiz.totalQuestions} questions</span>
              </div>

              <div className="quiz-actions">
                <Button 
                  onClick={() => handleTakeQuiz(quiz.id)} 
                  variant="primary"
                  size="sm"
                >
                  Take Quiz
                </Button>
                <Button 
                  onClick={() => handleDeleteQuiz(quiz.id)} 
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
