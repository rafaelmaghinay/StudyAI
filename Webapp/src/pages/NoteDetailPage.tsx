import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import { Note } from '../types'
import { noteService } from '../services/noteService'
import './NoteDetailPage.css'

export default function NoteDetailPage() {
  const { noteId } = useParams<{ noteId: string }>()
  const navigate = useNavigate()
  const [note, setNote] = useState<Note | null>(null)
  const [loading, setLoading] = useState(true)
  const [downloading, setDownloading] = useState(false)

  useEffect(() => {
    if (noteId) {
      loadNote()
    }
  }, [noteId])

  const loadNote = async () => {
    if (!noteId) return
    try {
      setLoading(true)
      const data = await noteService.getById(noteId)
      setNote(data)
    } catch (error) {
      console.error('Failed to load note:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async () => {
    if (!note || !note.s3Key) {
      console.error('Note does not have an associated file')
      return
    }

    try {
      setDownloading(true)
      const response = await noteService.getDownloadUrl(note.id)
      
      if (response && response.downloadUrl) {
        const link = document.createElement('a')
        link.href = response.downloadUrl
        link.target = '_blank'
        link.rel = 'noopener noreferrer'
        
        const filename = note.s3Key.split('/').pop() || note.title
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
      setDownloading(false)
    }
  }

  const handleDelete = async () => {
    if (!note || !confirm('Delete this note and its file?')) return
    try {
      await noteService.deleteWithFile(note.id)
      navigate('/notes')
    } catch (error) {
      console.error('Failed to delete note:', error)
    }
  }

  if (loading) {
    return <div className="loading">Loading note...</div>
  }

  if (!note) {
    return (
      <div className="not-found">
        <h2>Note not found</h2>
        <Button onClick={() => navigate('/notes')}>Back to Notes</Button>
      </div>
    )
  }

  return (
    <div className="note-detail-page">
      <div className="note-header">
        <h1>{note.title}</h1>
        <div className="note-actions">
          {note.s3Key && (
            <Button 
              variant="primary" 
              onClick={handleDownload}
              disabled={downloading}
            >
              {downloading ? '⬇️ Downloading...' : '⬇️ Download'}
            </Button>
          )}
          <Button variant="secondary" onClick={() => navigate('/notes')}>
            ← Back
          </Button>
          <Button variant="danger" onClick={handleDelete}>
            🗑️ Delete
          </Button>
        </div>
      </div>

      <Card>
        <div className="note-info">
          <div>
            <strong>File Type:</strong> {note.fileType || 'Unknown'}
          </div>
          <div>
            <strong>Size:</strong> {note.fileSizeKb || 0} KB
          </div>
          {/* Pages count not available from backend; show N/A for now */}
          <div>
            <strong>Pages:</strong> N/A
          </div>
          <div>
            <strong>Added:</strong> {new Date(note.createdAt).toLocaleDateString()}
          </div>
        </div>
      </Card>

      <Card>
        <h2>📖 Extracted Content</h2>
        <div className="note-content">
          {note.extractedText || 'No extracted content available'}
        </div>
      </Card>
    </div>
  )
}
