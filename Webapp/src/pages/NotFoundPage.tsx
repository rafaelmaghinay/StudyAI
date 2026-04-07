import { useNavigate } from 'react-router-dom'
import Button from '../components/common/Button'
import './NotFoundPage.css'

export default function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className="not-found-page">
      <div className="not-found-content">
        <h1>404</h1>
        <h2>Page Not Found</h2>
        <p>Sorry, the page you're looking for doesn't exist.</p>
        <Button onClick={() => navigate('/')}>Go Back to Dashboard</Button>
      </div>
    </div>
  )
}
