import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import LoadingSpinner from '../components/common/LoadingSpinner'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import './NotesPage.css'
import { Note } from '../types'
import { noteService } from '../services/noteService'

export default function NotesPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [notes, setNotes] = useState<Note[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [downloading, setDownloading] = useState<string | null>(null)

  useEffect(() => {
    if (user?.id) {
      loadNotes()
    }
  }, [user?.id])

  const loadNotes = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      setError(null)
      const data = await noteService.getByUser(user.id)
      setNotes(data || [])
    } catch (error) {
      console.error('Failed to load notes:', error)
      setError('Failed to load notes. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleDownloadNote = async (note: Note) => {
    if (!note.s3Key) {
      console.error('Note does not have an associated file')
      return
    }

    try {
      setDownloading(note.id)
      const response = await noteService.getDownloadUrl(note.id)
      
      if (response && response.downloadUrl) {
        const link = document.createElement('a')
        link.href = response.downloadUrl
        link.target = '_blank'
        link.rel = 'noopener noreferrer'
        
        const filename = note.s3Key?.split('/').pop() || note.title
        link.download = filename
        
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        console.error('Could not get download URL')
      }
    } catch (error) {
      console.error('Failed to download note:', error)
    } finally {
      setDownloading(null)
    }
  }

  const handleDeleteNote = async (note: Note) => {
    if (!confirm('Delete this note?')) return
    try {
      await noteService.delete(note.id)
      setNotes(notes.filter((n) => n.id !== note.id))
    } catch (error) {
      console.error('Failed to delete note:', error)
      setError('Failed to delete note. Please try again.')
    }
  }

  if (loading) {
    return <LoadingSpinner message="Loading notes..." />
  }

  return (
    <div className="notes-container">
      <div className="notes-header">
        <div>
          <h1>All Notes</h1>
          <p>View and manage all your uploaded notes</p>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {notes.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-content">
            <h2>No notes yet</h2>
            <p>Upload your first note from a subject to get started!</p>
          </div>
        </Card>
      ) : (
        <div className="notes-grid">
          {notes.map((note) => (
            <Card key={note.id} className="note-card">
              <div className="note-card-header">
                <h3>📄 {note.title}</h3>
                <div className="note-actions">
                  {note.s3Key && (
                    <button
                      className="action-btn download"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleDownloadNote(note)
                      }}
                      title="Download"
                      disabled={downloading === note.id}
                    >
                      {downloading === note.id ? '⏳' : '⬇️'}
                    </button>
                  )}
                  <button
                    className="action-btn delete"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleDeleteNote(note)
                    }}
                    title="Delete"
                  >
                    🗑️
                  </button>
                </div>
              </div>

              <div className="note-meta">
                <span> {note.fileSizeKb || 0} KB</span>
                <span>📅 {new Date(note.createdAt).toLocaleDateString()}</span>
              </div>

              <div className="note-actions-footer">
                <Button
                  onClick={() => navigate(`/notes/${note.id}`)}
                  variant="primary"
                  size="sm"
                >
                  View Note
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
