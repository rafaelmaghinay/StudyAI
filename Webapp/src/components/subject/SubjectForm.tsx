import { useState } from 'react'
import Button from '../common/Button'
import Input from '../common/Input'
import { Subject } from '../../types'

interface SubjectFormProps {
  subject?: Subject
  onSubmit: (data: Omit<Subject, 'id' | 'userId' | 'createdAt' | 'updatedAt'>) => Promise<void>
  loading?: boolean
}

export default function SubjectForm({
  subject,
  onSubmit,
  loading = false,
}: SubjectFormProps) {
  const [formData, setFormData] = useState({
    name: subject?.name || '',
    description: subject?.description || '',
  })

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    await onSubmit(formData)
  }

  return (
    <form onSubmit={handleSubmit} className="subject-form">
      <Input
        label="Subject Name"
        name="name"
        value={formData.name}
        onChange={handleChange}
        placeholder="E.g., Advanced Mathematics"
        required
      />

      <div className="form-group">
        <label htmlFor="description" className="form-label">
          Description (optional)
        </label>
        <textarea
          id="description"
          name="description"
          value={formData.description}
          onChange={handleChange}
          placeholder="Enter subject description..."
          rows={4}
          className="form-input"
        />
      </div>

      <Button type="submit" loading={loading} fullWidth>
        {subject ? 'Update Subject' : 'Create Subject'}
      </Button>
    </form>
  )
}
