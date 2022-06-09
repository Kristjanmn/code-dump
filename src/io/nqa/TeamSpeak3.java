package io.nqa;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.stefan1200.jts3serverquery.*;
import io.nqa.Sys.*;

@SuppressWarnings("static-access")

public class TeamSpeak3 implements TeamspeakActionListener {
	public Sys system;
	JTS3ServerQuery query = new JTS3ServerQuery();
	private String serverAddress = "ts.nqa.io";
	private int queryPort = 10011;
	private int serverPort = 9987;
	private String username = "IONQuery";
	private String password = "1GrDnEH0";
	public boolean isAlive;
	private boolean keepAlive = true;
	private boolean shutdown;
	private int keepAliveRate = 200;	//200
	private String displayName = "NQA Bot";
	private int defaultChannel = 2648; 				//Group A
	private int guestServerGroup = 527;
	private int defaultServerGroup = 567;			//527 = Guest
	
	private ArrayList<String> disallowedUsernames = new ArrayList<String>();
	
	boolean keepAliveConnected = false;
	
	// Groups and UIDs
	private int groupOverlord = 806;
	
	public void teamspeakActionPerformed(String eventType, HashMap<String, String> eventInfo) {
		try {
//			println(eventType + " " + eventInfo);	// test
			try {
				if(Integer.parseInt(eventInfo.get("targetmode")) == 1) {
					HashMap<String, String> dataClient = query.getInfo(query.INFOMODE_CLIENTINFO, Integer.parseInt(eventInfo.get("target")));
					println(Color.Client + eventInfo.get("invokername") + Color.Default + " -> " + Color.Client + dataClient.get("client_nickname") + Color.Default + ": " + eventInfo.get("msg"));
				} else if(Integer.parseInt(eventInfo.get("invokerid")) != query.getCurrentQueryClientID()) {
					println(query.getInfo(query.INFOMODE_CLIENTINFO, Integer.parseInt(eventInfo.get("invokerid"))).get("client_nickname") + ": " + eventInfo.get("msg"));
				}
			} catch(NumberFormatException e) {
//				e.printStackTrace();
			}
			if(eventType.contentEquals("notifytextmessage")) {
				boolean queryMsg = false;
				try {
					if(Integer.parseInt(eventInfo.get("invokerid")) == query.getCurrentQueryClientID()) {
						queryMsg = true;
					}
				} catch(NumberFormatException e) {
					println("ts3 57");
					e.printStackTrace();
				}
				int firstSpace = eventInfo.get("msg").indexOf(" ");
				String subString = eventInfo.get("msg").substring(firstSpace+1);
				boolean space = firstSpace != -1;
				int invoker = Integer.parseInt(eventInfo.get("invokerid"));
				if(!queryMsg) {
					StringBuffer stringBuffer = new StringBuffer();		// TODO: Recommended to replace with StringBuilder
					switch(eventInfo.get("msg")) {
					case "!help":
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Type !commands for list of commands\n!info for information");
						break;
					case "!commands":
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Commands here");
						break;
					case "!info":
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "This is the Not Quite Alright teamspeak server.");
						break;
					case "!clientlist":
						Vector<HashMap<String, String>> dataClientList = query.getList(query.LISTMODE_CLIENTLIST, "-info,-time");
						for(HashMap<String, String> hashMap : dataClientList) {
							if(stringBuffer.length() > 0) {
								stringBuffer.append(", ");
							}
							stringBuffer.append(hashMap.get("client_nickname"));
						}
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Client List (Only client names displayed): " + stringBuffer.toString());
						break;
					case "!channellist":
						Vector<HashMap<String, String>> dataChannelList = query.getList(query.LISTMODE_CHANNELLIST);
						for(HashMap<String, String> hashMap : dataChannelList) {
							if(stringBuffer.length() > 0) {
								stringBuffer.append(", ");
							}
							stringBuffer.append(hashMap.get("channel_name"));
						}
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Channel list (only channel names displayed): " + stringBuffer.toString());
						break;
					case "!serverinfo":
						HashMap<String, String> dataServerInfo = query.getInfo(query.INFOMODE_SERVERINFO, 0);
						query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Server info: " + dataServerInfo.get("virtualserver_name"));
						break;
					case "!status":
						if(space) {
							String serverStatus = "";
							String teamSpeakStatus = "";
							if(system.server.isAlive) {
								serverStatus = "[color=green]Online[/color]";
							} else {
								serverStatus = "[color=red]Offline[/color]";
							}
							if(isAlive) {
								teamSpeakStatus = "[color=green]Online[/color]";
							}
							if(subString.equalsIgnoreCase("system")) {
								query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "System info:\nServer: " + serverStatus + "\nTeamSpeak: " + teamSpeakStatus);
							} else if(subString.equalsIgnoreCase("server")) {
								query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Server: " + serverStatus);
							} else if(subString.equalsIgnoreCase("teamspeak")) {
								query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "System info:\nTeamSpeak: poop");
							} else {
								query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Invalid parameter " + subString);
							}
						} else {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "!status lacks a parameter");
						}
						break;
					case "!start":
						if(space) {
							if(subString.equalsIgnoreCase("server")) {
								query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Starting server");
								system.server.initialize();
							}
						} else {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "!start lacks a parameter");
						}
						break;
					case "!shutdown":
						if(subString.equalsIgnoreCase("system")) {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "system shutdown initiated");
							system.shutdown();
						} else if(subString.equalsIgnoreCase("server")) {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "server shutdown initiated");
							system.server.shutdown();
						} else if(subString.equalsIgnoreCase("teamspeak") || subString.equalsIgnoreCase("ts")) {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "TeamSpeak shutdown initiated");
							shutdown();
						} else if(subString.isEmpty()) {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "!shutdown lacks a parameter!");
						}
						break;
					case "!say":
						if(!subString.isBlank()) {
							try {
								msgTeamSpeakToServer(invoker, subString);
							} catch(NumberFormatException e) {
								e.printStackTrace();
							}
						} else {
							query.sendTextMessage(invoker, query.TEXTMESSAGE_TARGET_CLIENT, "Message is empty");
						}
						break;
					case "!announce":
						if(!subString.isBlank()) {
//							system.announce(Color.Default + subString);
						}

					// BOT

					case "!bot":
						println("!bot");
						if(space) {
							if(subString.equalsIgnoreCase("start")) {
								startBot();
							} //else stopBot();
						}
						break;

					case "!register":
						if(space) {
							if(subString.equalsIgnoreCase("bot")) {
								// Start new bot, registered by client
								// This is quite hard with this old structure.
								// I should update this before adding bot registration from client side.
							}
						}
						break;
					}
				}
			}			// Use client loop for now and deal with this shit later
