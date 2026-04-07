import { Subject } from '../../types'
import Card from '../common/Card'

interface SubjectCardProps {
  subject: Subject
  onEdit?: (subject: Subject) => void
  onDelete?: (subject: Subject) => void
  onClick?: (subject: Subject) => void
}

export default function SubjectCard({
  subject,
  onEdit,
  onDelete,
  onClick,
}: SubjectCardProps) {
  return (
    <Card
      hoverable
      className="subject-card"
      onClick={() => onClick?.(subject)}
      style={{ cursor: 'pointer' }}
    >
      <div className="subject-card-header">
        <h3>{subject.name}</h3>
        <div className="subject-actions">
          {onEdit && (
            <button
              className="action-btn"
              onClick={(e) => {
                e.stopPropagation()
                onEdit(subject)
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
                onDelete(subject)
              }}
              title="Delete"
            >
              🗑️
            </button>
          )}
        </div>
      </div>

      {subject.description && (
        <p className="subject-description">{subject.description}</p>
      )}

      <div className="subject-stats">
        <span>📝 {subject.notesCount || 0} notes</span>
        <span>❓ {subject.quizzesCount || 0} quizzes</span>
      </div>
    </Card>
  )
}
