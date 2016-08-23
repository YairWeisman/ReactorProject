/**
 * The Class Game.
 */
public abstract class Game {
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Analyze the answers and score the players in the room accordingly.
	 *
	 * @param room the room
	 */
	public abstract void analyze(Room room);
}