//			if(eventType.contentEquals("notifycliententerview")) onClientJoin(Integer.parseInt(eventInfo.get("clid")));
//			if(eventType.contentEquals("notifyclientleftview")) {				// Check for disconnects only?
//				HashMap<String, String> dataClient = query.getInfo(query.INFOMODE_CLIENTINFO, Integer.parseInt(eventInfo.get("clid")));
//				Data.TeamspeakClient client = null;
//				String reason = "";
//				for(int i = 0; i < system.data.getTeamspeakClients().size(); i++) {
//					if(system.data.getTeamspeakClients().get(i).clid == Integer.parseInt(eventInfo.get("clid"))) client = system.data.getTeamspeakClients().get(i);
//				}
//				int reasonId = Integer.parseInt(eventInfo.get("reasonid"));
//				switch(reasonId) {
//				default:
//					println("173 - client leave - reason id: " + reasonId);
//					break;
//				case 3:
//					for(int i = 0; i < system.data.getTeamspeakClients().size(); i++) {
//						if(system.data.getTeamspeakClients().get(i).clid == Integer.parseInt(eventInfo.get("clid"))) client = system.data.getTeamspeakClients().get(i);
//					}
//					println(Color.Client + client.nickname + Color.Default + " lost connection");
//					break;
//				case 5:
//					if(eventInfo.get("reasonmsg").length() > 0) reason = (" reason: '" + eventInfo.get("reasonmsg") + "'");
//					println(Color.Client + eventInfo.get("invokername") + Color.Default + " kicked " + Color.Client + client.nickname + Color.Default + reason);
//					break;
//				case 6:
//					if(eventInfo.get("reasonmsg").length() > 0) reason = (" reason: '" + eventInfo.get("reasonmsg") + "'");
//					println(Color.Client + eventInfo.get("invokername") + Color.Default + " banned " + Color.Client + client.nickname + Color.Default + " for " + eventInfo.get("bantime") + "s" + reason);
//					break;
//				case 8:
//					if(eventInfo.get("reasonmsg").length() > 0) reason = (" '" + eventInfo.get("reasonmsg") + "'");
//					println(Color.Client + client.nickname + Color.Default + " disconnected" + reason);
//					break;
//				}
//			}
		} catch(TS3ServerQueryException e) {
			println("ts3 195");
			e.printStackTrace();
		}
	}
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!system.date.isEqual(LocalDate.now())) {
				system.newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.TeamSpeak + " TeamSpeak: " + Color.Default + t + Color.Input);
			system.appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	private void log(String str) {
		system.log("Teamspeak:	" + str);
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			keepAlive();
		}
	};
	Thread thread = new Thread(runnable);
	
	public TeamSpeak3(Sys system) {
		this.system = system;
	}
	
	public void initialize() {
		println(Color.Success + "Initializing");
		
		// Fill in disallowed username list
		disallowedUsernames.add("admin");
		disallowedUsernames.add("server");
		disallowedUsernames.add("owner");
		disallowedUsernames.add("host");
		disallowedUsernames.add("bot");

		//connect(serverAddress, queryPort);	// TODO: Disabled only for testing bot locally outside server

		// Bot
		startBot();
	}
	
	private void connect(String serverAddress, int queryPort) {
		try {
			query.connectTS3Query(serverAddress, queryPort);
			if(query.isConnected()) {
				keepAliveConnected = true;
				println(Color.Success + "Connected to server " + Color.Error + serverAddress + ":" + queryPort);
				log("Connected to server " + serverAddress + ":" + queryPort);
			}
			login(username, password);
		} catch(Exception e) {
			println(e.getClass().getName());
			if(e.getClass() == java.lang.NullPointerException.class) println(Color.Error + "Server host network may have temporarily blocked you");
			if(e.getClass() == java.net.UnknownHostException.class) println(Color.Error + "Connection to '" + serverAddress + ":" + queryPort + "' failed, check connection! (" + e.getClass().getName() + ")");
			else e.printStackTrace();
			shutdown();
		}
	}
	
	private void login(String queryUsername, String queryPassword) {
		try {
			query.loginTS3(queryUsername, queryPassword);
			println(Color.Success + "Logged in as " + Color.Client + queryUsername);
			log("Logged in as " + queryUsername);
			joinServer(serverPort);
		} catch(TS3ServerQueryException e) {
			if(e.getErrorID() == 520) {
				println(Color.Error + "Login failed: Invalid username or password");
				log("Login failed: Invalid credentials");
			}
			if(e.getErrorID() == 3329) {
				println(Color.Error + "Login failed: You are banned");
				log("Login failed: Banned");
			}
			else e.printStackTrace();
			shutdown();
		}
	}
	
	private void joinServer(int serverPort) {
		try {
			query.selectVirtualServer(serverPort, true);
			HashMap<String, String> info = query.getInfo(query.INFOMODE_SERVERINFO, 0);
//			query.moveClient(query.getCurrentQueryClientID(), defaultChannel, "");
			query.setTeamspeakActionListener(this);
			query.addEventNotify(query.EVENT_MODE_SERVER, 0);
			query.addEventNotify(query.EVENT_MODE_TEXTSERVER, 0);
			query.addEventNotify(query.EVENT_MODE_TEXTCHANNEL, 0);
			query.addEventNotify(query.EVENT_MODE_TEXTPRIVATE, 0);
			if(displayName != query.getCurrentQueryClientName()) setQueryName(displayName);		//it lies!!! >:(
			println(Color.Success + "Joined " + Color.Server + info.get("virtualserver_name") + Color.Success + " as " + Color.Client + query.getCurrentQueryClientName());
			isAlive = true;
			thread.setName("Thread-TeamSpeak");
			thread.start();
		} catch(TS3ServerQueryException e) {
			println("kaka: " + e.getClass().getName());
			if(e.getErrorID() == 768) println(Color.Error + e.getErrorMessage() + " " + defaultChannel);
			if(e.getErrorID() == 2568) println(Color.Error + "Insufficient client permissions " + e.getFailedPermissionID());
			else e.printStackTrace();
			log("TS3ServerQueryException\n" + e.getMessage());
			system.restart(3);
		} catch(IllegalThreadStateException e) {
			println(e.getMessage());
			//log("IllegalThreadStateException\n" + e.getMessage());
			system.restart(3);
		}
	}
	
	/**
	 * Change query client's name
	 * 
	 * @param name
	 */
	public void setQueryName(String name) {
		if(!query.getCurrentQueryClientName().equals(name)) try {
			query.setDisplayName(name);
		} catch(TS3ServerQueryException e) {
			if(e.getErrorID() == 513) println(Color.Error + "Error while setting query name to " + Color.Client + name + Color.Error + ": " + e.getErrorMessage());
			else e.printStackTrace();
		}
	}
	
	private void keepAlive() {
		/*if(!query.isConnected()) {
			println(Color.Error + "Lost connection to " + serverAddress);
			connect(serverAddress, queryPort);			//try to reestablish connection
		}*/
			
//			try {
//				system.restart(3);
//			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
//					| BadPaddingException | InterruptedException | IOException e1) {
//				e1.printStackTrace();
//				println(Color.Critical + "Failed to restart TeamSpeak after connection was lost");
//				keepAlive = false;
//			}
		while(keepAlive) {
			if(!query.isConnected()) {
				println(Color.Error + "Lost connection to " + serverAddress);
				connect(serverAddress, queryPort);			//try to reestablish connection
			} else {
				try {
					clientLoop();
					thread.sleep(keepAliveRate);
				} catch(InterruptedException e) {
					if(e.getClass() == java.lang.InterruptedException.class) println(e.getMessage());	//thread interrupted
					else e.printStackTrace();
				}
			}
		}
		println(Color.Warning + "keepAlive thread terminating!");
		shutdown();
	}
	
	/**
	 * Executed when a client joins the server.
	 * 
	 * @param clientId
	 */
	private void onClientJoin(int clientId) {			// check if there are any clients connected thru clientloop before this function is started
		try {
			println("382 - onClientJoin");
			HashMap<String, String> dataClient = query.getInfo(query.INFOMODE_CLIENTINFO, clientId);
			println("New client " + Color.Client + dataClient.get("client_nickname"));	//
			if(dataClient.get("client_servergroups").contains(Integer.toString(groupOverlord))) {
//				query.sendTextMessage(query.getCurrentQueryClientServerID(), query.TEXTMESSAGE_TARGET_VIRTUALSERVER, "All hail OVERLORD " + dataClient.get("client_nickname") + "!");
			}
//			query.sendTextMessage(clientId, query.TEXTMESSAGE_TARGET_CLIENT, "Welcome to the " + query.getInfo(query.INFOMODE_SERVERINFO, 0).get("virtualserver_name") + " server!");
		} catch(TS3ServerQueryException e) {
			if(e.getErrorID() != 512) e.printStackTrace();
			if(e.getErrorID() != 512) println("kaka TS3 ln-391");
			println("client joined - TS3 ln-392");
		}
	}
	
	/**
	 * Loop to check if a client has joined.
	 * Exists only because ServerEvent does not work.
	 * Ineffective, but currently only method.
	 */
	private void clientLoop() {		// Make it also detect if a client has left, keeping a list of connected clients including more than just clid
		if(!query.isConnected()) println(Color.Critical + "a oops has happaned! ln351");
		List<String> cuidBuffer = new ArrayList<String>();
		List<Integer> connectedClid = new ArrayList<Integer>();			// make it into an array of clients instead of clid to streamline checking, prolly more taxing
		List<TeamspeakClient> clientBuffer = new ArrayList<TeamspeakClient>();			//never used
		for(TeamspeakClient client : system.data.getTeamspeakClients()/*int i = 0; i < system.data.getTeamspeakClients().size(); i++*/) {
			//clientBuffer.add(system.data.getTeamspeakClients().get(i));
			cuidBuffer.add(client.getCuid());
			if(client.isConnected()) {
				connectedClid.add(client.getClid());
			}
		}
		List<TeamspeakClient> connectedClientBuffer = new ArrayList<TeamspeakClient>();
		try {
			//Connected clients
			Vector<HashMap<String, String>> dataClientList = query.getList(query.LISTMODE_CLIENTLIST);			// Currently connected clients
			for(HashMap<String, String> hashMap : dataClientList) {
				if(Integer.parseInt(hashMap.get("client_type")) == 0) {											// Check if is regular client, not Query
					HashMap<String, String> clientInfo;
					try {		//to narrow down the errors to certain id and stuff????
						clientInfo = query.getInfo(query.INFOMODE_CLIENTINFO, Integer.parseInt(hashMap.get("clid")));		// cuid?
						TeamspeakClient client = new TeamspeakClient(clientInfo.get("client_unique_identifier"), Integer.parseInt(clientInfo.get("client_database_id")), LocalDateTime.MIN);
						client.setCid(Integer.parseInt(clientInfo.get("cid")));
						client.setClid(Integer.parseInt(hashMap.get("clid")));
						client.setIp(clientInfo.get("connection_client_ip"));
						//println(clientInfo.get("client_country"));
						client.setNickname(clientInfo.get("client_nickname"));
						client.setLastSeen(LocalDateTime.now());
						connectedClientBuffer.add(client);
					} catch (TS3ServerQueryException e) {
						if(e.getErrorID() == 512)
						e.printStackTrace();
					}
				}
			}
		} catch(TS3ServerQueryException e) {		// make also catch java.net.SocketTimeoutException and reconnect when connection closed
			if(e.getErrorID() == 512) println(e.getMessage());
			if(e.getErrorID() == 2568) println("kaka TS3 ln-362");
			println("ts3ln336");
			println(Color.Critical + e.getErrorMessage());
			e.printStackTrace();
			log(e.getErrorMessage());
		} catch(Exception e) {		//for other exceptions??
			if(e.getClass().equals(java.net.SocketTimeoutException.class) || 	// SocketTimeoutException
					e.getClass().equals(java.net.SocketException.class)) {		// SocketException
				println(e.getMessage());
				println("Attempting TeamSpeak service restart");
				log("SocketException caught, attempting restart");
				system.restart(3);
			}
		}
		
		// Go thru clients who are connected to TeamSpeak server
		for(TeamspeakClient client : connectedClientBuffer) {
			
			// Check if client uses an disallowed username
			for(String name : disallowedUsernames) {
				if(client.getNickname().toLowerCase().contains(name.toLowerCase())) {
					//TODO: Add exceptions for allowing certain names or just change the system around a bit
					// Kick user
					try {
						query.kickClient(client.getClid(), false, "Username must not contain '" + name + "', change your name and contact Kristjan if want an exception");
						println("Kicked " + Color.Client + client.getNickname() + Color.Default + " for using disallowed username " + Color.Client + client.getNickname());
						log("Kicked '" + client.getNickname() + "' for using disallowed username '" + client.getNickname() + "'");
					} catch(TS3ServerQueryException e) {
						e.printStackTrace();
					}
				}
			}
			
			// Client has previously connected
			if(cuidBuffer.contains(client.getCuid())) {
				TeamspeakClient updateClient = system.data.getTeamspeakClients().get(cuidBuffer.indexOf(client.getCuid()));
				// Client was connected during last scan
				if(updateClient.isConnected() && connectedClid.contains(updateClient.getClid())) {
					updateClient.setCid(client.getCid());
//					updateClient.setClid(client.getClid());
					if(!updateClient.getNickname().contentEquals(client.getNickname())) {
						println(Color.User + updateClient.getNickname() + Color.Default + " has changed their name to " + Color.Client + client.getNickname());
						log("'" + updateClient.getNickname() + "' has changed their name to '" + client.getNickname() + "'");
						updateClient.setNickname(client.getNickname());
					}
					// Client's IP address has changed
					if(!updateClient.getIp().contentEquals(client.getIp())) {
						// It doesn't seem to display IP changed message anymore.. --solved?
						println(Color.User + updateClient.getNickname() + Color.Default + "'s new IP address is " + client.getIp());
						log(updateClient.getNickname() + "'s new IP address is " + client.getIp());
						updateClient.setIp(client.getIp());
					}
					updateClient.setLastSeen(client.getLastSeen());
				} else {		// Client was not connected during last scan
					updateClient.setClid(client.getClid());
					updateClient.setIsConnected(true);
					updateClient.setIp(client.getIp());
					println(Color.Client + client.getNickname() + Color.Default + " connected from "/* + clientInfo.get("client_country" + "("*/ + client.getIp());
					log(client.getNickname() + " connected from " + client.getIp());
				}
				system.data.updateTeamspeakClient(updateClient);
				if(connectedClid.contains(client.getClid())) {
					connectedClid.remove(connectedClid.indexOf(client.getClid()));
				}
				
				// Make sure client has server group. Should probably be executed only when joining?
				try {
					HashMap<String, String> dataClient = query.getInfo(query.INFOMODE_CLIENTINFO, client.getClid());
					// Client has Guest server group
					if(dataClient.get("client_servergroups").equals(Integer.toString(guestServerGroup))) {		// Group ID has to be String in this case
						println(client.getNickname() + " has no group assigned, function disabled on TeamSpeak3.java ln510");
						/*println(client.getNickname() + " has no group assigned, giving default group");
						query.doCommand("servergroupaddclient sgid=" + defaultServerGroup + " cldbid=" + client.getCdbid());
						query.sendTextMessage(client.getClid(), query.TEXTMESSAGE_TARGET_CLIENT,
								"Welcome to the [b]" + query.getInfo(query.INFOMODE_SERVERINFO, 0).get("virtualserver_name") + "[/b] server!\n"
								+ "Since it seems to be your first time here, please click the subscribe channels button twice (it's the button with an eye icon on top bar) "
								+ "to reveal other people's location within this server.\n"
								+ "Or simply join the [b]Group A[/b] channel :)\n"
								+ "[i][b]P.S.[/b] I'm an automated bot and may not be able to answer you (yet)! Report any problems to [b]Kristjan[/b][/i]");*/
					}
				} catch(TS3ServerQueryException e) {
					if(e.getErrorID() != 512) {
						System.out.println(e.getErrorMessage() + "\n\n" + e.getMessage());
						e.printStackTrace();
						log(e.getMessage());
					}
					if(e.getErrorID() == 512) println("invalid client ID, client most likely disconnected");
					if(e.getMessage() == "Connection reset") {
						System.out.println(e.getMessage() + "\nTeamspeak connection reset, attempting service restart");
						log(e.getMessage());
						system.restart(3);
					}
				}
				
				//if(updateClient.nickname.contains("onald") || updateClient.nickname.contains("query"))
					//println("doing");
					//query.doCommand("banclient clid=145");
					/*try {
						query.kickClient(updateClient.clid, false, "illegal name");
					} catch (TS3ServerQueryException e) {
						e.printStackTrace();
					}*/
			} else {		// New client
				// For some reason it adds already existing client as new client. check not only clid but also cuid
				TeamspeakClient newClient = new TeamspeakClient(client.getCuid(), client.getCdbid(), LocalDateTime.now());
				newClient.setCid(client.getCid());
				newClient.setClid(client.getClid());
				newClient.setNickname(client.getNickname());
				newClient.setIp(client.getIp());
				newClient.setLastSeen(LocalDateTime.now());
				newClient.setIsConnected(true);
				
				system.data.updateTeamspeakClient(newClient);
				println("New client " + Color.Client + client.getNickname() + Color.Default + " connected from "/* + clientInfo.get("client_country" + "("*/ + client.getIp());
				log("New client " + client.getNickname() + " connected from " + client.getIp());
			}
		}
		
		if(!connectedClid.isEmpty()) {
			// An client has disconnected
			for(Integer connClid : connectedClid/*int i = 0; i < connectedClid.size(); i++*/) {
				for(TeamspeakClient client : system.data.getTeamspeakClients()/*int j = 0; j < system.data.getTeamspeakClients().size(); j++*/) {
					if(client.getClid() == connClid && client.isConnected()) {
						TeamspeakClient updateClient = client;
						updateClient.setIsConnected(false);
						system.data.updateTeamspeakClient(updateClient);
						println(Color.Client + client.getNickname() + Color.Default + " disconnected");
						log(client.getNickname() + " disconnected");
					}
				}
			}
		}
	}
	
	public void msgSystemToServer(String msg) {
		try {
			query.sendTextMessage(query.getCurrentQueryClientServerID(), query.TEXTMESSAGE_TARGET_VIRTUALSERVER, msg);
			println(msg);
		} catch (TS3ServerQueryException e) {
			e.printStackTrace();
		}
	}
	
	public void msgToClient(int clid, String msg) {
		TeamspeakClient client = null;
		for(int i = 0; i < system.data.getTeamspeakClients().size(); i++) {
			if(system.data.getTeamspeakClients().get(i).getClid() == clid) client = system.data.getTeamspeakClients().get(i);
		}
		if(client.isConnected())
			try {
				query.sendTextMessage(clid, query.TEXTMESSAGE_TARGET_CLIENT, msg);
			} catch (TS3ServerQueryException e) {
				e.printStackTrace();
			}
		else msgOffline(clid, 1, msg);
	}
	
	private void msgOffline(int recipientId, int senderId, String msg) {
		// check if client is connected and then send this message to them
		if(false) {
			try {
				query.sendTextMessage(recipientId, query.TEXTMESSAGE_TARGET_CLIENT, msg);
			} catch (TS3ServerQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void msgTeamSpeakToServer(int clientId, String msg) {
		if(!msg.isBlank()) {
			HashMap<String, String> clientInfo;
			String clientName = "";
			try {
				clientInfo = query.getInfo(query.INFOMODE_CLIENTINFO, clientId);
				clientName = clientInfo.get("client_nickname");
			} catch (TS3ServerQueryException e) {
				e.printStackTrace();
			}
			system.server.msgToServer(clientName, msg);
		}
	}
	
	private void terminateConnection() {
		try {
			query.removeTeamspeakActionListener();
			query.closeTS3Connection();
			isAlive = false;
			println(Color.Warning + "Connection terminated");
		} catch(TS3ServerQueryException e) {
			println(e.getClass().getName());
			println("errmsg " + e.getErrorMessage());
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		if(!shutdown) {				// To prevent function being called more than once
			shutdown = true;
			keepAlive = false;
			thread.interrupt();
			if(!thread.isAlive()) println(Color.Critical + "TeamSpeak service failure");
			terminateConnection();
			//thread.interrupt();		called twice??
			println(Color.Warning + "Shutdown");
			log("Shutdown");
		}
	}
	
	
	// TODO: Fully implement
	TeamSpeakBotHandler botHandler;
	
	public void startBot() {
		if(botHandler == null) {
			println("starting bot");
			//botHandler = new TeamSpeakBotHandler(system, "192.168.1.188", 25639, "VQRY-3GLO-ZBDE-AF4Q-SC50-FS01");		// PC
			botHandler = new TeamSpeakBotHandler(system, "192.168.1.150", 25639, "03SA-7GYE-IJT8-JPQ2-Y99F-086H");	// Server
			//new Thread(botHandler).start();
		}
		//TS3ClientQuery ts3ClientQuery = new TS3ClientQuery();
	}
	
	public void stopBot() {
		if(botHandler != null) {
			println("stoping bot");
			botHandler.quit();
			botHandler = null;
		}
	}
	
	private class Server {
		int id;
		int guid;
		String ip;
		int port;
		String login;
		String password;
	}
}
