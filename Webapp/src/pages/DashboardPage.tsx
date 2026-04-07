import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import Card from '../components/common/Card'
import Button from '../components/common/Button'
import QuizList from '../components/quiz/QuizList'
import SubjectList from '../components/subject/SubjectList'
import { Quiz, Subject, Note } from '../types'
import { quizService } from '../services/quizService'
import { subjectService } from '../services/subjectService'
import { noteService } from '../services/noteService'
import './DashboardPage.css'

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [recentQuizzes, setRecentQuizzes] = useState<Quiz[]>([])
  const [notes, setNotes] = useState<Note[]>([])
  const [stats, setStats] = useState({
    totalSubjects: 0,
    totalNotes: 0,
    totalQuizzes: 0,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (user?.id) {
      loadDashboardData()
      localStorage.setItem('userId', user.id)
    }
  }, [user?.id])

  const loadDashboardData = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      const [subjectsData, quizzesData, notesData] = await Promise.all([
        subjectService.getAll(user.id),
        quizService.getAll(user.id),
        noteService.getByUser(user.id),
      ])

      setSubjects((subjectsData || []).slice(0, 3))
      setRecentQuizzes((quizzesData || []).slice(0, 6))
      setNotes(notesData || [])
      setStats({
        totalSubjects: subjectsData?.length || 0,
        totalNotes: notesData?.length || 0,
        totalQuizzes: quizzesData?.length || 0,
      })
    } catch (error) {
      console.error('Failed to load dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="dashboard">
      <section className="dashboard-header">
        <div>
          <h1>Welcome back, {user?.name || 'Learner'}! 👋</h1>
          <p>You have {stats.totalQuizzes} quizzes to explore</p>
        </div>
        <Button onClick={() => navigate('/subjects')}>
          ➕ Create New Subject
        </Button>
      </section>

      <div className="dashboard-stats">
        <Card>
          <h3>Subjects</h3>
          <p className="stat-value">📚 {stats.totalSubjects}</p>
        </Card>
        <Card>
          <h3>Notes</h3>
          <p className="stat-value">📝 {stats.totalNotes}</p>
        </Card>
        <Card>
          <h3>Quizzes</h3>
          <p className="stat-value">❓ {stats.totalQuizzes}</p>
        </Card>
      </div>

      {loading ? (
        <div className="loading">Loading dashboard...</div>
      ) : (
        <>
          <Card>
            <h2>🎯 Continue Learning</h2>
            {recentQuizzes.length > 0 ? (
              <QuizList
                quizzes={recentQuizzes}
                onClick={(quiz) => navigate(`/quiz/${quiz.id}/take`)}
              />
            ) : (
              <p>No quizzes yet. Create one to get started!</p>
            )}
          </Card>

          <Card>
            <h2>📚 Your Subjects</h2>
            {subjects.length > 0 ? (
              <SubjectList
                subjects={subjects}
                onClick={(subject) => navigate(`/subjects/${subject.id}`)}
              />
            ) : (
              <p>No subjects yet. Create one to get started!</p>
            )}
          </Card>

          <div className="dashboard-actions">
            <Button onClick={() => navigate('/subjects')}>
              Browse All Subjects →
            </Button>
            <Button variant="secondary" onClick={() => navigate('/performance')}>
              View Performance →
            </Button>
          </div>
        </>
      )}
    </div>
  )
}
