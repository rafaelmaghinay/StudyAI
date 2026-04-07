import { Note } from '../../types'
import Card from '../common/Card'

interface NoteCardProps {
  note: Note
  onEdit?: (note: Note) => void
  onDelete?: (note: Note) => void
  onView?: (note: Note) => void
  onDownload?: (note: Note) => void
  onClick?: () => void
}

export default function NoteCard({
  note,
  onEdit,
  onDelete,
  onView,
  onDownload,
  onClick,
}: NoteCardProps) {
  return (
    <Card
      hoverable
      className="note-card"
      onClick={onClick}
    >
      <div className="note-header">
        <h3>📄 {note.title}</h3>
        <div className="note-actions">
          {onDownload && note.s3Key && (
            <button
              className="action-btn download"
              onClick={(e) => {
                e.stopPropagation()
                onDownload(note)
              }}
              title="Download"
            >
              ⬇️
            </button>
          )}
          {onView && (
            <button
              className="action-btn"
              onClick={(e) => {
                e.stopPropagation()
                onView(note)
              }}
              title="View"
            >
              👁️
            </button>
          )}
          {onEdit && (
            <button
              className="action-btn"
              onClick={(e) => {
                e.stopPropagation()
                onEdit(note)
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
                onDelete(note)
              }}
              title="Delete"
            >
              🗑️
            </button>
          )}
        </div>
      </div>

      <div className="note-details">
        <span>{note.fileSizeKb || 0} KB</span>
      </div>

      <p className="note-date">
        {new Date(note.createdAt).toLocaleDateString()}
      </p>
    </Card>
  )
}
