import { Quiz } from '../../types'
import QuizCard from './QuizCard'

interface QuizListProps {
  quizzes: Quiz[]
  onEdit?: (quiz: Quiz) => void
  onDelete?: (quiz: Quiz) => void
  onClick?: (quiz: Quiz) => void
  loading?: boolean
}

export default function QuizList({
  quizzes,
  onEdit,
  onDelete,
  onClick,
  loading = false,
}: QuizListProps) {
  if (loading) {
    return <div className="loading">Loading quizzes...</div>
  }

  if (quizzes.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">❓</div>
        <h3>No quizzes yet</h3>
        <p>Create your first quiz to get started</p>
      </div>
    )
  }

  return (
    <div className="quiz-grid">
      {quizzes.map((quiz) => (
        <QuizCard
          key={quiz.id}
          quiz={quiz}
          onEdit={onEdit}
          onDelete={onDelete}
          onClick={() => onClick?.(quiz)}
        />
      ))}
    </div>
  )
}
