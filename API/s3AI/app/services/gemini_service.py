import google.generativeai as genai
from app.config import settings
import logging
import json
from typing import List, Optional

logger = logging.getLogger(__name__)

# Import Grok service for fallback
grok_service = None  # Lazy import to avoid circular dependency

def get_grok_service():
    global grok_service
    if grok_service is None:
        from app.services.grok_service import GrokService
        grok_service = GrokService()
    return grok_service


class GeminiService:
    """Facade for Google Gemini-based quiz generation with Grok fallback.

    Handles lazy client initialization, prompt construction, response parsing,
    and graceful degradation to the GrokService when Gemini is unavailable or
    rate-limited.
    """
    def __init__(self):
        self.client = None
        self.model = settings.quiz_generation_model
        self._initialized = False

    def _ensure_initialized(self):
        """Lazily initialize the Gemini client on first use"""
        if not self._initialized:
            if not settings.gemini_api_key:
                raise ValueError("Gemini API key is not configured in .env file")
            try:
                genai.configure(api_key=settings.gemini_api_key)
                self.client = genai.GenerativeModel(self.model)
                self._initialized = True
                logger.info(f"Gemini client initialized with model: {self.model}")
            except Exception as e:
                logger.error(f"Failed to initialize Gemini client: {str(e)}")
                raise

    async def generate_quiz_from_text(self, text: str, num_questions: int = 10, quiz_title: str = "Generated Quiz") -> dict:
        """
        Generate multiple choice quiz questions from extracted text using Google Gemini (with Grok fallback)

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
            logger.info(f"Attempting quiz generation with Gemini (primary)")
            prompt = self._build_quiz_generation_prompt(text, num_questions, quiz_title)

            response = self.client.generate_content(
                prompt,
                generation_config=genai.types.GenerationConfig(
                    max_output_tokens=4096,
                    temperature=0.7,
                )
            )

            # Parse response
            response_text = response.text
            questions = self._parse_quiz_response(response_text)

            logger.info(f"Successfully generated {len(questions)} multiple choice quiz questions using Gemini (primary)")

            return {
                "quiz_title": quiz_title,
                "questions": questions,
                "total_questions": len(questions),
                "generation_status": "success",
                "ai_provider": "gemini"
            }

        except Exception as e:
            error_str = str(e)
            # Check if it's a quota or rate limit error
            if any(keyword in error_str for keyword in ["quota", "429", "rate limit", "exceeded"]):
                logger.warning(f"Gemini quota/rate limit exceeded, falling back to Grok: {error_str}")
                
                # Fallback to Grok
                try:
                    grok = get_grok_service()
                    logger.info(f"Attempting quiz generation with Grok (fallback)")
                    
                    result = await grok.generate_quiz_from_text(text, num_questions, quiz_title)
                    logger.info(f"Successfully fell back to Grok for quiz generation")
                    return result
                    
                except Exception as grok_error:
                    logger.error(f"Grok fallback also failed: {str(grok_error)}")
                    raise Exception(f"Both Gemini and Grok failed. Gemini error: {error_str}, Grok error: {str(grok_error)}")
            else:
                # Not a quota error, re-raise immediately
                logger.error(f"Error generating quiz from text: {str(e)}")
                raise Exception(f"Failed to generate quiz: {str(e)}")

    def _build_quiz_generation_prompt(self, text: str, num_questions: int, quiz_title: str) -> str:
        """
        Build prompt for Gemini quiz generation - ALL MULTIPLE CHOICE WITH A, B, C, D

        Args:
            text: Document text
            num_questions: Number of questions
            quiz_title: Quiz title

        Returns:
            Formatted prompt string
        """
        return f"""
You are an expert educational content creator. Based on the following text, generate exactly {num_questions} multiple choice quiz questions.

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
Return a JSON array with this exact structure:
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

IMPORTANT:
- Correct answer MUST be ONLY the letter: A, B, C, or D (not the full text)
- Each question must have exactly 4 options
- All questions must be multiple choice
- Options list must have exactly 4 strings

Generate the JSON array now:
"""

    def _parse_quiz_response(self, response_text: str) -> List[dict]:
        """
        Parse Gemini response to extract quiz questions

        Args:
            response_text: Raw response from Gemini

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
            logger.error(f"Error parsing JSON response: {str(e)}")
            raise Exception(f"Failed to parse quiz generation response: {str(e)}")

