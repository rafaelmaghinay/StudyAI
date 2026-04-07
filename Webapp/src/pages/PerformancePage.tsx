import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import Card from '../components/common/Card'
import LoadingSpinner from '../components/common/LoadingSpinner'
import { QuizAttempt } from '../types'
import { quizService } from '../services/quizService'
import {
  LineChart,
  Line,
  PieChart,
  Pie,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
} from 'recharts'
import './PerformancePage.css'

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8']
const PASS_COLORS = ['#22c55e', '#ef4444']

export default function PerformancePage() {
  const { user } = useAuth()
  const [attempts, setAttempts] = useState<QuizAttempt[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (user?.id) {
      loadAttempts()
    }
  }, [user?.id])

  const loadAttempts = async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      const data = await quizService.getAttempts(user.id)
      // Sort by completedAt date to ensure chronological order for line chart
      const sorted = (data || []).sort(
        (a, b) => new Date(a.completedAt).getTime() - new Date(b.completedAt).getTime()
      )
      setAttempts(sorted)
    } catch (error) {
      console.error('Failed to load attempts:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <LoadingSpinner message="Loading performance data..." />
  }

  const averageScore =
    attempts.length > 0
      ? (attempts.reduce((sum, a) => sum + a.score, 0) / attempts.length).toFixed(1)
      : 0

  const totalQuestions = attempts.reduce((sum, a) => sum + a.totalQuestions, 0)
  const totalCorrect = attempts.reduce((sum, a) => sum + a.correctAnswers, 0)

  // Data for score trend line chart (limit to last 15 for readability)
  const scoreData = attempts.slice(-15).map((attempt, index) => ({
    name: `Quiz ${attempts.length - 15 + index + 1}`,
    score: attempt.score,
    date: new Date(attempt.completedAt).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
    }),
  }))

  // Data for correct vs incorrect pie chart
  const correctnessData = [
    { name: 'Correct', value: totalCorrect, fill: '#22c55e' },
    { name: 'Incorrect', value: totalQuestions - totalCorrect, fill: '#ef4444' },
  ]

  // Data for pass/fail pie chart
  const passCount = attempts.filter((a) => a.score >= 70).length
  const failCount = attempts.filter((a) => a.score < 70).length
  const passFailData = [
    { name: 'Passed (≥70%)', value: passCount, fill: '#22c55e' },
    { name: 'Failed (<70%)', value: failCount, fill: '#ef4444' },
  ]

  // Difficulty distribution pie chart (mock - based on score distribution)
  const difficultyData = [
    { name: 'Easy (80-100%)', value: attempts.filter((a) => a.score >= 80).length },
    { name: 'Medium (60-79%)', value: attempts.filter((a) => a.score >= 60 && a.score < 80).length },
    { name: 'Hard (<60%)', value: attempts.filter((a) => a.score < 60).length },
  ].filter((d) => d.value > 0)

  return (
    <div className="performance-page">
      <h1>📊 Your Performance</h1>

      <div className="stats-grid">
        <Card>
          <h3>Total Quizzes</h3>
          <p className="stat-number">{attempts.length}</p>
        </Card>
        <Card>
          <h3>Average Score</h3>
          <p className="stat-number">{averageScore}%</p>
        </Card>
        <Card>
          <h3>Total Questions</h3>
          <p className="stat-number">{totalQuestions}</p>
        </Card>
        <Card>
          <h3>Correct Answers</h3>
          <p className="stat-number">
            {totalCorrect}/{totalQuestions}
          </p>
        </Card>
      </div>

      {attempts.length > 0 && (
        <>
          {/* Score Trend Chart */}
          {scoreData.length > 0 && (
            <Card>
              <h2>📈 Score Trend</h2>
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={scoreData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                    <XAxis dataKey="date" />
                    <YAxis domain={[0, 100]} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#f3f4f6',
                        border: '1px solid #d1d5db',
                        borderRadius: '8px',
                      }}
                    />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="score"
                      stroke="#0088FE"
                      dot={{ fill: '#0088FE', r: 6 }}
                      activeDot={{ r: 8 }}
                      strokeWidth={2}
                      name="Score (%)"
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Card>
          )}

          {/* Charts Grid */}
          <div className="charts-grid">
            {/* Correct vs Incorrect Pie Chart */}
            {totalQuestions > 0 && (
              <Card>
                <h2>✅ Answer Accuracy</h2>
                <div className="pie-chart-container">
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={correctnessData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, value }) => `${name}: ${value}`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {correctnessData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.fill} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            )}

            {/* Pass/Fail Distribution Pie Chart */}
            {attempts.length > 0 && (
              <Card>
                <h2>🎯 Pass/Fail Distribution</h2>
                <div className="pie-chart-container">
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={passFailData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, value }) => `${name}: ${value}`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {passFailData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.fill} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            )}

            {/* Difficulty Distribution Pie Chart */}
            {difficultyData.length > 0 && (
              <Card>
                <h2>💪 Performance by Score Range</h2>
                <div className="pie-chart-container">
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={difficultyData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, value }) => `${name}: ${value}`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {difficultyData.map((_, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </Card>
            )}
          </div>
        </>
      )}

      <Card>
        <h2>📈 Recent Quiz History</h2>
        {attempts.length === 0 ? (
          <p>No quiz attempts yet</p>
        ) : (
          <table className="history-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Score</th>
                <th>Questions</th>
                <th>Time Spent</th>
              </tr>
            </thead>
            <tbody>
              {attempts.map((attempt) => (
                <tr key={attempt.id}>
                  <td>{new Date(attempt.completedAt).toLocaleDateString()}</td>
                  <td className="score-cell">{attempt.score.toFixed(1)}%</td>
                  <td>{attempt.correctAnswers}/{attempt.totalQuestions}</td>
                  <td>
                    {Math.floor((attempt.timeSpentSeconds || 0) / 60)}m{' '}
                    {(attempt.timeSpentSeconds || 0) % 60}s
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
