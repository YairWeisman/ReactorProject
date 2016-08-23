import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import com.google.gson.Gson;
/**
 * The Class GameBoard.
 */
public class GameBoard {
	
	/** The callback to player. */
	private HashMap<ProtocolCallback<String>, Player> callbackToPlayer;
	
	/** The rooms. */
	private HashMap<String, Room> rooms;
	
	/** The games supported. */
	private HashMap<String, Game> games;
	
	/** The questions to ask. */
	private Question[] questions;
	
	
	/**
	 * Instantiates a new game board.
	 */
	private GameBoard() {
		this.callbackToPlayer = new HashMap<ProtocolCallback<String>, Player>();
		this.rooms = new HashMap<String, Room>();
		this.games = new HashMap<String, Game>();
	}
	
	/**
	 * The Class GameBoardHolder.
	 */
	private static class GameBoardHolder{
		/** The game board instance. */
		private static GameBoard gameBoardInstance = new GameBoard();
	}
	
	/**
	 * Gets the single instance of GameBoard.
	 *
	 * @return single instance of GameBoard
	 */
	public static GameBoard getInstance() {
		return GameBoardHolder.gameBoardInstance;
	}
	
	
	/**
	 * Gets the player from callback.
	 *
	 * @param callback the callback
	 * @return the player from callback
	 */
	public Player getPlayerFromCallback(ProtocolCallback<String> callback){
		return callbackToPlayer.get(callback);
	}
	
	/**
	 * Gets the callback to player.
	 *
	 * @return the callback to player
	 */
	public HashMap<ProtocolCallback<String>, Player> getCallbackToPlayer() {
		return callbackToPlayer;
	}

	/**
	 * Gets the rooms.
	 *
	 * @return the rooms
	 */
	public HashMap<String, Room> getRooms() {
		return rooms;
	}
	
	/**
	 * Gets a specific room.
	 *
	 * @param roomName the room name
	 * @return the room
	 */
	public Room getRoom(String roomName){
		return rooms.get(roomName);
	}
	
	/**
	 * Gets the players in room.
	 *
	 * @param room the room
	 * @return the players in room
	 */
	public ArrayList<Player> getPlayersInRoom(Room room){
		return rooms.get(room.getRoomName()).getPlayers();
	}
	
	/**
	 * Adds the to room.
	 *
	 * @param room the room
	 * @param player the player
	 * @return the boolean
	 */
	public Boolean addToRoom(String room,Player player) {
		boolean playerHasRoom = (player.getRoom() != null);
		boolean playerIsPlaying = (playerHasRoom && player.getRoom().gameIsStarted());
		boolean roomExist = (rooms.get(room)!=null);
		boolean roomRunningAGame = (roomExist && rooms.get(room).gameIsStarted());
		if (!playerIsPlaying){
			if(!roomRunningAGame){
				if(roomExist){
					rooms.get(room).addPlayer(player);
					return true;
				}
				else{
					rooms.put(room, new Room(room,new ArrayList<Player>()));
					rooms.get(room).addPlayer(player);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Adds the player.
	 *
	 * @param player the player
	 * @return the boolean
	 */
	public Boolean addPlayer(Player player){
		if(callbackToPlayer.get(player.getCallback())==null){
			callbackToPlayer.put(player.getCallback(), player);
			return true;
		}
		else
			return false;
		
	}

	
	/**
	 * List the games.
	 *
	 * @return the hash map
	 */
	public HashMap<String, Game> listTheGames(){
		return games;
	}

	/**
	 * Adds a game.
	 *
	 * @param game the game
	 */
	public void addGame(Game game) {
		games.put(game.getName(), game);
	}


	/**
	 * Gets the game.
	 *
	 * @param gameName the game name
	 * @return the game
	 */
	public Game getGame(String gameName) {
		return games.get(gameName);
	}


	/**
	 * Gets the 3 questions from database (json file).
	 * Insert the json file name to load the questions.
	 *
	 * @return the 3 questions
	 */
	public ArrayList<Question> get3Questions() {
		Scanner sc = new Scanner(System.in);
		Gson gson = new Gson();  
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/users/studs/bsc/2016/yairweis/"+sc.nextLine()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		AppData appData = gson.fromJson(br, AppData.class);
		ArrayList<Question> questionsToAdd = new ArrayList<Question>();		
		for(int i = 0; i < appData.getQuestions().length; i++){
			questionsToAdd.add(new Question(appData.getQuestions()[i].getQuestionText(),appData.getQuestions()[i].getRealAnswer()));
		}
		ArrayList<Question> threeQuestions = new ArrayList<Question>();
		for(int i = 0; i < 3; i++){
			int index = (int) Math.random()*questionsToAdd.size();
			Question question= questionsToAdd.get(index);
			threeQuestions.add(question);
			questionsToAdd.remove(index);
		}
		return threeQuestions;
	}


	/**
	 * Checks if the nickname exist already.
	 *
	 * @param nickname the nickname
	 * @return true, if nickname exist already
	 */
	public boolean nickExist(String nickname) {
		for(Player player : callbackToPlayer.values()){
			if(player.getNickname().equals(nickname))
				return true;
		}
		return false;
	}
}
