import React from 'react'

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode
  hoverable?: boolean
}

export default function Card({
  children,
  hoverable = false,
  className = '',
  ...props
}: CardProps) {
  const classes = ['card', hoverable && 'card-hover', className]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={classes} {...props}>
      {children}
    </div>
  )
}
