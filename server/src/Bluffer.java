/**
 * The Bluffer game Class.
 */
public class Bluffer extends Game{
	
	/** The name. */
	private final String name;
	
	/**
	 * Instantiates a new bluffer.
	 */
	public Bluffer(){
		this.name = "Bluffer";
	}
	
	/* 
	 * @see Game#analyze(Room)
	 */
	@Override
	public void analyze(Room room) {
		String choice = "";
		Player player;
		for(int i=0; i<room.getPlayers().size();i++){
			player=room.getPlayers().get(i);
			choice = room.getChoices().get(room.getPlayers().get(i));
			if(choice.equals(room.getRealAnswerNumber())){
				player.addScore(10);
			}
			else{
				String answerString = room.getShuffledAnswers().get(room.getChoices().get(player));
				room.getAnswers().get(answerString).addScore(5);
			}
		}
	}
	
	/* 
	 * @see Game#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
}
