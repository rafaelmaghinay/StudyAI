import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import QuestionDisplay from '../components/quiz/QuestionDisplay'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import { QuizAttempt, UserAnswer, Question } from '../types'
import { quizService } from '../services/quizService'
import './QuizResultsPage.css'

export default function QuizResultsPage() {
  const { quizId, attemptId } = useParams<{ quizId: string; attemptId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [attempt, setAttempt] = useState<QuizAttempt | null>(null)
  const [answers, setAnswers] = useState<UserAnswer[]>([])
  const [questions, setQuestions] = useState<Question[]>([])
  const [loading, setLoading] = useState(true)
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)
  const [viewMode, setViewMode] = useState<'overview' | 'detailed'>('overview')

  useEffect(() => {
    if (attemptId && quizId) {
      loadResults()
    }
  }, [attemptId, quizId])

  const loadResults = async () => {
    if (!attemptId || !quizId) return
    try {
      setLoading(true)
      const [attemptData, answersData, questionsData] = await Promise.all([
        quizService.getAttemptById(attemptId),
        quizService.getAttemptAnswers(attemptId),
        quizService.getQuestions(quizId),
      ])
      setAttempt(attemptData)
      setAnswers(answersData || [])
      setQuestions(questionsData || [])
    } catch (error) {
      console.error('Failed to load results:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="loading">Loading results...</div>
  }

  if (!attempt) {
    return (
      <div className="not-found">
        <h2>Results not found</h2>
        <Button onClick={() => navigate('/')}>Back to Dashboard</Button>
      </div>
    )
  }

  const accuracy = ((attempt.correctAnswers / attempt.totalQuestions) * 100).toFixed(1)
  const currentQuestion = questions[currentQuestionIndex]
  const currentUserAnswer = answers.find((a) => a.questionId === currentQuestion?.id)
  const isAnswerCorrect = currentUserAnswer?.userAnswer === currentQuestion?.correctAnswer
  const getPerformanceMessage = (score: number) => {
    if (score >= 90) return "Outstanding! You've mastered this material! 🌟"
    if (score >= 80) return 'Great job! You did really well! 👏'
    if (score >= 70) return 'Good effort! Keep studying to improve. 📚'
    if (score >= 60) return 'Not bad, but there\'s room for improvement. 💪'
    return 'Keep practicing! You\'ll do better next time! 🚀'
  }

  const getPerformanceColor = (score: number) => {
    if (score >= 90) return '#34C759'
    if (score >= 80) return '#34C759'
    if (score >= 70) return '#FF9500'
    if (score >= 60) return '#FF9500'
    return '#FF3B30'
  }

  return (
    <div className="quiz-results-page">
      {/* Header Section */}
      <div className="results-header" style={{ borderLeftColor: getPerformanceColor(parseFloat(accuracy)) }}>
        <h1>🎯 Quiz Results</h1>
        <p className="performance-message">{getPerformanceMessage(parseFloat(accuracy))}</p>
      </div>

      {/* Score Card */}
      <Card className="score-card">
        <div className="score-grid">
          <div className="score-main">
            <div className="score-circle" style={{ borderColor: getPerformanceColor(parseFloat(accuracy)) }}>
              <div className="score-value">{accuracy}%</div>
            </div>
            <p className="score-detail">
              {attempt.correctAnswers} out of {attempt.totalQuestions} correct
            </p>
          </div>

          <div className="metrics-grid">
            <div className="metric-item">
              <span className="metric-icon">⏱️</span>
              <span className="metric-label">Time Spent</span>
              <span className="metric-value">
                {Math.floor((attempt.timeSpentSeconds || 0) / 60)}m {(attempt.timeSpentSeconds || 0) % 60}s
              </span>
            </div>
            <div className="metric-item">
              <span className="metric-icon">✅</span>
              <span className="metric-label">Correct</span>
              <span className="metric-value">{attempt.correctAnswers}</span>
            </div>
            <div className="metric-item">
              <span className="metric-icon">❌</span>
              <span className="metric-label">Incorrect</span>
              <span className="metric-value">{attempt.totalQuestions - attempt.correctAnswers}</span>
            </div>
            <div className="metric-item">
              <span className="metric-icon">📅</span>
              <span className="metric-label">Completed</span>
              <span className="metric-value">
                {new Date(attempt.completedAt).toLocaleDateString()}
              </span>
            </div>
          </div>
        </div>
      </Card>

      {/* View Mode Toggle */}
      <div className="view-mode-toggle">
        <button
          className={`toggle-btn ${viewMode === 'overview' ? 'active' : ''}`}
          onClick={() => setViewMode('overview')}
        >
          📊 Overview
        </button>
        <button
          className={`toggle-btn ${viewMode === 'detailed' ? 'active' : ''}`}
          onClick={() => setViewMode('detailed')}
        >
          🔍 Detailed Review
        </button>
      </div>

      {/* Overview Mode - All Questions Summary */}
      {viewMode === 'overview' && (
        <div className="overview-mode">
          <h2>📋 Question Summary</h2>
          <div className="questions-grid">
            {questions.map((question, index) => {
              const userAnswer = answers.find((a) => a.questionId === question.id)
              const isCorrect = userAnswer?.userAnswer === question.correctAnswer
              return (
                <button
                  key={question.id}
                  className={`question-summary-btn ${isCorrect ? 'correct' : 'incorrect'}`}
                  onClick={() => {
                    setCurrentQuestionIndex(index)
                    setViewMode('detailed')
                  }}
                >
                  <span className="question-number">{index + 1}</span>
                  <span className="question-status">{isCorrect ? '✓' : '✗'}</span>
                </button>
              )
            })}
          </div>
          <p className="summary-note">Click on any question to see detailed review</p>
        </div>
      )}

      {/* Detailed Mode - Individual Question Review */}
      {viewMode === 'detailed' && currentQuestion && (
        <div className="detailed-mode">
          <div className="question-progress">
            <span className="progress-text">
              Question {currentQuestionIndex + 1} of {questions.length}
            </span>
            <div className="progress-bar">
              <div
                className="progress-fill"
                style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
              ></div>
            </div>
          </div>

          <Card className="question-review-card">
            <div className={`question-status-banner ${isAnswerCorrect ? 'correct' : 'incorrect'}`}>
              <span className="status-icon">{isAnswerCorrect ? '✓' : '✗'}</span>
              <span className="status-text">
                {isAnswerCorrect ? 'Correct Answer' : 'Incorrect Answer'}
              </span>
            </div>

            <div className="question-header">
              <h3 className="question-title">{currentQuestion.questionText}</h3>
            </div>

            <QuestionDisplay
              question={currentQuestion}
              selectedAnswer={currentUserAnswer?.userAnswer}
              onAnswerSelect={() => {}}
              showCorrect={true}
            />

            {currentQuestion.explanation && (
              <Card className={`explanation-card ${isAnswerCorrect ? 'correct' : 'incorrect'}`}>
                <h4 className="explanation-title">💡 Explanation</h4>
                <p className="explanation-text">{currentQuestion.explanation}</p>
              </Card>
            )}

            {!isAnswerCorrect && currentUserAnswer && (
              <Card className="answer-comparison">
                <h4 className="comparison-title">📌 Your Answer vs Correct Answer</h4>
                <div className="comparison-grid">
                  <div className="your-answer">
                    <span className="comparison-label">Your Answer</span>
                    <span className="comparison-value incorrect">{currentUserAnswer.userAnswer}</span>
                  </div>
                  <div className="correct-answer">
                    <span className="comparison-label">Correct Answer</span>
                    <span className="comparison-value correct">{currentQuestion.correctAnswer}</span>
                  </div>
                </div>
              </Card>
            )}
          </Card>

          {/* Navigation Controls */}
          <div className="navigation-controls">
            <Button
              variant="secondary"
              onClick={() => setCurrentQuestionIndex((prev) => Math.max(0, prev - 1))}
              disabled={currentQuestionIndex === 0}
            >
              ← Previous
            </Button>
            <span className="nav-info">{currentQuestionIndex + 1} / {questions.length}</span>
            <Button
              variant="secondary"
              onClick={() => setCurrentQuestionIndex((prev) => Math.min(questions.length - 1, prev + 1))}
              disabled={currentQuestionIndex === questions.length - 1}
            >
              Next →
            </Button>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="results-actions">
        <Button onClick={() => navigate('/quizzes')}>📝 Take Another Quiz</Button>
        <Button variant="secondary" onClick={() => navigate('/')}>
          🏠 Back to Dashboard
        </Button>
      </div>
    </div>
  )
}
