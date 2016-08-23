import java.io.*;
import java.net.*;
import java.util.ArrayList;
/**
 * The Class GameProtocol.
 */
public class GameProtocol implements ServerProtocol<String>  {
	
	/** The true if the connection should be closed. */
	private boolean _shouldClose=false;
	
	/* 
	 * Gets a message, process it and sends the answer and react accordingly.
	 * 
	 * @param msg The message to process.
	 * @param callback The callback to send the message to the right client
	 */
	@Override
	public void processMessage(String msg, ProtocolCallback<String> callback) throws IOException {
		GameBoard gameBoard = GameBoard.getInstance();
		String type = msg.toString().split(" ")[0];
		switch(type) {
			case "NICK":
				String nickname = msg.toString().split(" ")[1];
				boolean nickOccupied = gameBoard.nickExist(nickname);
				if(nickOccupied)
					callback.sendMessage("SYSMSG NICK REJECTED This nickname is already in use.");
				Player player = new Player(nickname, callback);
				boolean ValidNick = gameBoard.addPlayer(player);
				if(!ValidNick)
					callback.sendMessage("SYSMSG NICK REJECTED You already have a nickname.");
				else callback.sendMessage("SYSMSG NICK ACCEPTED Your nickname is: "+nickname+" .");
				break;
			case "JOIN":
				String room = msg.toString().split(" ")[1];
				Player playerr = gameBoard.getPlayerFromCallback(callback);
				boolean isInRoom = gameBoard.addToRoom(room, playerr);
				if (isInRoom){
					callback.sendMessage("SYSMSG JOIN ACCEPTED You've joined "+room+" successfuly.");
					playerr.setRoom(gameBoard.getRoom(room));
				}
				else callback.sendMessage("SYSMSG JOIN REJECTED An Error accured while trying to join room "+room+".");
				break;
			case "MSG":
				String messageToSend = msg.toString().substring(4);
				ArrayList<Player> playersInTheRoom = gameBoard.getPlayersInRoom(gameBoard.getPlayerFromCallback(callback).getRoom());
				for(Player somePlayer : playersInTheRoom){
					somePlayer.getCallback().sendMessage("USERMSG "+messageToSend);
				}
				break;
			case "LISTGAMES":
				String ans = "The games avialble are:";
				for(Game game : gameBoard.listTheGames().values()){
					ans = ans + "\n"+ "\t" + game.getName();
				}
				callback.sendMessage("USERMSG "+ans);
				break;
			case "STARTGAME":
				String gameToStart = msg.toString().split(" ")[1];
				if(gameBoard.getGame(gameToStart)==null){
					callback.sendMessage("SYSMSG STARTGAME REJECTED This game is not supported.");
					break;
				}
				Player playerStartedTheGame = gameBoard.getPlayerFromCallback(callback);
				Room roomToStartTheGame = playerStartedTheGame.getRoom();
				roomToStartTheGame.startGame(gameToStart);
				break;
			case "TEXTRESP":
				Player player1 = gameBoard.getCallbackToPlayer().get(callback);
				String playerRoom1 = player1.getRoom().getRoomName();
				gameBoard.getRoom(playerRoom1).insertPlayerAnswer(msg.toString().substring(9).toLowerCase(),player1);
				break;
			case "SELECTRESP":
				Player player2 = gameBoard.getCallbackToPlayer().get(callback);
				String playerRoom2 = player2.getRoom().getRoomName();
				gameBoard.getRoom(playerRoom2).insertPlayerChoice(player2, msg.toString().substring(11));
				break;
			case "QUIT":
				Player player3 = gameBoard.getCallbackToPlayer().get(callback);
				if(player3.getRoom()!=null && player3.getRoom().gameIsStarted()){
					callback.sendMessage("SYSMSG QUIT REJECTED An Error accured while trying to quit during an active game.");					
				}
				
				else{
					callback.sendMessage("SYSMSG QUIT ACCEPTED.");
					gameBoard.getCallbackToPlayer().remove(player3.getCallback());
					connectionTerminated();
					//player3.getCallback().getHandler().getClientSocket().close();
				}
				break;
			default: 
				callback.sendMessage("SYSMSG "+type+" UNIDENTIFIED This command in not valid.");
				break;
		}
	}
	
	/* (non-Javadoc)
	 * Checks if the message is a "quit" message
	 * @param msg The message.
	 * @return true if it's a "quit" message.
	 */
	@Override
	public boolean isEnd(String msg) {
		return msg.equals("QUIT");
	}
	
	/**
	 * Sets the protocol to closing mode.
	 */
	public void connectionTerminated(){
		_shouldClose=true;
	}
	
	/**
	 * Checks if the connection should be close.
	 *
	 * @return true, if the connection should be closed.
	 */
	public boolean shouldClose(){
		return _shouldClose;
	}

}
