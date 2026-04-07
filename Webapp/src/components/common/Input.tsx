import React from 'react'

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  helperText?: string
}

export default function Input({
  label,
  error,
  helperText,
  id,
  ...props
}: InputProps) {
  const generatedId = id || `input-${Math.random()}`

  return (
    <div className="form-group">
      {label && (
        <label htmlFor={generatedId} className="form-label">
          {label}
        </label>
      )}
      <input
        id={generatedId}
        className={`form-input ${error ? 'error' : ''}`}
        {...props}
      />
      {error && <span className="form-error">{error}</span>}
      {helperText && <span className="form-helper">{helperText}</span>}
    </div>
  )
}
