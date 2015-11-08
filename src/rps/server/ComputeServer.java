package rps.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import rps.server.Server;
import rps.server.manage.Client;
import rps.server.ref.ClientAction;

public class ComputeServer extends Thread{
	
	int sleepCount;
	boolean afterSleepCount;
	ArrayList <Client> playingClients;
	
	public void sendGameResult(Client client){
		HashMap<Object, Object> message = new HashMap<>();
		message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.GAME_RESULT);
		
		if(client.getGameResult().equals(ClientAction.WIN)){
			message.put(ClientAction.GAME_RESULT, ClientAction.WIN);
		}
		else if(client.getGameResult().equals(ClientAction.LOSE)){
			message.put(ClientAction.GAME_RESULT, ClientAction.LOSE);
		}
		else if(client.getGameResult().equals(ClientAction.DRAW)){
			message.put(ClientAction.GAME_RESULT, ClientAction.DRAW);
		}
		
		try {
			client.getObjectOutStream().writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getDoRPSCount(ArrayList<Client> playingClients){
		int do_rps_count = 0;
		for(int i = 0; i < playingClients.size(); i++){
			if(playingClients.get(i).getIfDoRPS()){
				do_rps_count++;
			}
		}
		return do_rps_count;
	} 
	
	public boolean ifDraw(ArrayList<Client> clients, int r, int p, int s){
		if (clients.size() == r)
			return true;
		else if (clients.size() == p)
			return true;
		else if (clients.size() == s)
			return true;
		return false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		// client가 어떤것을 냈는지 체크해야함. 30초동안 쉬면서..
		// 처음에 winner에 clients를 모두 shallow copy.
		playingClients = new ArrayList<Client>(Server.clients); 
		afterSleepCount = false;
		long startTime;
		long endTime;
		while(Server.getPlaying()){
			sleepCount = 0;
			while(true){
				startTime = System.currentTimeMillis();
				try {
					int do_rps_count = getDoRPSCount(playingClients);
					if (playingClients.size() == do_rps_count) {
						break;
					} else {
						endTime = System.currentTimeMillis();
						long timeDiff = startTime - endTime;
						sleepCount++;
						System.out.println("sleep Count = "+sleepCount);
						Thread.sleep(1000 - timeDiff);
					}
					if (sleepCount >= 30) {
						afterSleepCount = true;
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(afterSleepCount){
				//30초가 지나서 break;
				System.out.println("30초 지나서 종료됨..");
				Server.addLog("30초동안 가위/바위/보를 내지 않은 client가 있습니다.");
			}else{
				//모두 rps를 do해서 break;
				System.out.println("그전에 종료됨..");
				Server.addLog("모든 USER가 가위/바위/보 를 냈습니다.");
			}
			
			int rock = 0;
			int scissors = 0;
			int paper = 0;
			int lose_action = 0;
			boolean ifDraw = false;
			
			for (Client client : playingClients){
				if(client.getRps() == ClientAction.ROCK_ACTION){
					rock++;
				}
				else if(client.getRps() == ClientAction.SCISSORS_ACTION){
					scissors++;
				}
				else if(client.getRps() == ClientAction.PAPER_ACTION){
					paper++;
				} // player가의 가위/바위/보 몇개인지 체크..
			}

			if(rock!=0 && scissors!=0 && paper==0){
				//주먹과 가위만 있을때
				//가위 = lose.
				lose_action = ClientAction.SCISSORS_ACTION;
			}
			else if(rock!=0 && scissors==0 && paper!=0){
				//주먹과 보만 있을때
				//바위 = lose.
				lose_action = ClientAction.ROCK_ACTION; 
			}
			else if(rock==0 && scissors!=0 && paper!=0){
				//가위와 보만 있을때
				lose_action = ClientAction.PAPER_ACTION; 
			}
			else if(rock !=0 && scissors != 0 && paper !=0 || ifDraw(playingClients, rock, paper, scissors)){
				//셋 다 있을때...draw
				ifDraw = true;
				lose_action = ClientAction.RPS_DRAW;
			}
			
			int now_playing_clients = 0;
			
			Iterator<Client> iterator = playingClients.iterator();
			
			while(iterator.hasNext()){
				Client client = iterator.next();
				//가위바위보를 낸 client
				if (client.getIfDoRPS()){
					System.out.println(client.getUserID() + " : " + client.getRps());
					if(!ifDraw){ // draw가 아닐때..
						if (client.getRps() == lose_action) {
							client.setGameResult(ClientAction.LOSE);
							sendGameResult(client);
							Server.addLog(client.getUserID() + " : " + "LOSE!");
							iterator.remove(); // lose 한 유저는 playingClients에서 삭제..
						} else if (client.getRps() != lose_action) {
							client.setGameResult(ClientAction.WIN);
							sendGameResult(client);
							Server.addLog(client.getUserID() + " : " + "WIN!");
							now_playing_clients++;
						}
					}
					else{ // if draw...
						client.setGameResult(ClientAction.DRAW);
						sendGameResult(client);
						Server.addLog(client.getUserID() + " : " + "DRAW!");
						now_playing_clients++;
					}
				}
				//가위바위보를 내지 않는 client
				else{ 
					client.setGameResult(ClientAction.LOSE);
					Server.addLog(client.getUserID() + " : " + "LOSE [30초 안에 가위/바위/보를 내지 않음]");
					sendGameResult(client);
					iterator.remove(); //lose 한 유저는 playingClients에서 삭제..
				}
			} //while, iterator, client
			
			System.out.println("현재 playing clients..");
			Server.addLog("현재 playing clients..");
			for(int i = 0; i < playingClients.size();i++){
				playingClients.get(i).setIfDoRPS(false); //재경기를 해야하므로 초기화
				playingClients.get(i).setRps(0); //재경기를 해야하므로 초기화
				System.out.print(playingClients.get(i).getUserID()+", ");
				Server.addLog(i+1 + "번째 client, IP : " + playingClients.get(i).getClientSocket().getRemoteSocketAddress() + "ID : " + playingClients.get(i).getUserID());
			}			
			System.out.println("\n");
			System.out.println("client_live_num = ? " + now_playing_clients);
			
			//모든 client에게 이번판 이긴 user의 id_list 발송, 위너들의 모임..
			HashMap<Object, Object> now_playing_clients_message = new HashMap<Object, Object>();
			ArrayList<String> now_playing_clients_ids = new ArrayList<String>();
			for(Client client : playingClients){
				now_playing_clients_ids.add(client.getUserID());
			}
			now_playing_clients_message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.NOW_PLAYING_CLIENTS);
			now_playing_clients_message.put(ClientAction.NOW_PLAYING_CLIENTS, now_playing_clients_ids);
			ServerBehavior.broadCast(now_playing_clients_message);
			
			//남은 user가 한명이면....
			if(now_playing_clients == 1){
				//GAME END..
				Server.addLog("게임이 종료되었습니다.");
				Server.setPlaying(false);
				Client winnerClient = playingClients.get(0);
				HashMap<Object, Object> message = new HashMap<>();
				message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.FINAL_GAME_RESULT);
				message.put("client_id", winnerClient.getUserID());
				message.put("client_live_num", now_playing_clients);
				Server.addLog("최종승자 : " + winnerClient.getUserID());
				ServerBehavior.broadCast(message);
				Server.initializeClients(); //서버에 접속한 client들의 정보를 초기화. ready, 가위바위보 낸것.
			}else if (now_playing_clients == 0){
				HashMap<Object, Object> message = new HashMap<>();
				Server.addLog("게임이 종료되었습니다.");
				Server.setPlaying(false);
				message.put(ClientAction.MESSAGE_FROM_SERVER, ClientAction.FINAL_GAME_RESULT);
				message.put("client_id", "NONE!");
				message.put("client_live_num", now_playing_clients);
				Server.addLog("최종승자 : 아무도 없습니다.");
				ServerBehavior.broadCast(message);
				Server.initializeClients(); //서버에 접속한 client들의 정보를 초기화. ready, 가위바위보 낸것.
			}
		} //while(Server.getPlaying())
	} //run
}
