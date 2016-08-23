/**
 * The Class Question.
 */
public class Question {
	
	/** The question text. */
	private String questionText;
	
	/** The real answer. */
	private String realAnswer;
	
	
	/**
	 * Instantiates a new question.
	 *
	 * @param questionText the question text
	 * @param realAnswer the real answer
	 */
	public Question(String questionText, String realAnswer) {
		super();
		this.questionText = questionText;
		this.realAnswer = realAnswer;
	}


	/**
	 * Gets the question text.
	 *
	 * @return the question text
	 */
	public String getQuestionText() {
		return questionText;
	}


	/**
	 * Gets the real answer.
	 *
	 * @return the real answer
	 */
	public String getRealAnswer() {
		return realAnswer;
	}
	
	
}
