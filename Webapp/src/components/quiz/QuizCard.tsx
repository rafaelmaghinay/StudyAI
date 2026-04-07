import { Quiz } from '../../types'
import Card from '../common/Card'

interface QuizCardProps {
  quiz: Quiz
  onEdit?: (quiz: Quiz) => void
  onDelete?: (quiz: Quiz) => void
  onClick?: () => void
}

export default function QuizCard({
  quiz,
  onEdit,
  onDelete,
  onClick,
}: QuizCardProps) {
  return (
    <Card hoverable className="quiz-card" onClick={onClick}>
      <div className="quiz-header">
        <h3>❓ {quiz.title}</h3>
        <div className="quiz-actions">
          {onEdit && (
            <button
              className="action-btn"
              onClick={(e) => {
                e.stopPropagation()
                onEdit(quiz)
              }}
              title="Edit"
            >
              ✎
            </button>
          )}
          {onDelete && (
            <button
              className="action-btn delete"
              onClick={(e) => {
                e.stopPropagation()
                onDelete(quiz)
              }}
              title="Delete"
            >
              🗑️
            </button>
          )}
        </div>
      </div>

      <p className="quiz-description">{quiz.description || 'No description'}</p>

      <div className="quiz-stats">
        <span>❓ {quiz.totalQuestions} questions</span>
        {quiz.difficultyLevel && <span>⚡ {quiz.difficultyLevel}</span>}
      </div>

      <p className="quiz-date">
        {new Date(quiz.createdAt).toLocaleDateString()}
      </p>
    </Card>
  )
}
