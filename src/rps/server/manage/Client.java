package rps.server.manage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
	
	private String userID; 
	private Socket clientSocket;
	private ObjectInputStream objectInStream;
	private ObjectOutputStream objectOutStream;
	private int rps;
	private boolean ifDoRPS;
	private boolean isReady;
	private String gameResult;
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	public ObjectInputStream getObjectInStream() {
		return objectInStream;
	}
	public void setObjectInStream(ObjectInputStream objectInStream) {
		this.objectInStream = objectInStream;
	}
	public ObjectOutputStream getObjectOutStream() {
		return objectOutStream;
	}
	public void setObjectOutStream(ObjectOutputStream objectOutStream) {
		this.objectOutStream = objectOutStream;
	}
	public int getRps() {
		return rps;
	}
	public void setRps(int rps) {
		this.rps = rps;
	}
	public boolean getReady(){
		return isReady;
	}
	public void setReady(boolean isReady){
		this.isReady = isReady;
	}
	public boolean getIfDoRPS(){
		return ifDoRPS;
	}
	public void setIfDoRPS(boolean ifDoRPS){
		this.ifDoRPS = ifDoRPS;
	}
	public String getGameResult(){
		return gameResult;
	}
	public void setGameResult(String gameResult){
		this.gameResult = gameResult;
	}
	
}
