import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { quizService } from '../services/quizService'
import { Quiz, UserAnswer } from '../types'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import QuestionDisplay from '../components/quiz/QuestionDisplay'
import './Quiz.css'

export default function QuizPage() {
  const { quizId } = useParams<{ quizId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()

  const [quiz, setQuiz] = useState<Quiz | null>(null)
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)
  const [answers, setAnswers] = useState<UserAnswer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [timeStarted, setTimeStarted] = useState<Date | null>(null)
  const [timerSeconds, setTimerSeconds] = useState(0)
  const [attemptId, setAttemptId] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const attemptStartedRef = useRef(false)

  useEffect(() => {
    if (quizId && user?.id && !attemptStartedRef.current) {
      attemptStartedRef.current = true
      loadQuiz()
      startAttempt()
    }
  }, [quizId, user?.id])

  // Timer effect
  useEffect(() => {
    const interval = setInterval(() => {
      setTimerSeconds((prev) => prev + 1)
    }, 1000)
    return () => clearInterval(interval)
  }, [])

  const loadQuiz = async () => {
    if (!quizId) return
    try {
      setLoading(true)
      const quizData = await quizService.getById(quizId)
      setQuiz(quizData)
    } catch (err) {
      setError('Failed to load quiz')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const startAttempt = async () => {
    if (!quizId || !user?.id) return
    try {
      const attempt = await quizService.startAttempt(quizId)
      setAttemptId(attempt.id)
      setTimeStarted(new Date())
    } catch (err) {
      console.error('Failed to start attempt:', err)
    }
  }

  const handleAnswerSelect = (questionId: string, answer: string) => {
    setAnswers((prev) => {
      const existing = prev.find((a) => a.questionId === questionId)
      if (existing) {
        return prev.map((a) =>
          a.questionId === questionId ? { ...a, userAnswer: answer } : a
        )
      }
      return [...prev, { 
        questionId, 
        userAnswer: answer, 
        answeredAt: new Date().toISOString(),
        timeSpentSeconds: timerSeconds
      } as any]
    })
  }

  const handleNext = () => {
    if (quiz && currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex((prev) => prev + 1)
    }
  }

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex((prev) => prev - 1)
    }
  }

  const handleJumpToQuestion = (index: number) => {
    setCurrentQuestionIndex(index)
  }

  const handleSubmitQuiz = async () => {
    if (!attemptId || !user?.id || isSubmitting) {
      alert('Failed to submit quiz')
      return
    }

    // Prevent double submission
    setIsSubmitting(true)

    try {
      // Submit the quiz attempt
      await quizService.submitAttempt(attemptId, answers)
      // Navigate to quiz attempts page
      navigate('/quiz-attempts')
    } catch (err) {
      setError('Failed to submit quiz')
      console.error(err)
      setIsSubmitting(false)
    }
  }

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    return `${hours > 0 ? hours + 'h ' : ''}${minutes}m ${secs}s`
  }

  const totalQuestions = quiz?.questions?.length ?? 0
  const answeredCount = answers.length
  const allAnswered = totalQuestions > 0 && answeredCount === totalQuestions
  const isLastQuestion = totalQuestions > 0 && currentQuestionIndex === totalQuestions - 1

  if (loading && !quiz) {
    return (
      <div className="quiz-loading">
        <div className="spinner"></div>
        <p>Loading quiz...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="quiz-error">
        <Card>
          <h2>Error Loading Quiz</h2>
          <p>{error}</p>
          <Button onClick={() => navigate('/quizzes')}>
            ← Back to Quizzes
          </Button>
        </Card>
      </div>
    )
  }

  if (!quiz || !quiz.questions || quiz.questions.length === 0) {
    return (
      <div className="quiz-error">
        <Card>
          <h2>No Questions Found</h2>
          <p>This quiz doesn't have any questions yet. Please make sure questions were generated during quiz creation.</p>
          <Button onClick={() => navigate('/quizzes')}>
            ← Back to Quizzes
          </Button>
        </Card>
      </div>
    )
  }

  const currentQuestion = quiz.questions[currentQuestionIndex]
  const currentAnswer = answers.find(
    (a) => a.questionId === currentQuestion.id
  )

  return (
    <div className="quiz-page">
      {/* Quiz Header */}
      <div className="quiz-header">
        <div className="quiz-title-section">
          <h1>{quiz.title}</h1>
          <p>{quiz.description}</p>
        </div>
        <div className="quiz-stats">
          <div className="stat-item">
            <span className="stat-label">Timer</span>
            <span className="stat-value">⏱️ {formatTime(timerSeconds)}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Progress</span>
            <span className="stat-value">
              {currentQuestionIndex + 1} / {quiz.questions.length}
            </span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Answered</span>
            <span className="stat-value">
              {answeredCount} / {quiz.questions.length}
            </span>
          </div>
        </div>
      </div>

      {/* Progress Bar */}
      <div className="progress-container">
        <div className="progress-bar">
          <div
            className="progress-fill"
            style={{
              width: `${((currentQuestionIndex + 1) / quiz.questions.length) * 100}%`,
            }}
          ></div>
        </div>
      </div>

      <div className="quiz-content">
        {/* Main Content */}
        <div className="question-section">
          <Card>
            <div className="question-header">
              <h2>Question {currentQuestionIndex + 1}</h2>
              <span className="question-type">({currentQuestion.questionType})</span>
            </div>

            <QuestionDisplay
              question={currentQuestion}
              selectedAnswer={currentAnswer?.userAnswer}
              onAnswerSelect={(answer) =>
                handleAnswerSelect(currentQuestion.id, answer)
              }
            />

            {/* Navigation */}
            <div className="question-navigation">
              <Button
                variant="secondary"
                onClick={handlePrevious}
                disabled={currentQuestionIndex === 0}
              >
                ← Previous
              </Button>

              {isLastQuestion ? (
                <Button
                  onClick={handleSubmitQuiz}
                  disabled={!allAnswered || isSubmitting}
                  className={allAnswered ? 'btn-submit' : ''}
                >
                  {isSubmitting ? '⏳ Submitting...' : `✓ Submit Quiz ${!allAnswered && `(${answeredCount}/${quiz.questions.length})`}`}
                </Button>
              ) : (
                <Button onClick={handleNext}>
                  Next →
                </Button>
              )}
            </div>
          </Card>
        </div>

        {/* Sidebar */}
        <aside className="quiz-sidebar">
          {/* Question Navigator */}
          <Card>
            <h3>📋 Questions</h3>
            <div className="question-navigator">
              {quiz.questions.map((q, index) => {
                const isAnswered = answers.some((a) => a.questionId === q.id)
                const isCurrent = index === currentQuestionIndex
                return (
                  <button
                    key={q.id}
                    className={`nav-dot ${isCurrent ? 'current' : ''} ${
                      isAnswered ? 'answered' : ''
                    }`}
                    onClick={() => handleJumpToQuestion(index)}
                    title={`Question ${index + 1}${isAnswered ? ' (Answered)' : ''}`}
                  >
                    {index + 1}
                  </button>
                )
              })}
            </div>
          </Card>

          {/* Quiz Info */}
          <Card>
            <h3>ℹ️ Info</h3>
            <div className="quiz-info">
              <div className="info-item">
                <span className="label">Total Questions</span>
                <span className="value">{quiz.questions.length}</span>
              </div>
              <div className="info-item">
                <span className="label">Difficulty</span>
                <span className="value">Medium</span>
              </div>
              <div className="info-item">
                <span className="label">Answered</span>
                <span className="value">
                  {answeredCount} / {quiz.questions.length}
                </span>
              </div>
            </div>
          </Card>

          {/* Unanswered Warning */}
          {!allAnswered && (
            <Card className="warning-card">
              <h3>⚠️ Unanswered Questions</h3>
              <p>
                You have {quiz.questions.length - answeredCount} unanswered questions.
                Answer all questions before submitting.
              </p>
            </Card>
          )}
        </aside>
      </div>
    </div>
  )
}
