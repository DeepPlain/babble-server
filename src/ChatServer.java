import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ChatServer {
	static int noCount = 0;
	ArrayList<Room> roomList;
	HashMap<String, DataOutputStream> clients;
	private ServerSocket ServerSocket = null;
	
	public static void main(String[] args) {
		new ChatServer().start();
	}
	
	public ChatServer() {
		roomList = new ArrayList<Room>();
		clients = new HashMap<String, DataOutputStream>();
		Collections.synchronizedMap(clients);
		
		roomList.add(new Room(0,"0번","스포츠","1","2",clients));
		noCount++;
		roomList.add(new Room(1,"1번","TV톡","1","2",clients));
		noCount++;
		roomList.add(new Room(2,"2번","TV톡","1","2",clients));
		noCount++;
		roomList.add(new Room(3,"3번","TV톡","1","2",clients));
		noCount++;
		roomList.add(new Room(4,"4번","스포츠","1","2",clients));
		noCount++;
		roomList.add(new Room(5,"5번","스포츠","1","2",clients));
		noCount++;
		roomList.add(new Room(6,"6번","TV톡","1","2",clients));
		noCount++;
		roomList.add(new Room(7,"7번","스포츠","1","2",clients));
		noCount++;
		roomList.add(new Room(8,"8번","TV톡","1","2",clients));
		noCount++;
		roomList.add(new Room(9,"강준모 이상규 이창욱 김대래4643","TV톡","1","2",clients));
		noCount++;
		
	}	
	
	private void start() {
		int port = 8888;
		Socket socket = null;
		DataOutputStream output;
		DataInputStream input;
		
		try {
			ServerSocket = new ServerSocket(port);
			System.out.println("접속대기중");
			
			while(true) {
				socket = ServerSocket.accept();
				InetAddress ip = socket.getInetAddress();
				System.out.println(ip + " connected");
				
				new MultiThread(socket).start();
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
		
	class MultiThread extends Thread {
			
		Socket socket = null;
		String nick = null;
		String mac = null;
		String msg = null;
			
		DataInputStream input;
		DataOutputStream output;
		
		HashMap<String, DataOutputStream> roomPeople;
			
		public MultiThread(Socket socket/*, DataInputStream input, DataOutputStream output, String nick, HashMap<String, DataOutputStream> roomPeople*/) {
			this.socket = socket;
			/*this.input = input;
			this.output = output;
			this.nick = nick;
			this.roomPeople = roomPeople;*/
			
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
		}
			
		public void run() {
			Room room = null;
			int no;
			String title;
			String category;
			String number;
			String totalNumber;
			StringBuffer roomInfo = new StringBuffer();
			
			for(int i=0; i<roomList.size(); i++) {
				no = roomList.get(i).no;
				title = roomList.get(i).title;
				category = roomList.get(i).category;
				number = roomList.get(i).number;
				totalNumber = roomList.get(i).totalNumber;
				roomInfo.append(no + "%^" + title + "%^" + category + "%^" + number + "%^" + totalNumber + "%^");
			}
			System.out.println(roomInfo);
			try {
				output.writeUTF(roomInfo.toString());
				while(true) {
					String read = input.readUTF();
					System.out.println("111111");
					if(read != null) {
						System.out.println(read);
						StringTokenizer str = new StringTokenizer(read, "%^");
						String noStr = str.nextToken();
						String titleStr = str.nextToken();
						String categoryStr = str.nextToken();
						String what = str.nextToken();
						String totalNumberStr = str.nextToken();
						nick = str.nextToken();
						System.out.println("22222222");

					
						if(what.equals("make")) { //방 만들기
							roomPeople = new HashMap<String, DataOutputStream>();
							roomPeople.put(nick, output);
							roomList.add(room = new Room(noCount, titleStr, categoryStr, "1", totalNumberStr, roomPeople));
							System.out.println("방 생성 : " + noCount + " " + titleStr + " " + categoryStr + " " + totalNumberStr);
							noCount++;
							break;
						}
						else if(what.equals("in")){ //방 입장
							Boolean ok = true;
							Boolean in = false;
							for(int i=0; i<roomList.size(); i++) {
								if(roomList.get(i).no == Integer.parseInt(noStr)) {
									in = true;
									room = roomList.get(i);
									if(roomList.get(i).roomPeople.containsKey(nick) == true) {
										System.out.println("이미 사용하고 있는 닉네임입니다.");
										output.writeUTF("이미 사용하고 있는 닉네임입니다.");
										ok = false;
									}
									if(roomList.get(i).totalNumber.equals(roomList.get(i).number)) {
										output.writeUTF("인원이 초과되었습니다.");
										ok = false;
									}
									if(ok == true) {
										System.out.println("4444444");
										roomPeople = room.roomPeople;
										roomPeople.put(nick, output);
										room.number = String.valueOf((roomPeople.size()));
										output.writeUTF("ok");
										break;
									}	
								}
							}
							if(in == false){
								System.out.println("방 입장 오류");
								output.writeUTF("방 입장 오류" + noStr);
								ok = false;
							}
							if(ok == true) {
								System.out.println("5555555");
								break;
							}
						}	
					}
				}
					//mac = input.readUTF();
					//System.out.println("닉네임 : " + nick);
					
					//clients.put(mac, output);
				sendMsg(room.title + "%^" + room.number + "%^" + room.totalNumber);
				sendMsg(nick + "님이 채팅방에 입장하셨습니다.$& $& ");

				while(true) {
					String exitNick;
					try {
						String temp = input.readUTF();
						StringTokenizer str = new StringTokenizer(temp, "%^");
						if(str.countTokens() == 2) { //exit
							exitNick = str.nextToken();
							room.roomPeople.remove(exitNick);
							room.number = String.valueOf(room.roomPeople.size());
							if(room.roomPeople.isEmpty()) { // 인원이 0명이면
								roomList.remove(room);
								break;
							}
							sendMsg(room.title + "%^" + room.number + "%^" + room.totalNumber);
							sendMsg(exitNick + "님이 채팅방을 나가셨습니다.$& $& ");
							break;
						}
						System.out.println("read : " + temp);
						sendMsg(temp);
					} catch (IOException e) {
						room.roomPeople.remove(nick);
						room.number = String.valueOf(room.roomPeople.size());
						if(room.roomPeople.isEmpty()) { // 인원이 0명이면
							roomList.remove(room);
							break;
						}
						sendMsg(room.title + "%^" + room.number + "%^" + room.totalNumber);
						sendMsg(nick + "님이 채팅방을 나가셨습니다.$& $& ");
						break;
					}
					
				}
			}catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();	
			}	
							
		}
			
		private void sendMsg(String msg) {
			Iterator<String> it = roomPeople.keySet().iterator();
				
			while(it.hasNext()) {
				try {
					System.out.println("send : " + msg);
					String key = it.next();
					OutputStream dos = roomPeople.get(key);
					DataOutputStream output = new DataOutputStream(dos);
					output.writeUTF(msg);
				} catch(IOException e) {
					System.out.println(e);
				}
			}
		}
	}
	
	class Room {
		int no;
		String title;
		String category;
		String number;
		String totalNumber;
		HashMap<String, DataOutputStream> roomPeople;
		
		public Room(int no, String title, String category, String number, String totalNumber, HashMap<String, DataOutputStream> roomPeople) {
			this.no = no;
			this.title = title;
			this.category = category;
			this.number = number;
			this.totalNumber = totalNumber;
			this.roomPeople = roomPeople;
		}
	}
}

	
	
