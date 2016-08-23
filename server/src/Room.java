import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
/**
 * The Class Room.
 */
public class Room {
	
	/** The room name. */
	private String roomName;
	
	/** The game. */
	private Game game;
	
	/** The is started. */
	private Boolean isStarted;
	
	/** The round number. */
	private int roundNumber;
	
	/** The players. */
	private ArrayList<Player> players;
	
	/** The questions. */
	private ArrayList<Question> questions;
	
	/** The real answer number. */
	private String realAnswerNumber;
	
	/** The answers. */
	private HashMap<String,Player> answers;
	
	/** The answers bank to shuffle. */
	private Vector<String> answersBankToShuffle;
	
	/** The shuffled answers. */
	private HashMap<String,String> shuffledAnswers;
	
	/** The choices. */
	private HashMap<Player,String> choices;
	
	

	/**
	 * Instantiates a new room.
	 *
	 * @param roomName the room name
	 * @param players the players
	 */
	public Room(String roomName, ArrayList<Player> players) {
		super();
		this.roundNumber = 0;
		this.roomName = roomName;
		this.isStarted = false;
		this.players = players;
		this.game = null;
		this.answers = new HashMap<String,Player>();
		this.questions = new ArrayList<Question>();
		this.answers = new HashMap<String,Player>();
		this.answersBankToShuffle = new Vector<String>();
		this.shuffledAnswers = new HashMap<String,String>();
		this.choices = new HashMap<Player,String>();
	}
	
	/**
	 * Gets the checks if is started.
	 *
	 * @return the checks if is started
	 */
	public Boolean getIsStarted() {
		return isStarted;
	}
	
	/**
	 * Gets the round number.
	 *
	 * @return the round number
	 */
	public int getRoundNumber() {
		return roundNumber;
	}
	
	/**
	 * Gets the questions.
	 *
	 * @return the questions
	 */
	public ArrayList<Question> getQuestions() {
		return questions;
	}
	
	/**
	 * Gets the real answer number.
	 *
	 * @return the real answer number
	 */
	public String getRealAnswerNumber() {
		return realAnswerNumber;
	}
	
	/**
	 * Gets the answers bank to shuffle.
	 *
	 * @return the answers bank to shuffle
	 */
	public Vector<String> getAnswersBankToShuffle() {
		return answersBankToShuffle;
	}
	
	/**
	 * Gets the shuffled answers.
	 *
	 * @return the shuffled answers
	 */
	public HashMap<String, String> getShuffledAnswers() {
		return shuffledAnswers;
	}
	
	/**
	 * Gets the choices.
	 *
	 * @return the choices
	 */
	public HashMap<Player, String> getChoices() {
		return choices;
	}
	
	/**
	 * Gets the room name.
	 *
	 * @return the room name
	 */
	public String getRoomName() {
		return roomName;
	}

	/**
	 * Game is started.
	 *
	 * @return the boolean
	 */
	public Boolean gameIsStarted() {
		return isStarted;
	}

	/**
	 * Gets the players.
	 *
	 * @return the players
	 */
	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	/**
	 * Adds the player.
	 *
	 * @param player the player
	 */
	public void addPlayer(Player player){
		players.add(player);
	}
	
	/**
	 * Gets the game.
	 *
	 * @return the game
	 */
	public Game getGame(){
		return game;
	}
	
	/**
	 * Sets the game.
	 *
	 * @param gameName the game name
	 * @return true, if successful
	 */
	public boolean setGame(String gameName){
		if(isStarted || gameName==null){
			return false;
		}
		else{
			GameBoard gameBoard = GameBoard.getInstance();
			game = gameBoard.getGame(gameName);
			if(game==null)
				return false;
			return true;
		}
	}

	/**
	 * Gets the answers.
	 *
	 * @return the answers
	 */
	public HashMap<String, Player> getAnswers() {
		return answers;
	}

	/**
	 * Insert player answer.
	 *
	 * @param answer the answer
	 * @param player the player
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void insertPlayerAnswer(String answer, Player player) throws IOException {
		answers.put(answer, player);
		answersBankToShuffle.addElement(answer);
		if(answersBankToShuffle.size()==(players.size())){
			sendAnswersOptions();
		}
	}
	
	/**
	 * Send answers options.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void sendAnswersOptions() throws IOException{
		answersBankToShuffle.add(questions.get(roundNumber).getRealAnswer());
		int counter = 0;
		while(answersBankToShuffle.size() != 0){
			int index = (int) (Math.random()*answersBankToShuffle.size());
			String answer = answersBankToShuffle.remove(index);
			if(answer.equals(questions.get(roundNumber).getRealAnswer()))
				realAnswerNumber=""+counter;
			shuffledAnswers.put(""+counter, answer);
			counter++;
		}
		String ans = questions.get(roundNumber).getQuestionText()+"\n"+"The options are:";
		for(String answerNumber : shuffledAnswers.keySet()){
			ans += "\n\t"+answerNumber+". "+shuffledAnswers.get(answerNumber);
		}
		for(Player player1 : players){
			player1.getCallback().sendMessage("ASKCHIOCES "+ans);
		}
	}
	
	/**
	 * Insert player choice.
	 *
	 * @param player the player
	 * @param answer the answer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void insertPlayerChoice(Player player, String answer) throws IOException {
		choices.put(player, answer);
		if(choices.size()==players.size()){
			game.analyze(this);
			roundNumber++;
			if(roundNumber<3){
				shuffledAnswers.clear();
				choices.clear();
				answers.clear();
				sendQuestion();
			}
			if(roundNumber==3){
				String gameOverMsg = "The game is over and ths scores are:"+"\n";
				for(Player player2 : players){
					gameOverMsg += "\t"+"Nickname: "+player2.getNickname()+" Score: "+player2.getScore();
				}
				for(Player player3 : players){
					player3.getCallback().sendMessage("SYSMSG SELECTRESP ACCEPTED "+gameOverMsg);
				}
				shuffledAnswers.clear();
				choices.clear();
				answers.clear();
				roundNumber=0;
				questions.clear();
				players.clear();
				isStarted=false;
			}
		}
	}
	
	/**
	 * Start game.
	 *
	 * @param gameToStart the game to start
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void startGame(String gameToStart) throws IOException {
		isStarted = true;
		GameBoard gameBoard = GameBoard.getInstance();
		game = gameBoard.getGame(gameToStart);
		questions = gameBoard.get3Questions();
		sendQuestion();
	}
	
	/**
	 * Send question.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void sendQuestion() throws IOException {
		for(Player player4 : players){
			player4.getCallback().sendMessage("ASKTXT "+ questions.get(roundNumber).getQuestionText());
		}
		
	}
}
