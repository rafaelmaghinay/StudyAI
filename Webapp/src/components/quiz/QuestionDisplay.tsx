import { Question } from '../../types'
import Card from '../common/Card'

interface QuestionDisplayProps {
  question: Question
  selectedAnswer?: string
  onAnswerSelect: (answer: string) => void
  showCorrect?: boolean
}

export default function QuestionDisplay({
  question,
  selectedAnswer,
  onAnswerSelect,
  showCorrect = false,
}: QuestionDisplayProps) {
  const isCorrect = selectedAnswer === question.correctAnswer

  return (
    <Card className="question-display">
      <h3 className="question-text">{question.questionText}</h3>

      {question.questionType === 'multiple_choice' && question.options && (
        <div className="question-options">
          {question.options.map((option, idx) => {
            const optionLetter = String.fromCharCode(65 + idx) // A, B, C, D
            return (
              <label key={idx} className="option">
                <input
                  type="radio"
                  name="answer"
                  value={optionLetter}
                  checked={selectedAnswer === optionLetter}
                  onChange={() => onAnswerSelect(optionLetter)}
                  disabled={showCorrect}
                />
                <span className="option-text"><strong>{optionLetter}.</strong> {option}</span>
                {showCorrect && optionLetter === question.correctAnswer && (
                  <span className="option-badge correct">✓ Correct</span>
                )}
                {showCorrect && selectedAnswer === optionLetter && optionLetter !== question.correctAnswer && (
                  <span className="option-badge incorrect">✗ Wrong</span>
                )}
              </label>
            )
          })}
        </div>
      )}

      {question.questionType === 'true_false' && (
        <div className="question-options">
          {['True', 'False'].map((option) => (
            <label key={option} className="option">
              <input
                type="radio"
                name="answer"
                value={option}
                checked={selectedAnswer === option}
                onChange={() => onAnswerSelect(option)}
                disabled={showCorrect}
              />
              <span className="option-text">{option}</span>
              {showCorrect && option === question.correctAnswer && (
                <span className="option-badge correct">✓ Correct</span>
              )}
              {showCorrect && selectedAnswer === option && option !== question.correctAnswer && (
                <span className="option-badge incorrect">✗ Wrong</span>
              )}
            </label>
          ))}
        </div>
      )}

      {question.questionType === 'short_answer' && (
        <input
          type="text"
          placeholder="Enter your answer..."
          value={selectedAnswer || ''}
          onChange={(e) => onAnswerSelect(e.target.value)}
          disabled={showCorrect}
          className="form-input"
        />
      )}

      {showCorrect && question.explanation && (
        <div className={`explanation ${isCorrect ? 'correct' : 'incorrect'}`}>
          <strong>Explanation:</strong>
          <p>{question.explanation}</p>
        </div>
      )}
    </Card>
  )
}
