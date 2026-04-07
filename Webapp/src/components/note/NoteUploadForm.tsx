import { useState } from 'react'
import Button from '../common/Button'
import Input from '../common/Input'
import './NoteUploadForm.css'

interface NoteUploadFormProps {
  subjectId: string
  onSubmit: (data: {
    file: File
    title: string
    subjectId: string
  }) => Promise<void>
  loading?: boolean
}

const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
const ALLOWED_TYPES = ['.pdf', '.docx', '.doc', '.txt']

export default function NoteUploadForm({
  subjectId,
  onSubmit,
  loading = false,
}: NoteUploadFormProps) {
  const [formData, setFormData] = useState({
    title: '',
    file: null as File | null,
  })
  const [dragActive, setDragActive] = useState(false)
  const [error, setError] = useState<string>('')

  const getFileIcon = (fileName: string) => {
    if (fileName.endsWith('.pdf')) return '📕'
    if (fileName.endsWith('.docx') || fileName.endsWith('.doc')) return '📘'
    if (fileName.endsWith('.txt')) return '📄'
    return '📁'
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
  }

  const validateFile = (file: File): boolean => {
    setError('')

    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
    if (!ALLOWED_TYPES.includes(fileExtension)) {
      setError(`File type not supported. Please upload: ${ALLOWED_TYPES.join(', ')}`)
      return false
    }

    if (file.size > MAX_FILE_SIZE) {
      setError(`File is too large. Maximum size is ${formatFileSize(MAX_FILE_SIZE)}`)
      return false
    }

    return true
  }

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(e.type === 'dragenter' || e.type === 'dragover')
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setDragActive(false)

    const files = e.dataTransfer.files
    if (files?.[0]) {
      if (validateFile(files[0])) {
        setFormData((prev) => ({ ...prev, file: files[0] }))
      }
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      if (validateFile(e.target.files[0])) {
        setFormData((prev) => ({ ...prev, file: e.target.files![0] }))
      }
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleRemoveFile = () => {
    setFormData((prev) => ({ ...prev, file: null }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.title.trim()) {
      setError('Please enter a title for your note')
      return
    }

    if (!formData.file) {
      setError('Please select a file to upload')
      return
    }

    setError('')
    
    try {
      await onSubmit({
        file: formData.file,
        title: formData.title,
        subjectId,
      })
      setFormData({ title: '', file: null })
    } catch (err) {
      setError('Failed to upload note. Please try again.')
    }
  }

  return (
    <form onSubmit={handleSubmit} className="note-upload-form">
      <div className="form-section">
        <h3 className="section-title">📝 Note Details</h3>
        
        <Input
          label="Note Title"
          name="title"
          value={formData.title}
          onChange={handleChange}
          placeholder="E.g., Chapter 1 - Introduction to Biology"
          error={!formData.title.trim() && error ? error : ''}
        />
      </div>

      <div className="form-section">
        <h3 className="section-title">📂 Upload File</h3>
        
        <div
          className={`file-drop-zone ${dragActive ? 'active' : ''} ${
            formData.file ? 'has-file' : ''
          }`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          {!formData.file ? (
            <>
              <input
                type="file"
                id="file-input"
                onChange={handleFileChange}
                accept={ALLOWED_TYPES.join(',')}
                style={{ display: 'none' }}
              />
              <label htmlFor="file-input" className="file-label">
                <span className="file-icon">📁</span>
                <span className="file-text">
                  <strong>Click to browse</strong> or drag file here
                </span>
                <span className="file-info">
                  Supported formats: PDF, DOCX, DOC, TXT • Max size: 10 MB
                </span>
              </label>
            </>
          ) : (
            <div className="file-preview">
              <div className="file-preview-header">
                <span className="file-preview-icon">
                  {getFileIcon(formData.file.name)}
                </span>
                <div className="file-preview-info">
                  <p className="file-preview-name">{formData.file.name}</p>
                  <p className="file-preview-size">
                    {formatFileSize(formData.file.size)}
                  </p>
                </div>
                <button
                  type="button"
                  className="file-remove-btn"
                  onClick={handleRemoveFile}
                  title="Remove file"
                >
                  ✕
                </button>
              </div>
              <div className="file-preview-actions">
                <label htmlFor="file-input-replace" className="file-change-btn">
                  Choose Different File
                </label>
                <input
                  type="file"
                  id="file-input-replace"
                  onChange={handleFileChange}
                  accept={ALLOWED_TYPES.join(',')}
                  style={{ display: 'none' }}
                />
              </div>
            </div>
          )}
        </div>
      </div>

      {error && <div className="form-error-banner">{error}</div>}

      <div className="form-actions">
        <Button 
          type="submit" 
          loading={loading} 
          fullWidth
          disabled={!formData.file || !formData.title.trim()}
        >
          {loading ? 'Uploading & Processing...' : '⬆️ Upload & Process'}
        </Button>
      </div>

      <div className="form-footer">
        <p>
          💡 <strong>Tip:</strong> Notes are automatically processed to extract content. You can create quizzes from your processed notes.
        </p>
      </div>
    </form>
  )
}
