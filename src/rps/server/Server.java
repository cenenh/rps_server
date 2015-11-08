package rps.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import rps.server.manage.Client;

public class Server extends JFrame{
	
	private static boolean playing;
	static JFrame jframe;
	static JTextArea textArea;
	static ArrayList<Client> clients;
	
	public Server(){
		super("SERVER");
		JScrollPane scrollPane = new JScrollPane();
		textArea = new JTextArea();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textArea.setBackground(new Color(255,255,255));
		setSize(1300, 500);
		textArea.setFont(new Font("굴림",Font.BOLD,15));
		textArea.setEditable(false);
		setVisible(true);
		DefaultCaret ServerCaret = (DefaultCaret)textArea.getCaret();
		ServerCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(textArea);
	}
	
	public static void setPlaying(boolean ifPlaying){
		playing = ifPlaying;
	}
	
	public static boolean getPlaying(){
		return playing;
	}
	
	public static void addLog(String str){
		textArea.append(str+"\n");
	}
	
	public static boolean ifDuplicateUserID(Client input_client){
		for (Client client : clients){
			if(!input_client.getClientSocket().getRemoteSocketAddress().equals(client.getClientSocket().getRemoteSocketAddress())){
				if(client.getUserID().equals(input_client.getUserID())){
					return true;
				}
			}
		}
		return false;
	}
	
	public static void modifyClientUserID(Client cl){
		for (Client client : clients){
			if(client.getClientSocket().getRemoteSocketAddress().equals(cl.getClientSocket().getRemoteSocketAddress())){
				client.setUserID(cl.getUserID());
			}
		}
	}
	
	public static int findClientIndex(String client_id){
		int index = 0;
		for (int i = 0; i < Server.clients.size(); i++){
			if(Server.clients.get(i).getUserID().equals(client_id)){
				index++;
			}
		}
		return index-1;
	}
	
	public static void initializeClients(){
		for(Client client : clients){
			client.setIfDoRPS(false);
			client.setReady(false);
			client.setRps(0);
			client.setGameResult("NONE");
		}
	}
	
	public static int getReadyCount(){
		int clientReadyCount = 0;
		for(Client user : clients){
			if(user.getReady()){
				clientReadyCount++;
			}	
		}
		return clientReadyCount;
	}
	
	public static int getDoRPSCount(){
		int clientDoRPSCount = 0;
		for(Client user : clients){
			if(user.getIfDoRPS()){
				clientDoRPSCount++;
			}
		}
		return clientDoRPSCount;
	}
	
	public static void startGame(){
		System.out.println("Game Start!");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server();
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		clients = new ArrayList<Client>();
		Client client;
		
		ServerBehavior serverBehavior;
		int port=8888;
		int clientNumber = 0;
		System.out.println("Server is Running...");
		addLog("Server is Running...\n");
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		while(true){
			try {
				clientSocket = new Socket();
				client = new Client();
				
				// client가 connect 눌렀을시 ....
				clientSocket = serverSocket.accept();
				client.setClientSocket(clientSocket);
				serverBehavior = new ServerBehavior(client);
				System.out.println(client.getClientSocket());
				clientNumber++;
				clients.add(client);
				
				System.out.println("현 client 수 : " + clientNumber);
				System.out.println(clientSocket.getRemoteSocketAddress());
				addLog(clientSocket.getRemoteSocketAddress().toString()+" 에서 접속!.\n");
				
				Thread thread = new Thread(serverBehavior);
				thread.start();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				System.out.println("finally");
			}
		}
	}
}
