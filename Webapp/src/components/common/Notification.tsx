import { useEffect, useState } from 'react'

export interface Notification {
  id: string
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

interface NotificationProps extends Notification {
  onClose: (id: string) => void
}

function NotificationItem({ id, message, type, duration = 5000, onClose }: NotificationProps) {
  useEffect(() => {
    const timer = setTimeout(() => onClose(id), duration)
    return () => clearTimeout(timer)
  }, [id, duration, onClose])

  return (
    <div className={`notification notification-${type}`}>
      <span>{message}</span>
      <button onClick={() => onClose(id)} className="notification-close">
        ✕
      </button>
    </div>
  )
}

interface NotificationContainerProps {
  notifications: Notification[]
  onClose: (id: string) => void
}

export function NotificationContainer({
  notifications,
  onClose,
}: NotificationContainerProps) {
  return (
    <div className="notification-container">
      {notifications.map((notif) => (
        <NotificationItem key={notif.id} {...notif} onClose={onClose} />
      ))}
    </div>
  )
}
