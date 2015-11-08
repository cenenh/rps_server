package rps.server.ref;

public class ClientAction {
	public static final String CONNECT = "CONNECT";
	public static final String READY = "READY";
	public static final String DISCONNECT = "DISCONNECT";
	public static final String DUPLICATE_USERID = "DUPLICATE_USERID";
	public static final String NEW_USERID = "NEW_USERID";
	
	public static final String ROCK = "ROCK";
	public static final String SCISSORS = "SCISSORS";
	public static final String PAPER = "PAPER";
	public static final String GAME_START = "GAME_START";
	
	public static final String DO_RPS = "RSP_ACTION";
	public static final int ROCK_ACTION = 1;
	public static final int SCISSORS_ACTION = 2;
	public static final int PAPER_ACTION = 3;
	public static final int RPS_DRAW = -1;
	
	public static final String GAME_RESULT ="GAME_RESULT";
	public static final String WIN = "WIN";
	public static final String LOSE = "LOSE";
	public static final String DRAW = "DRAW";
	
	public static final String FINAL_GAME_RESULT = "FINAL_RESULT";
	public static final String MESSAGE_FROM_SERVER = "client_action";
	public static final String CLIENT_ID = "CLIENT_ID";
	public static final String NOW_PLAYING_CLIENTS = "NOW_PLAYING_CLIENTS";

}

