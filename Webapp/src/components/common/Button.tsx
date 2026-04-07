import React from 'react'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  fullWidth?: boolean
}

export default function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  children,
  disabled,
  ...props
}: ButtonProps) {
  const classNames = [
    'btn',
    `btn-${variant}`,
    `btn-${size}`,
    fullWidth && 'btn-full-width',
    loading && 'btn-loading',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <button
      className={classNames}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? <span className="spinner"></span> : children}
    </button>
  )
}
