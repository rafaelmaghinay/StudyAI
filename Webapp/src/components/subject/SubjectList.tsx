import { Subject } from '../../types'
import SubjectCard from './SubjectCard'

interface SubjectListProps {
  subjects: Subject[]
  onEdit?: (subject: Subject) => void
  onDelete?: (subject: Subject) => void
  onClick?: (subject: Subject) => void
  loading?: boolean
}

export default function SubjectList({
  subjects,
  onEdit,
  onDelete,
  onClick,
  loading = false,
}: SubjectListProps) {
  if (loading) {
    return <div className="loading">Loading subjects...</div>
  }

  // Filter out any undefined subjects and handle edge cases
  const validSubjects = (subjects || []).filter((subject): subject is Subject => subject !== undefined && subject !== null)

  if (validSubjects.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📚</div>
        <h3>No subjects yet</h3>
        <p>Create your first subject to get started</p>
      </div>
    )
  }

  return (
    <div className="subject-grid">
      {validSubjects.map((subject) => (
        <SubjectCard
          key={subject.id}
          subject={subject}
          onEdit={onEdit}
          onDelete={onDelete}
          onClick={onClick}
        />
      ))}
    </div>
  )
}
