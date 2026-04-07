import json
import logging
from typing import List
from openai import OpenAI
from app.config import settings

logger = logging.getLogger(__name__)

class GrokService:
    def __init__(self):
        self.client = None
        self.model = settings.grok_model
        self._initialized = False

    def _ensure_initialized(self):
        """Initialize Grok client on first use"""
        if not self._initialized:
            if not settings.grok_api_key:
                raise ValueError("Grok API key is not configured in .env file")
            try:
                self.client = OpenAI(
                    api_key=settings.grok_api_key,
                    base_url=settings.grok_api_url
                )
                self._initialized = True
                logger.info(f"Grok client initialized with model: {self.model}")
            except Exception as e:
                logger.error(f"Failed to initialize Grok client: {str(e)}")
                raise

    async def generate_quiz_from_text(self, text: str, num_questions: int = 10, quiz_title: str = "Generated Quiz") -> dict:
        """
        Generate multiple choice quiz questions from extracted text using Grok (xAI)

        Args:
            text: Extracted text content from document
            num_questions: Number of questions to generate
            quiz_title: Title for the quiz

        Returns:
            dict with generated questions (all multiple choice with A, B, C, D options)
        """
        self._ensure_initialized()

        if num_questions > settings.max_num_questions:
            raise ValueError(f"Number of questions ({num_questions}) exceeds maximum allowed ({settings.max_num_questions})")

        try:
            prompt = self._build_quiz_generation_prompt(text, num_questions, quiz_title)

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are an expert educational content creator. Generate quiz questions exactly in JSON format as requested."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.7,
                max_tokens=4096
            )

            # Parse response
            response_text = response.choices[0].message.content
            questions = self._parse_quiz_response(response_text)

            logger.info(f"Successfully generated {len(questions)} multiple choice quiz questions using Grok")

            return {
                "quiz_title": quiz_title,
                "questions": questions,
                "total_questions": len(questions),
                "generation_status": "success",
                "ai_provider": "grok"
            }

        except Exception as e:
            logger.error(f"Error generating quiz from text using Grok: {str(e)}")
            raise Exception(f"Failed to generate quiz with Grok: {str(e)}")

    def _build_quiz_generation_prompt(self, text: str, num_questions: int, quiz_title: str) -> str:
        """
        Build prompt for Grok quiz generation - ALL MULTIPLE CHOICE WITH A, B, C, D

        Args:
            text: Document text
            num_questions: Number of questions
            quiz_title: Quiz title

        Returns:
            Formatted prompt string
        """
        return f"""
Based on the following text, generate exactly {num_questions} multiple choice quiz questions.

TEXT:
{text}

REQUIREMENTS:
1. Generate exactly {num_questions} multiple choice questions
2. EVERY question MUST have exactly 4 options
3. EVERY question MUST have the correct answer as ONE of: A, B, C, or D
4. Options should be labeled as: A, B, C, D (not 1, 2, 3, 4)
5. Vary difficulty levels from easy to hard
6. Make questions test understanding, not just memorization
7. Include brief explanations for why the answer is correct

OUTPUT FORMAT:
Return ONLY a valid JSON array with this exact structure (no markdown, no extra text):
[
  {{
    "question_text": "What is the primary function of mitochondria?",
    "question_type": "multiple_choice",
    "options": ["DNA storage", "Energy production", "Protein synthesis", "Cell division"],
    "correct_answer": "B",
    "explanation": "Mitochondria are responsible for producing ATP through cellular respiration",
    "order_index": 1
  }},
  {{
    "question_text": "Which of the following is NOT a organelle?",
    "question_type": "multiple_choice",
    "options": ["Ribosome", "Cytoplasm", "Golgi apparatus", "Mitochondria"],
    "correct_answer": "B",
    "explanation": "Cytoplasm is the gel-like substance filling the cell, not a separate organelle",
    "order_index": 2
  }}
]

CRITICAL REQUIREMENTS:
- Return ONLY the JSON array, nothing else
- Correct answer must be ONLY the letter: A, B, C, or D
- Each question must have exactly 4 options
- All questions must be multiple choice
- Options list must have exactly 4 strings
"""

    def _parse_quiz_response(self, response_text: str) -> List[dict]:
        """
        Parse Grok response to extract quiz questions

        Args:
            response_text: Raw response from Grok

        Returns:
            List of question dictionaries
        """
        try:
            # Find JSON in response
            start_idx = response_text.find('[')
            end_idx = response_text.rfind(']') + 1

            if start_idx == -1 or end_idx == 0:
                raise ValueError("Could not find JSON array in response")

            json_str = response_text[start_idx:end_idx]
            questions = json.loads(json_str)

            # Validate and clean questions
            validated_questions = []
            for idx, q in enumerate(questions):
                options = q.get("options", [])

                # Ensure exactly 4 options
                if len(options) != 4:
                    logger.warning(f"Question {idx} does not have exactly 4 options, skipping")
                    continue

                correct_answer = str(q.get("correct_answer", "")).upper()

                # Ensure correct answer is A, B, C, or D
                if correct_answer not in ["A", "B", "C", "D"]:
                    logger.warning(f"Question {idx} has invalid correct answer '{correct_answer}', skipping")
                    continue

                validated_questions.append({
                    "question_text": q.get("question_text", ""),
                    "question_type": "multiple_choice",
                    "options": options,
                    "correct_answer": correct_answer,
                    "explanation": q.get("explanation", ""),
                    "order_index": len(validated_questions) + 1
                })

            if len(validated_questions) == 0:
                raise ValueError("No valid questions were parsed from the response")

            return validated_questions

        except json.JSONDecodeError as e:
            logger.error(f"Error parsing JSON response from Grok: {str(e)}")
            raise Exception(f"Failed to parse quiz generation response: {str(e)}")
