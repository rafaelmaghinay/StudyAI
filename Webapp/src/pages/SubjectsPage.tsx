import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import SubjectForm from '../components/subject/SubjectForm'
import Modal from '../components/common/Modal'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { Subject } from '../types'
import { subjectService } from '../services/subjectService'
import './SubjectsPage.css'

export default function SubjectsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    if (user?.id) {
      loadSubjects()
    }
  }, [user?.id])

  const loadSubjects = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      setError(null)
      const data = await subjectService.getAll(user.id)
      setSubjects(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Failed to load subjects:', error)
      setError('Failed to load subjects. Please try again.')
      setSubjects([])
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async (data: any) => {
    if (!user?.id) {
      console.error('User ID is not available')
      return
    }
    try {
      setIsSubmitting(true)
      const newSubject = await subjectService.create({
        userId: user.id,
        ...data,
      })
      setSubjects([...subjects, newSubject])
      setIsCreateModalOpen(false)
    } catch (error: any) {
      console.error('Failed to create subject:', error)
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to create subject. Please try again.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleUpdate = async (data: any) => {
    if (!selectedSubject) return
    try {
      setIsSubmitting(true)
      const updated = await subjectService.update(selectedSubject.id, data)
      setSubjects(subjects.map((s) => (s.id === updated.id ? updated : s)))
      setIsEditModalOpen(false)
      setSelectedSubject(null)
    } catch (error: any) {
      console.error('Failed to update subject:', error)
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to update subject. Please try again.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async (subject: Subject) => {
    if (!confirm('Are you sure you want to delete this subject?')) return
    try {
      await subjectService.delete(subject.id)
      setSubjects(subjects.filter((s) => s.id !== subject.id))
    } catch (error: any) {
      console.error('Failed to delete subject:', error)
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to delete subject. Please try again.'
      alert(errorMessage)
    }
  }

  const handleSubjectClick = (subject: Subject) => {
    navigate(`/subjects/${subject.id}`)
  }

  if (loading) {
    return <LoadingSpinner message="Loading subjects..." />
  }

  return (
    <div className="subjects-container">
      <div className="subjects-header">
        <div>
          <h1>My Subjects</h1>
          <p>Create and manage your study subjects</p>
        </div>
        <Button onClick={() => setIsCreateModalOpen(true)} variant="primary">
          📚 Create New Subject
        </Button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {subjects.length === 0 ? (
        <Card className="empty-state">
          <div className="empty-state-content">
            <h2>No subjects yet</h2>
            <p>Create your first subject to organize your notes and quizzes</p>
            <Button onClick={() => setIsCreateModalOpen(true)} variant="primary">
              Create Subject
            </Button>
          </div>
        </Card>
      ) : (
        <div className="subjects-grid">
          {subjects.map((subject) => (
            <Card 
              key={subject.id} 
              className="subject-card"
              onClick={() => handleSubjectClick(subject)}
              style={{ cursor: 'pointer' }}
            >
              <div className="subject-card-header">
                <h3>{subject.name}</h3>
                <div className="subject-actions">
                  <button
                    className="action-btn"
                    onClick={(e) => {
                      e.stopPropagation()
                      setSelectedSubject(subject)
                      setIsEditModalOpen(true)
                    }}
                    title="Edit"
                  >
                    ✏️
                  </button>
                  <button
                    className="action-btn delete"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleDelete(subject)
                    }}
                    title="Delete"
                  >
                    🗑️
                  </button>
                </div>
              </div>

              {subject.description && (
                <p className="subject-description">{subject.description}</p>
              )}

              <div className="subject-meta">
                <span>📝 {subject.notesCount || 0} notes</span>
                <span>❓ {subject.quizzesCount || 0} quizzes</span>
              </div>

              <div className="subject-actions-footer">
                <Button
                  onClick={() => handleSubjectClick(subject)}
                  variant="primary"
                  size="sm"
                >
                  View Subject
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create New Subject"
      >
        <SubjectForm onSubmit={handleCreate} loading={isSubmitting} />
      </Modal>

      <Modal
        isOpen={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false)
          setSelectedSubject(null)
        }}
        title="Edit Subject"
      >
        {selectedSubject && (
          <SubjectForm
            subject={selectedSubject}
            onSubmit={handleUpdate}
            loading={isSubmitting}
          />
        )}
      </Modal>
    </div>
  )
}
