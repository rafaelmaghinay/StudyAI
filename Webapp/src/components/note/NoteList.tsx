import { Note } from '../../types'
import NoteCard from './NoteCard'
import LoadingSpinner from '../common/LoadingSpinner'

interface NoteListProps {
  notes: Note[]
  onEdit?: (note: Note) => void
  onDelete?: (note: Note) => void
  onView?: (note: Note) => void
  onDownload?: (note: Note) => void
  loading?: boolean
}

export default function NoteList({
  notes,
  onEdit,
  onDelete,
  onView,
  onDownload,
  loading = false,
}: NoteListProps) {
  if (loading) {
    return <LoadingSpinner message="Loading notes..." />
  }

  if (notes.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📝</div>
        <h3>No notes yet</h3>
        <p>Upload your first note to get started</p>
      </div>
    )
  }

  return (
    <div className="note-grid">
      {notes.map((note) => (
        <NoteCard
          key={note.id}
          note={note}
          onEdit={onEdit}
          onDelete={onDelete}
          onView={onView}
          onDownload={onDownload}
        />
      ))}
    </div>
  )
}
