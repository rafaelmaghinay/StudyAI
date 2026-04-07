import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { noteService } from '../services/noteService'
import { subjectService } from '../services/subjectService'
import { quizService } from '../services/quizService'
import { Note, Question, Subject } from '../types'
import Button from '../components/common/Button'
import Input from '../components/common/Input'
import Card from '../components/common/Card'
import './CreateQuiz.css'

type Step = 1 | 2 | 3

interface QuizConfig {
  title: string
  description: string
  selectedNoteIds: string[]
  subjectId?: string
  difficulty: 'easy' | 'medium' | 'hard'
  questionCount: number
}

export default function CreateQuizPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [step, setStep] = useState<Step>(1)
  const [notes, setNotes] = useState<Note[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [loading, setLoading] = useState(false)
  const [generatedQuestions, setGeneratedQuestions] = useState<Question[]>([])
  const [config, setConfig] = useState<QuizConfig>({
    title: '',
    description: '',
    selectedNoteIds: [],
    subjectId: undefined,
    difficulty: 'medium',
    questionCount: 5,
  })

  useEffect(() => {
    if (user?.id) {
      loadUserNotes()
      loadSubjects()
    }
  }, [user?.id])

  const loadUserNotes = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      const userNotes = await noteService.getByUser(user.id)
      setNotes(userNotes || [])
    } catch (error) {
      console.error('Failed to load notes:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadSubjects = async () => {
    if (!user?.id) return
    try {
      const userSubjects = await subjectService.getAll(user.id)
      setSubjects(userSubjects || [])
    } catch (error) {
      console.error('Failed to load subjects:', error)
    }
  }

  const toggleNoteSelection = (noteId: string) => {
    setConfig((prev) => ({
      ...prev,
      selectedNoteIds: prev.selectedNoteIds.includes(noteId)
        ? prev.selectedNoteIds.filter((id) => id !== noteId)
        : [...prev.selectedNoteIds, noteId],
    }))
  }

  const handleGenerateQuestions = async () => {
    if (!config.title || config.selectedNoteIds.length === 0) {
      alert('Please fill in all required fields')
      return
    }
    if (!user?.id) {
      alert('User not authenticated')
      return
    }
    try {
      setLoading(true)
      // Call API to generate questions and get preview
      const quiz = await quizService.create(
        config.selectedNoteIds,
        {
          title: config.title,
          description: config.description,
          difficulty: config.difficulty,
          questionCount: config.questionCount,
          subjectId: config.subjectId,
        },
        user.id
      )
      setGeneratedQuestions(quiz.questions || [])
      setStep(3)
    } catch (error) {
      console.error('Failed to generate questions:', error)
      alert('Failed to generate quiz questions')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateQuiz = async () => {
    if (!user?.id) {
      alert('User not authenticated')
      return
    }
    try {
      setLoading(true)
      const quiz = await quizService.create(
        config.selectedNoteIds,
        {
          title: config.title,
          description: config.description,
          difficulty: config.difficulty,
          questionCount: config.questionCount,
          subjectId: config.subjectId,
        },
        user.id
      )
      // After creating the quiz, go to the quizzes list page
      navigate('/quizzes')
    } catch (error) {
      console.error('Failed to create quiz:', error)
      alert('Failed to create quiz')
    } finally {
      setLoading(false)
    }
  }

  const isStep1Valid = config.selectedNoteIds.length > 0
  const isStep2Valid =
    config.title.trim() !== '' && config.selectedNoteIds.length > 0

  return (
    <div className="create-quiz-container">
      <div className="stepper">
        <div className={`step ${step >= 1 ? 'active' : ''}`}>
          <div className="step-number">1</div>
          <div className="step-label">Select Notes</div>
        </div>
        <div className={`step-connector ${step > 1 ? 'active' : ''}`}></div>
        <div className={`step ${step >= 2 ? 'active' : ''}`}>
          <div className="step-number">2</div>
          <div className="step-label">Configure</div>
        </div>
        <div className={`step-connector ${step > 2 ? 'active' : ''}`}></div>
        <div className={`step ${step >= 3 ? 'active' : ''}`}>
          <div className="step-number">3</div>
          <div className="step-label">Review</div>
        </div>
      </div>

      {/* Step 1: Select Notes */}
      {step === 1 && (
        <Card>
          <h2>📝 Step 1: Select Notes</h2>
          <p className="step-subtitle">
            Choose one or more notes to generate a quiz from
          </p>

          {loading ? (
            <div className="loading">Loading your notes...</div>
          ) : notes.length === 0 ? (
            <div className="empty-state">
              <p>No notes found. Upload some notes first to create a quiz.</p>
              <Button onClick={() => navigate('/notes')}>
                Go to Notes →
              </Button>
            </div>
          ) : (
            <div className="notes-selection">
              {notes.map((note) => (
                <div
                  key={note.id}
                  className={`note-item ${
                    config.selectedNoteIds.includes(note.id) ? 'selected' : ''
                  }`}
                  onClick={() => toggleNoteSelection(note.id)}
                >
                  <input
                    type="checkbox"
                    checked={config.selectedNoteIds.includes(note.id)}
                    onChange={() => toggleNoteSelection(note.id)}
                  />
                  <div className="note-info">
                    <h4>{note.title}</h4>
                    <p className="note-meta">
                      {note.fileType ? `${note.fileType.toUpperCase()} • ` : ''}
                      {note.createdAt}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="step-actions">
            <Button variant="secondary" onClick={() => navigate('/notes')}>
              Cancel
            </Button>
            <Button
              onClick={() => setStep(2)}
              disabled={!isStep1Valid || loading}
            >
              Next →
            </Button>
          </div>
        </Card>
      )}

      {/* Step 2: Configure Quiz */}
      {step === 2 && (
        <Card>
          <h2>⚙️ Step 2: Configure Quiz</h2>
          <p className="step-subtitle">Set up your quiz parameters</p>

          <div className="form-group">
            <label>Quiz Title *</label>
            <Input
              type="text"
              placeholder="e.g., Biology Chapter 3 Quiz"
              value={config.title}
              onChange={(e) =>
                setConfig((prev) => ({ ...prev, title: e.target.value }))
              }
            />
          </div>

          <div className="form-group">
            <label>Description (Optional)</label>
            <Input
              type="text"
              placeholder="e.g., Quiz on cell structure and function"
              value={config.description}
              onChange={(e) =>
                setConfig((prev) => ({
                  ...prev,
                  description: e.target.value,
                }))
              }
            />
          </div>

          <div className="form-group">
            <label>Subject (Optional)</label>
            <select
              value={config.subjectId || ''}
              onChange={(e) =>
                setConfig((prev) => ({
                  ...prev,
                  subjectId: e.target.value || undefined,
                }))
              }
            >
              <option value="">No Subject Selected</option>
              {subjects.map((subject) => (
                <option key={subject.id} value={subject.id}>
                  {subject.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Difficulty Level</label>
              <select
                value={config.difficulty}
                onChange={(e) =>
                  setConfig((prev) => ({
                    ...prev,
                    difficulty: e.target.value as 'easy' | 'medium' | 'hard',
                  }))
                }
              >
                <option value="easy">Easy</option>
                <option value="medium">Medium</option>
                <option value="hard">Hard</option>
              </select>
            </div>

            <div className="form-group">
              <label>Number of Questions</label>
              <Input
                type="number"
                min="1"
                max="50"
                value={config.questionCount}
                onChange={(e) =>
                  setConfig((prev) => ({
                    ...prev,
                    questionCount: parseInt(e.target.value) || 5,
                  }))
                }
              />
            </div>
          </div>

          <div className="selected-notes">
            <h4>Selected Notes ({config.selectedNoteIds.length})</h4>
            <div className="notes-list">
              {notes
                .filter((n) => config.selectedNoteIds.includes(n.id))
                .map((note) => (
                  <span key={note.id} className="note-tag">
                    ✓ {note.title}
                  </span>
                ))}
            </div>
          </div>

          <div className="step-actions">
            <Button variant="secondary" onClick={() => setStep(1)}>
              ← Back
            </Button>
            <Button
              onClick={() => handleGenerateQuestions()}
              disabled={!isStep2Valid || loading}
            >
              {loading ? 'Generating...' : 'Generate Questions →'}
            </Button>
          </div>
        </Card>
      )}

      {/* Step 3: Review Questions */}
      {step === 3 && (
        <Card>
          <h2>👀 Step 3: Review Questions</h2>
          <p className="step-subtitle">
            Review the generated questions before creating the quiz
          </p>

          <div className="questions-preview">
            {generatedQuestions.length > 0 ? (
              generatedQuestions.map((question, index) => (
                <div key={question.id} className="question-preview">
                  <h4>
                    Question {index + 1} ({question.questionType})
                  </h4>
                  <p className="question-text">{question.questionText}</p>
                  {question.questionType === 'multiple_choice' && (
                    <ul className="options-list">
                      {(question.options || []).map((option, i) => (
                        <li key={i}>{String.fromCharCode(65 + i)}) {option}</li>
                      ))}
                    </ul>
                  )}
                  {question.questionType === 'true_false' && (
                    <ul className="options-list">
                      <li>A) True</li>
                      <li>B) False</li>
                    </ul>
                  )}
                </div>
              ))
            ) : (
              <p>No questions generated. Please try again.</p>
            )}
          </div>

          <div className="quiz-summary">
            <h4>Quiz Summary</h4>
            <ul>
              <li>Title: {config.title}</li>
              <li>Difficulty: {config.difficulty}</li>
              <li>Questions: {generatedQuestions.length}</li>
              <li>Selected Notes: {config.selectedNoteIds.length}</li>
            </ul>
          </div>

          <div className="step-actions">
            <Button variant="secondary" onClick={() => setStep(2)}>
              ← Back
            </Button>
            <Button
              onClick={handleCreateQuiz}
              disabled={generatedQuestions.length === 0 || loading}
            >
              {loading ? 'Creating...' : '✓ Create Quiz'}
            </Button>
          </div>
        </Card>
      )}
    </div>
  )
}
