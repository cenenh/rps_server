package rps.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import rps.server.manage.Client;
import rps.server.ref.ClientAction;

public class ServerBehavior implements Runnable {

	ObjectInputStream objectInStream;
	ObjectOutputStream objectOutStream;
	Client client;
	StringTokenizer msg;
	HashMap<Object, Object> request;
	HashMap<String, String> response;

	public ServerBehavior() {
		request = new HashMap<>();
		response = new HashMap<>();
	}

	public ServerBehavior(Client client) {
		request = new HashMap<>();
		response = new HashMap<>();
		this.client = client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
	
	public String checkRPS(int rps){
		String rpsAction = null;
		if (rps == ClientAction.ROCK_ACTION){
			rpsAction = "바위";
		}
		else if (rps == ClientAction.SCISSORS_ACTION){
			rpsAction = "가위";
		}
		else if (rps == ClientAction.PAPER_ACTION){
			rpsAction = "보";
		}
		return rpsAction; 
	}

	@Override
	public void run() {
		try {
			objectInStream = new ObjectInputStream(client.getClientSocket().getInputStream());
			objectOutStream = new ObjectOutputStream(client.getClientSocket().getOutputStream());
			client.setObjectInStream(objectInStream);
			client.setObjectOutStream(objectOutStream);
			
			while ((request = (HashMap<Object, Object>) objectInStream.readObject()) != null) {
				Object client_action = request.get("client_action");
				Object client_id = request.get("client_id");
				client.setUserID(client_id.toString());
				System.out.println("Request from IP : " + client.getClientSocket().getRemoteSocketAddress().toString()
						+ ", ID : " + client.getUserID());

				String client_address = (String) request.get("client_address");
			
				//첫 접속일 경우
				if (client_action.equals(ClientAction.CONNECT)) {
					if(Server.ifDuplicateUserID(client)){
						int index = Server.findClientIndex(client_id.toString());
						Server.addLog("User ID 중복!, "+ client_id + " 에서 다른 User ID로 수정됩니다.");
						String temp = (String) client_id;
						client_id = client_id.toString() + "("+index+")";
						client.setUserID(client_id.toString());
						Server.modifyClientUserID(client);
						Server.addLog(temp + " => " + client.getUserID()+" 로 수정되었습니다.");
						HashMap<Object, Object> rename_message = new HashMap<>();
						rename_message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.DUPLICATE_USERID);
						rename_message.put(ClientAction.NEW_USERID, client_id);
						uniCast(rename_message, client);
					}
					
					HashMap<Object, Object> message = new HashMap<>();
					ArrayList<String> client_id_list = new ArrayList<String>();
					System.out.println(Server.clients.size()+" Clients in Server Now..");
					for (int i = 0; i < Server.clients.size(); i++) {
						System.out.println(i + "." + Server.clients.get(i).getUserID());
						client_id_list.add(Server.clients.get(i).getUserID());
					}
					Server.addLog("현재 client 수 = " + Server.clients.size());
					message.put("client_action", "FIRST_CONNECT");
					message.put("client_id_list", client_id_list);
					uniCast(message, client); //첫 접속 client에게 unicast로  현재 USERLIST를 보내줌.
					
					HashMap<Object, Object> firstConnectClient = new HashMap<>();
					firstConnectClient.put("client_action", "NEW_CONNECT");
					firstConnectClient.put("client_id", client.getUserID());
					broadCast(firstConnectClient); //기존 접속 client들에게는 broadcast로 새로 접속한 유저를 알려줌. 
				} 
				//READY 일 경우,
				else if (client_action.equals(ClientAction.READY)) {
					client.setReady(true);
					client.setIfDoRPS(false);
					HashMap<Object, Object> message = new HashMap<>();
					System.out.println("client IP : " + client_address + ", client ID : " + client_id + ", client do "
							+ client_action);
					Server.addLog("client IP : " + client_address + ", client ID : " + client_id + ", client do "
							+ client_action);
					message.put("client_id", (String) client_id);
					message.put("client_action", (String) client_action);
					System.out.println("will broadcast " + message);
					broadCast(message);		
					// 모든 user가 ready 했는지 check
					if(Server.clients.size() == Server.getReadyCount() && Server.clients.size() > 1){
						//server에 접속한 client 수와 ready 수가 같고, server에 접속한 client 수가 1명 이상일때 게임을 시작.
						HashMap<Object, Object> gameStartMessage = new HashMap<>();
						gameStartMessage.put("client_action", ClientAction.GAME_START);
						System.out.println("ALL USER READY, You can Start Game");
						startGame(); //ComputeServer Thread 시작..
						broadCast(gameStartMessage);
						Server.addLog("All Clients are READY, GAME START!");
						Server.addLog("모든 클라이언트가 READY!, 게임을 시작합니다.");
					}
					else{
						System.out.println("NOT YET");
					}	
				} 
				//RPS 일 경우,
				else if(client_action.equals(ClientAction.DO_RPS)){
					System.out.println(request);
					int client_do_rps = (int) request.get("client_rps_action");
					client.setRps(client_do_rps);
					client.setIfDoRPS(true);
					String rpsAction = checkRPS(client.getRps());
					Server.addLog(client.getUserID() + "가 " + rpsAction + "을(를) 냈습니다.");
				}
				//Client disconnect 요청
				else if(client_action.equals(ClientAction.DISCONNECT)){
					String disconnect_user_id = (String) request.get("client_id");
					System.out.println("will delete "+client.getUserID());
					Server.addLog(client.getClientSocket().getRemoteSocketAddress().toString()+" 에서 " + client.getUserID()+" 님이 나가셨습니다.");
					client.getObjectInStream().close();
					client.getObjectOutStream().close();
					Server.clients.remove(client);
					System.out.println(Server.clients.size());
					Server.addLog("현재 client 수 = " + Server.clients.size());
					HashMap<Object, Object> message = new HashMap<Object, Object>();
					message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.DISCONNECT);
					message.put(ClientAction.DISCONNECT, disconnect_user_id);
					ServerBehavior.broadCast(message);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			
		} 
	}
	public void startGame(){
		ComputeServer compute = new ComputeServer();
		Thread computeThread = new Thread(compute);
		Server.setPlaying(true);
		System.out.println(Server.getPlaying());
		System.out.println("GAME START!!");
		computeThread.start();
	}
	
	public static void broadCast(HashMap<Object, Object> message) {
		for (Client client : Server.clients) {
			try {
				//System.out.println(client.getUserID() + ", "
				//		+ client.getClientSocket().getRemoteSocketAddress().toString() + ", " + message);
				client.getObjectOutStream().writeObject(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("broadcast 성공");
		}
	}
	
	public static void uniCast(HashMap<Object, Object> message, Client client){
		System.out.println("unicast to client IP : "+client.getClientSocket().getRemoteSocketAddress()+" , "
				+ "client ID : "+client.getUserID() + " , message = " + message);
		try {
			client.getObjectOutStream().writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("unicast 성공");
	}


}
