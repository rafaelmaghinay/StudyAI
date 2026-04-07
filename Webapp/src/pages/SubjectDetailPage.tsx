import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import NoteUploadForm from '../components/note/NoteUploadForm'
import Modal from '../components/common/Modal'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { Subject, Note, Quiz } from '../types'
import { subjectService } from '../services/subjectService'
import { noteService } from '../services/noteService'
import { quizService } from '../services/quizService'
import './SubjectDetailPage.css'

export default function SubjectDetailPage() {
  const { subjectId } = useParams<{ subjectId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [subject, setSubject] = useState<Subject | null>(null)
  const [notes, setNotes] = useState<Note[]>([])
  const [quizzes, setQuizzes] = useState<Quiz[]>([])
  const [loading, setLoading] = useState(true)
  const [downloading, setDownloading] = useState<string | null>(null)
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false)
  const [isUploading, setIsUploading] = useState(false)

  useEffect(() => {
    if (subjectId) {
      loadData()
    }
  }, [subjectId])

  const loadData = async () => {
    if (!subjectId) return
    try {
      setLoading(true)
      const [subjectData, notesData, quizzesData] = await Promise.all([
        subjectService.getById(subjectId),
        noteService.getBySubject(subjectId),
        quizService.getAll(user?.id || ''),
      ])
      setSubject(subjectData)
      setNotes(notesData || [])
      setQuizzes((quizzesData || []).filter(q => q.notes?.some(n => notesData?.some(nd => nd.id === n.id))))
    } catch (error) {
      console.error('Failed to load data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleUploadNote = async (data: {
    file: File
    title: string
    subjectId: string
  }) => {
    try {
      setIsUploading(true)
      const formData = new FormData()
      formData.append('file', data.file)
      formData.append('noteTitle', data.title)
      formData.append('subjectId', data.subjectId)
      formData.append('userId', user?.id || '')

      const newNote = await noteService.upload(formData)
      setNotes([...notes, newNote])
      setIsUploadModalOpen(false)
    } catch (error) {
      console.error('Failed to upload note:', error)
    } finally {
      setIsUploading(false)
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
    }
  }

  if (loading) {
    return <LoadingSpinner message="Loading subject..." />
  }

  if (!subject) {
    return (
      <div className="not-found">
        <h2>Subject not found</h2>
        <Button onClick={() => navigate('/subjects')}>Back to Subjects</Button>
      </div>
    )
  }

  return (
    <div className="subject-detail-container">
      <div className="subject-header">
        <div>
          <h1>{subject.name}</h1>
          {subject.description && <p>{subject.description}</p>}
        </div>
        <div className="header-actions">
          <Button onClick={() => setIsUploadModalOpen(true)} variant="primary">
            📥 Upload Note
          </Button>
          <Button variant="secondary" onClick={() => navigate(`/create-quiz?subjectId=${subjectId}`)}>
            ✨ Create Quiz
          </Button>
        </div>
      </div>

      {/* Notes Section */}
      <div className="detail-section">
        <div className="section-header">
          <h2>📝 Notes ({notes.length})</h2>
        </div>

        {notes.length === 0 ? (
          <Card className="empty-state">
            <div className="empty-state-content">
              <h3>No notes yet</h3>
              <p>Upload your first note for this subject</p>
              <Button onClick={() => setIsUploadModalOpen(true)} variant="primary">
                Upload Note
              </Button>
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

      {/* Quizzes Section */}
      {quizzes.length > 0 && (
        <div className="detail-section">
          <div className="section-header">
            <h2>❓ Quizzes ({quizzes.length})</h2>
          </div>

          <div className="quizzes-grid">
            {quizzes.map((quiz) => (
              <Card key={quiz.id} className="quiz-card">
                <div className="quiz-card-header">
                  <h3>{quiz.title}</h3>
                  <span className="quiz-badge">{quiz.totalQuestions} Q</span>
                </div>
                
                {quiz.description && (
                  <p className="quiz-description">{quiz.description}</p>
                )}

                <div className="quiz-meta">
                  <span>📅 {new Date(quiz.createdAt).toLocaleDateString()}</span>
                  <span>❓ {quiz.totalQuestions} questions</span>
                </div>

                <div className="quiz-actions">
                  <Button 
                    onClick={() => navigate(`/quiz/${quiz.id}/take`)} 
                    variant="primary"
                    size="sm"
                  >
                    Take Quiz
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      <Modal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        title="Upload New Note"
      >
        <NoteUploadForm
          subjectId={subjectId || ''}
          onSubmit={handleUploadNote}
          loading={isUploading}
        />
      </Modal>
    </div>
  )
}
