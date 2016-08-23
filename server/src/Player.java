/**
 * The Class Player.
 */
public class Player {
	
	/** The nickname. */
	private String nickname;
	
	/** The callback. */
	private ProtocolCallback<String> callback;
	
	/** The room. */
	//private GameProtocol protocol;
	private Room room;
	
	/** The score. */
	private int score;
	
	/**
	 * Instantiates a new player.
	 *
	 * @param nickname the nickname
	 * @param callback the callback
	 */
	public Player(String nickname, ProtocolCallback<String> callback){
		this.nickname=nickname;
		this.callback=callback;
		room=null;
		score=0;
	}


	/**
	 * Gets the nickname.
	 *
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Gets the callback.
	 *
	 * @return the callback
	 */
	public ProtocolCallback<String> getCallback() {
		return callback;
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Adds the score.
	 *
	 * @param score the score
	 */
	public void addScore(int score) {
		this.score += score ;
	}

	/**
	 * Gets the room.
	 *
	 * @return the room
	 */
	public Room getRoom() {
		return room;
	}

	/**
	 * Sets the nickname.
	 *
	 * @param nickname the new nickname
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Sets the callback.
	 *
	 * @param callback the new callback
	 */
	public void setCallback(ProtocolCallback<String> callback) {
		this.callback = callback;
	}

	/**
	 * Sets the room.
	 *
	 * @param room the new room
	 */
	public void setRoom(Room room) {
		this.room = room;
	}
	
}