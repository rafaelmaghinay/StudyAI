import React from 'react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title?: string
  children: React.ReactNode
  size?: 'small' | 'medium' | 'large'
}

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  size = 'medium',
}: ModalProps) {
  if (!isOpen) return null

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className={`modal modal-${size}`}
        onClick={(e) => e.stopPropagation()}
      >
        {title && (
          <div className="modal-header">
            <h2>{title}</h2>
            <button
              className="modal-close"
              onClick={onClose}
              aria-label="Close modal"
            >
              ✕
            </button>
          </div>
        )}
        <div className="modal-body">{children}</div>
      </div>
    </div>
  )
}
