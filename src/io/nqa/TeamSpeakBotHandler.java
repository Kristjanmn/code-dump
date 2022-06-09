package io.nqa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import io.nqa.Sys.Color;

public class TeamSpeakBotHandler implements Runnable {
	private final Sys system;
	private Socket bot;
	private PrintWriter output;
	private BufferedReader input;
	private final String address;
	private final int port;
	private final String apiKey;
	private COMMAND command = COMMAND.NONE;
	private String message;
	
	/***** Bot variables *****/
	private LocalDateTime lastMsg;
	private int schandlerid;
	private int bot_clid;				// Different for each schandler
	private int bot_cid;				// Different for each schandler
	private boolean isAuthenticated;
	private String botNameExtension = "[BOT]";
	private String botName = "";		// init blank

	private List<TeamSpeakBot> bots = new ArrayList<TeamSpeakBot>();
	private List<ClientList> clientLists = new ArrayList<>();
	private List<TeamspeakGroup> serverGroups = new ArrayList<>();
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!system.date.isEqual(LocalDate.now())) {
				system.newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.Server + " Bot Handler: " + Color.Default + t + Color.Input);
			system.appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	private void log(String str) {
		system.log("TS3_BotHandler:	" + str);
	}
	
	public TeamSpeakBotHandler(Sys system, String address, int port, String apiKey) {
		this.system = system;
		this.address = address;
		this.port = port;
		this.apiKey = apiKey;
		try {
			println("Starting bot on " + address + ":" + port);
			
			bot = new Socket(address, port);
			//bot = new Socket("localhost", 25639);
			input = new BufferedReader(new InputStreamReader(bot.getInputStream()));
			output = new PrintWriter(bot.getOutputStream(), true);
			auth(apiKey);
			//auth("VQRY-3GLO-ZBDE-AF4Q-SC50-FS01");		// PC
			thread.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			// Set bot name
			while(true) {
				message = input.readLine();
				if(message == null) {
					// Bot has died
					println("Bot is dead.");
				}
				if(!message.isBlank()) processMessage();
				if(lastMsg.until(LocalDateTime.now(), ChronoUnit.MINUTES) > 1) {
					serverConnectionHandlerList();
					//whoAmI();
					println("5 minutes have passed");
					if(botName.isBlank()) clientList();
					else if(!botName.endsWith(" " + botNameExtension)) changeName();
				}
			}
		} catch(Exception e) {
			/*if(!e.getClass().equals(NullPointerException.class))*/ e.printStackTrace();
		}
	}
	Thread thread = new Thread(this);
	
	/***** Process Message temporary variables *****/
	/***** I know that some of the variable names are duplicate, I'm trying to match their names with incoming information to make it easier to handle. *****/
	/***** N.B. Most of these variables are here temporarily and will be removed as functions get finished. *****/
	int id;
	String msg;
	int clid;
	int cid;
	int notify_schandlerid;			// schandlerid of last incoming message, used to reply
	String notify_status;
	int notify_error;
	int notify_clid;
	int notify_talk_status;
	int notify_talk_isreceivedwhisper;
	int notify_talk_clid;			// Talking client's ID
	String notify_cluid;
	String notify_client_unique_identifier;
	int notify_tcldbid;				// Target dbid
	String notify_tname;			// Target name
	int notify_fcldbid;				// Invoker dbid
	String notify_fname;			// Invoker name
	String notify_message;
	long notify_timestamp;
	int notify_ctid;
	int notify_reasonid;			// 0 = self | 1 = somebody else
	int notify_count;				// Amount of entries in returned list
	
	// Client
	int notify_client_input_muted;
	int notify_client_output_muted;
	int notify_client_outputonly_muted;
	int notify_client_input_hardware;
	int notify_client_output_hardware;
	// "client_meta_data" is sent in notifycliententerview
	int notify_client_is_recording;
	int notify_client_database_id;
	int notify_client_channel_group_id;
	// "client_servergroups" separated with comma
	int notify_client_away;
	String notify_client_away_message;
	int notify_client_type;			// 0 = Client | 1 = Query
	// client_flag_avatar
	int notify_client_talk_power;
	int notify_client_talk_request;
	String notify_client_talk_request_msg;
	String notify_client_description;
	int notify_client_is_talker;
	int notify_client_is_priority_speaker;
	int notify_client_unread_messages;
	String notify_client_nickname_phonetic;
	int notify_client_needed_serverquery_view_power;
	int notify_client_icon_id;
	int notify_client_is_channel_commander;
	String notify_client_country;
	int notify_client_channel_group_inherited_channel_id;
	// client_badges
	// client_myteamspeak_id;
	// client_integrations
	// client_myteamspeak_avatar
	// client_signed_badges
	
	String notify_client_version;		// e.g. 3.5.6 [Build: 1606312422]
	String notify_client_platform;		// Windows / Linux / Android
	String notify_client_login_name; 	// Don't know it's use, is not related to my program
	long notify_client_created;
	long notify_client_lastconnected;
	int notify_client_totalconnections;
	long notify_client_month_bytes_uploaded;
	long notify_client_month_bytes_downloaded;
	long notify_client_total_bytes_uploaded;
	long notify_client_total_bytes_downloaded;
	
	// Message
	int notify_msgid;					// Message ID
	String notify_subject;
	int notify_flag_read;
	
	// Ban
	int notify_banid;
	String notify_ip;
	String notify_name;
	String notify_uid;
	String notify_mytsid;			// Not sure if this is right format, should be
	String notify_lastnickname;
	long notify_created;
	int notify_targetmode;			// 1 - private | 2 - channel | 3 - server
	int notify_target;
	int notify_duration;			// How long the ban lasts, probably in seconds?
	int notify_invokerid;
	String notify_invokername;
	int notify_invokercldbid;
	String notify_invokeruid;
	String notify_reason;
	int notify_enforcements;		// No idea what it means
	String notify_msg;
	
	// Connection
	String notify_connection_client_ip;
	int notify_connection_client_port;
	float notify_connection_client2server_packetloss_speech;
	float notify_connection_client2server_packetloss_keepalive;
	float notify_connection_client2server_packetloss_control;
	float notify_connection_client2server_packetloss_total;
	int notify_connection_idle_time;
	
	// Channel
	int notify_channel_cid;			// Channel ID, input is cid
	int notify_channel_pid;			// Channel parent ID, input is pid
	int notify_channel_order;
	String notify_channel_name;
	int notify_channel_flag_are_subscribed;
	int notify_channel_total_clients;	// input is total_clients
	int notify_cpid;

	// Server
	String notify_virtualserver_welcomemessage;
	int notify_virtualserver_maxclients;
	int notify_virtualserver_clientsonline;
	int notify_virtualserver_channelsonline;
	long notify_virtualserver_uptime;
	String notify_virtualserver_hostmessage;
	int notify_virtualserver_hostmessage_mode;
	int notify_virtualserver_flag_password;
	int notify_virtualserver_default_channel_admin_group;
	long notify_virtualserver_max_download_total_bandwidth;
	long notify_virtualserver_max_upload_total_bandwidth;
	int notify_virtualserver_complain_autoban_count;
	int notify_virtualserver_complain_autoban_time;
	int notify_virtualserver_complain_remove_time;
	int notify_virtualserver_min_clients_in_channel_before_forced_silence;		// TODO: find out more
	int notify_virtualserver_antiflood_points_tick_reduce;
	int notify_virtualserver_antiflood_points_needed_command_block;
	int notify_virtualserver_antiflood_points_needed_ip_block;
	int notify_virtualserver_client_connections;
	int notify_virtualserver_query_client_connections;
	int notify_virtualserver_queryclientsonline;
	long notify_virtualserver_download_quota;
	long notify_virtualserver_upload_quota;
	long notify_virtualserver_month_bytes_downloaded;
	long notify_virtualserver_month_bytes_uploaded;
	long notify_virtualserver_total_bytes_downloaded;
	long notify_virtualserver_total_bytes_uploaded;
	int notify_virtualserver_port;
	int notify_virtualserver_autostart;
	int notify_virtualserver_machine_id;							// Most likely don't need it
	int notify_virtualserver_needed_identity_security_level;
	int notify_virtualserver_log_client;
	int notify_virtualserver_log_query;
	int notify_virtualserver_log_channel;
	int notify_virtualserver_log_permissions;
	int notify_virtualserver_log_filetransfer;
	long notify_virtualserver_min_client_version;
	int notify_virtualserver_reserved_slots;
	float notify_virtualserver_total_packetloss_speech;				// Not sure if this variable is correct, should be percentage
	float notify_virtualserver_total_packetloss_keepalive;
	float notify_virtualserver_total_packetloss_control;
	float notify_virtualserver_total_packetloss_total;
	float notify_virtualserver_total_ping;
	int notify_virtualserver_weblist_enabled;
	long notify_virtualserver_min_android_version;
	long notify_virtualserver_min_ios_version;
	int notify_virtualserver_antiflood_points_needed_plugin_block;
	
	// No documentation
	int notify_cgid;				// Channel group ID
	int notify_cid;					// Channel ID
	int notify_cgi;					// UNKNOWN - value seems to be same as cid
	int notify_cfid;
	String notify_reasonmsg;
	String notify_client_nickname;
	int notify_permid;				// Permission ID
	int notify_permvalue;			// Permission Value
	int notify_sgid;				// Server group ID
	int notify_type;
	int notify_iconid;
	int notify_savedb;				// notifyservergrouplist
	int notify_sortid;				// Server group order
	String notify_namemode;			// Unknown
	int notify_n_modifyp;			// Group modify power - Don't know if they are granted or required
	int notify_n_member_addp;		// Member add power
	int notify_n_member_removep;	// Member remove power

	// Command booleans
	boolean get_schandlerlist;
	
	private void processMessage() {
		println(message);
		if(message.isBlank()) {		// Is already check above
			println("Incoming message is blank");
			return;
		}
		if(message.equalsIgnoreCase("TS3 Client") || 
				message.startsWith("Welcome") || 
				message.startsWith("Use the")) return; // Not going to write the rest of this line, me no likey quotes inside String.
		
		if(msgNext().equalsIgnoreCase("selected")) {
			msgUpdate();
			this.schandlerid = msgVarUp_int();
			println("set schandler to " + schandlerid);
			if(command.equals(COMMAND.SERVERCONNECTIONHANDLERLIST)) command = COMMAND.NONE;
			return;
		}
		
		String originalMsg = message;
		
		/***** Handle notify events *****/
		// TODO: Figure out a way to handle all those list methods like clients, complaints, bans, etc.

		if(msgNext().startsWith("notify") || msgNext().equalsIgnoreCase("channellist") || msgNext().equalsIgnoreCase("channellistfinished")) {
			String notifyEvent = msgNext();		// Saves space - doesn't need the first msgUpdate() in every event
			msgUpdate();
			// Define if message should be logged, main use is to disable logging for some messages
			// such as "error" response without being an actual error
			boolean logMessage = true;

			if(notifyEvent.equalsIgnoreCase("notifytalkstatuschange")) {					// Talk status changed
				logMessage = false;
				msgUpdate();	// schandlerid
				notify_talk_status = msgVarUp_int();										// 0 = not talking | 1 = talking
				notify_talk_isreceivedwhisper = msgVarUp_int();
				notify_talk_clid = msgVarUp_int();
				// TODO: Handle this information.
			}

			if(notifyEvent.equalsIgnoreCase("notifymessage")) {							// Message
				msgUpdate();	// schandlerid
				notify_msgid = msgVarUp_int();
				notify_cluid = msgVarUp();
				notify_subject = msgVarUp();
				notify_message = msgVarUp();
				notify_timestamp = msgVarUp_long();
			}

			if(notifyEvent.equalsIgnoreCase("notifymessagelist")) {						// Message list
				// make a message array with custom messages class
				msgUpdate();	// schandlerid
				// while loop checking if there is any more left
				notify_msgid = msgVarUp_int();
				notify_cluid = msgVarUp();
				notify_subject = msgVarUp();
				notify_timestamp = msgVarUp_long();
				notify_flag_read = msgVarUp_int();
			}

			if(notifyEvent.equalsIgnoreCase("notifycomplainlist")) {						// Complain list
				msgUpdate();	// schandlerid
				notify_tcldbid = msgVarUp_int();								// Target dbid
				notify_tname = msgVarUp();										// Target name
				notify_fcldbid = msgVarUp_int();								// Invoker dbid
				notify_fname = msgVarUp();										// Invoker name
				notify_message = msgVarUp();
				notify_timestamp = msgVarUp_long();
				// TODO: Handle this information.
			}

			if(notifyEvent.equalsIgnoreCase("notifybanlist")) {							// Ban list
				msgUpdate();	// schandlerid
				notify_count = Integer.parseInt(msgVarUp());					// Amount of entries in list
				for(int i = 0; i < notify_count; i++) {							// TODO: Add them into a list to handle them properly
					notify_banid = msgVarUp_int();
					notify_ip = msgVarUp();
					notify_name = msgVarUp();
					notify_uid = msgVarUp();
					notify_mytsid = msgVarUp();
					notify_lastnickname = msgVarUp();
					notify_created = msgVarUp_long();
					notify_duration = msgVarUp_int();
					notify_invokername = msgVarUp();
					notify_invokercldbid = msgVarUp_int();
					notify_invokeruid = msgVarUp();
					notify_reason = msgVarUp();
					notify_enforcements = msgVarUp_int();
					// TODO: Add into array from here.
					// Falls into a loop
				}
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientmoved")) {						// Client moved
				println(originalMsg);
				msgUpdate();	// schandlerid
				// notifyclientmoved schandlerid=1 ctid=3821 reasonid=0 clid=48736
				notify_ctid = msgVarUp_int();			// Target channel
				notify_reasonid = msgVarUp_int();		// 0 = switched | 1 = moved | 4 = kicked from channel
				if(notify_reasonid == 1) {
					// notifyclientmoved schandlerid=1 ctid=3072 reasonid=1 invokerid=51643 invokername=Kristjan invokeruid=7k\/3pNm1UZ2K3erv7KTQ2+WAh+o= clid=48736
					notify_invokerid = msgVarUp_int();
					notify_invokername = msgVarUp();
					notify_invokeruid = msgVarUp();
				}
				notify_clid = msgVarUp_int();
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientleftview")) {					// Client left view
				notify_schandlerid = msgVarUp_int();
				notify_cfid = msgVarUp_int();
				notify_ctid = msgVarUp_int();
				notify_reasonid = msgVarUp_int();		// 3 = connection lost
				notify_reasonmsg = msgVarUp();
				notify_clid = msgVarUp_int();

				for(ClientList clientList : clientLists) {
					if(clientList.getSchandlerid() == notify_schandlerid) {
						for(TeamspeakClient client : clientList.getClients()) {
							if(client.getClid() == notify_clid) {
								clientList.removeClient(client);
								break;
							}
						}
					}
				}
			}

			if(notifyEvent.equalsIgnoreCase("notifycliententerview")) {					// Client enter view
				// Example: notifycliententerview	schandlerid=1					cfid=0 							ctid=2613 						reasonid=2
				// clid=48736						client_unique_identifier=kNxYc1W\/ha41SSWV+FTroE93LRc= 			client_nickname=Stakemal		client_input_muted=0
				// client_output_muted=0			client_outputonly_muted=0 		client_input_hardware=0 		client_output_hardware=0 		client_meta_data
				// client_is_recording=0			client_database_id=6966 		client_channel_group_id=177 	client_servergroups=567,806 	client_away=0
				// client_away_message				client_type=0 					client_flag_avatar 				client_talk_power=25 			client_talk_request=0
				// client_talk_request_msg			client_description				client_is_talker=0 				client_is_priority_speaker=0 	client_unread_messages=0
				// client_nickname_phonetic			client_needed_serverquery_view_power=0							client_icon_id=0 				client_is_channel_commander=0
				// client_countryclient_channel_group_inherited_channel_id=2613 	client_badges=Overwolf=0
				// client_myteamspeak_id 			client_integrations 			client_myteamspeak_avatar 		client_signed_badges
				notify_schandlerid = msgVarUp_int();
				notify_cfid = msgVarUp_int();				// From channel
				notify_ctid = msgVarUp_int();				// To channel, this if connected
				notify_reasonid = msgVarUp_int();
				notify_clid = msgVarUp_int();
				notify_client_unique_identifier = msgVarUp();
				notify_client_nickname = msgVarUp();
				notify_client_input_muted = msgVarUp_int();
				notify_client_output_muted = msgVarUp_int();
				notify_client_outputonly_muted = msgVarUp_int();
				notify_client_input_hardware = msgVarUp_int();
				notify_client_output_hardware = msgVarUp_int();
				msgUpdate();	// client_meta_data		- String?
				notify_client_is_recording = msgVarUp_int();
				notify_client_database_id = msgVarUp_int();
				notify_client_channel_group_id = msgVarUp_int();
				//println("enter view server groups" + msgVarUp_int());		Need to make it read for an array, and not just one integer.
				//msgUpdate();	// client_server_groups
				List<TeamspeakGroup> notify_client_server_groups = msgGroups(msgVarUp());
				notify_client_away = msgVarUp_int();
				notify_client_away_message = msgVarUp();
				notify_client_type = msgVarUp_int();
				msgUpdate();	// client_flag_avatar
				notify_client_talk_power = msgVarUp_int();
				notify_client_talk_request = msgVarUp_int();
				notify_client_talk_request_msg = msgVarUp();
				notify_client_description = msgVarUp();
				notify_client_is_talker = msgVarUp_int();
				notify_client_is_priority_speaker = msgVarUp_int();
				notify_client_unread_messages = msgVarUp_int();
				notify_client_nickname_phonetic = msgVarUp();
				notify_client_needed_serverquery_view_power = msgVarUp_int();
				notify_client_icon_id = msgVarUp_int();;
				notify_client_is_channel_commander = msgVarUp_int();
				notify_client_country = msgVarUp();
				notify_client_channel_group_inherited_channel_id = msgVarUp_int();
				msgUpdate();	// client_badges
				msgUpdate();	// client_myteamspeak_id
				msgUpdate();	// client_integrations
				msgUpdate();	// client_myteamspeak_avatar
				msgUpdate();	// client_signed_badges

				// Log if client is regular client.
				if(notify_client_type == 0) {
					log(notify_client_nickname + " joined");
					log(originalMsg);
					// Add client to client list
					TeamspeakClient newClient = new TeamspeakClient(notify_client_unique_identifier, notify_client_database_id, LocalDateTime.now());
					newClient.setCid(notify_ctid);
					newClient.setClid(notify_clid);
					newClient.setNickname(notify_client_nickname);
					// IP not added currently
					newClient.setLastSeen(LocalDateTime.now());
					newClient.setIsConnected(true);		// obviously
					for(ClientList clientList : clientLists) {
						if(clientList.getSchandlerid() == notify_schandlerid) {
							clientList.addClient(newClient);
						}
					}
				} else logMessage = false;

				/* Add client to server group if they do not have it. */
				int guestServerGroup = 527;
				int defaultServerGroup = 567;
				//println(Color.Error + notify_client_server_groups.size());
				if(notify_client_server_groups.size() == 1 && notify_client_type == 0) {
					for(TeamspeakGroup group : notify_client_server_groups) {
						//println(Color.Error + group.getGroupId());
						if(group.getGroupId() == guestServerGroup) setServerGroup(notify_client_database_id, defaultServerGroup);
						sendTextMessage(notify_schandlerid, 1, notify_clid, msgReplaceSpaces("Welcome to Not Quite Alright server! ;-)"));
						sendTextMessage(notify_schandlerid, 1, notify_clid, msgReplaceSpaces("There is no point in replying to me, as I am not programmed to respond yet!"));
					}
				}

				// SANNA
				/*int sanna_dbid = 6537;
				if(notify_client_database_id == sanna_dbid) {
					println(Color.Critical + "STARTING SANNA");
					setServerGroup(sanna_dbid, 815);
					clientPoke(notify_schandlerid, notify_clid, "Hurr durr emotionally unstable :')");
					clientPoke(notify_schandlerid, notify_clid, "I made this server group for you.");
					clientPoke(notify_schandlerid, notify_clid, "Sorry it took so long, didn't feel much like doing anything and could not find any better icon.");
					clientPoke(notify_schandlerid, notify_clid, "I hope you like it ;-)");
					sanna_dbid = -1;	//disable
				}*/
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientpoke")) {						// Client poke
				notify_schandlerid = msgVarUp_int();
				notify_invokerid = msgVarUp_int();
				notify_invokername = msgVarUp();
				notify_invokeruid = msgVarUp();
				notify_msg = msgVarUp();
				// TODO: Handle the poke.

				clientPoke(notify_schandlerid, notify_invokerid, "Stop poking me ye horrible old butt!");
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientchatclosed")) {					// Client chat closed
				msgUpdate();	// schandlerid
				notify_clid = msgVarUp_int();
				notify_cluid = msgVarUp();
				// TODO: Probably don't need to do anything with it.
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientchatcomposing")) {				// Client chat composing
				// This event also triggers when receiving private message from closed window
				msgUpdate();	// schandlerid
				notify_clid = msgVarUp_int();
				notify_cluid = msgVarUp();
				// TODO: Probably don't need to do anything with it.
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientupdate")) {						// Client update
				TeamspeakClient client = new TeamspeakClient();		// init temporary client
				notify_schandlerid = msgVarUp_int();
				notify_clid = msgVarUp_int();
				for(ClientList clientList : clientLists) {
					if(clientList.getSchandlerid() == notify_schandlerid) {
						client = clientList.getClientByClid(notify_clid);
					}
				}
				/* Client has changed their name */
				if(msgNext().startsWith("client_nickname")) {
					notify_client_nickname = msgVarUp();
					client.setNickname(notify_client_nickname);
				}
				/* Client's description has been changed */
				else if(msgNext().startsWith("client_description")) {
					notify_client_description = msgVarUp();
					notify_invokerid = msgVarUp_int();
					notify_invokername = msgVarUp();
					notify_invokeruid = msgVarUp();
					client.setDescription(notify_client_description);
				} else if(msgNext().startsWith("client_signed_badges")) {
					// Dunno what it sends
				} else if(msgNext().startsWith("client_servergroups")) {
					// Client servergroup has changed, is triggered when a client is given or revoked a server group.
					// Returns just a list of servergroups assigned to client
					// Example: notifyclientupdated schandlerid=1 clid=48736 client_servergroups=567,789,806
					// update client's list of server groups and make sure they do not have default guest group
					//client.setServerGroups();
				}
				else {
					notify_client_version = msgVarUp();
					notify_client_platform = msgVarUp();
					notify_client_login_name = msgVarUp();
					notify_client_created = msgVarUp_long();
					notify_client_lastconnected = msgVarUp_long();
					notify_client_totalconnections = msgVarUp_int();
					notify_client_month_bytes_uploaded = msgVarUp_long();
					notify_client_month_bytes_downloaded = msgVarUp_long();
					notify_client_total_bytes_uploaded = msgVarUp_long();
					notify_client_total_bytes_downloaded = msgVarUp_long();
				}
				// TODO: Many more cases to add, such as client_talk_power
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientids")) {						// Client ids
				//message = "";
				command = COMMAND.NONE;
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientdbidfromuid")) {				// Client dbid from uid
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientnamefromuid")) {				// Client name from uid
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientnamefromdbid")) {				// Client name from dbid
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientuidfromclid")) {				// Client uid from clid
				//
			}

			// TODO: VERY much data to handle, leave it for later as it's pretty much useless for me at this point.
			if(notifyEvent.equalsIgnoreCase("notifyconnectioninfo")) {					// Connection info
//						msgUpdate();	// schandlerid
//						notify_clid = Integer.parseInt(msgVarUp());
//						notify_connection_client_ip = msgVarUp();
//						notify_connection_client_port = Integer.parseInt(msgVarUp());
//						notify_connection_client2server_packetloss_speech = Float.parseFloat(msgVarUp() + "f");
//						notify_connection_client2server_packetloss_keepalive = Float.parseFloat(msgVarUp() + "f");
//						notify_connection_client2server_packetloss_control = Float.parseFloat(msgVarUp() + "f");
//						notify_connection_client2server_packetloss_total = Float.parseFloat(msgVarUp() + "f");
//						notify_connection_idle_time = Integer.parseInt(msgVariable());
			}

			if(notifyEvent.equalsIgnoreCase("notifychannelcreated")) {					// Channel created
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifychanneledited")) {					// Channel edited
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifychanneldeleted")) {					// Channel deleted
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifychannelmoved")) {						// Channel moved
				notify_schandlerid = msgVarUp_int();
				notify_ctid = msgVarUp_int();						// target channel ID
				notify_reasonid = msgVarUp_int();					// 0 - switched channel | 1 - moved by another client | TODO: Check on channel kick "move"
				if(notify_reasonid == 0) {
					notify_clid = msgVarUp_int();					// client id, who switched channel
				}
				if(notify_reasonid == 1) {
					notify_invokerid = msgVarUp_int();				// Who moved the client
					notify_invokername = msgVarUp();
					notify_invokeruid = msgVarUp();
					notify_clid = msgVarUp_int();
				}
			}

			if(notifyEvent.equalsIgnoreCase("notifyserveredited")) {						// Server edited
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifyserverupdated")) {					// Server updated
				notify_schandlerid = msgVarUp_int();
				// VERY long list from line 202-248
			}

			if(notifyEvent.equalsIgnoreCase("channellist")) {							// Channel list
				Channel channel;
				List<Channel> channels = new ArrayList<>();
				// Does not always give schandlerid, gives it when joining server
				if(msgNext().startsWith("schandlerid")) notify_schandlerid = msgVarUp_int();
				while(!msgNext().isBlank()) {
					channel = new Channel();
					channel.cid = msgVarUp_int();                                                // cid		- Channel ID
					channel.pid = msgVarUp_int();                                                // pid		- Parent ID
					if (msgNext().startsWith("cpid")) channel.cpid = msgVarUp_int();
					if (msgNext().startsWith("channel_order")) channel.order = msgVarUp_int();
					channel.name = msgVarUp();
					if (msgNext().startsWith("channel_flag_are_subscribed")) {
						channel.flag_are_subscribed = msgVarUp_int();                            // Are you (bot) subscribed to this channel
						channel.total_clients = msgVarUp_int();
					} else {
						channel.topic = msgVarUp();
						channel.codec = msgVarUp_int();
						channel.codec_quality = msgVarUp_int();
						channel.maxclients = msgVarUp_int();
						channel.maxfamilyclients = msgVarUp_int();
						channel.order = msgVarUp_int();
						channel.flag_permanent = msgVarUp_int();
						channel.flag_semi_permanent = msgVarUp_int();
						channel.flag_default = msgVarUp_int();
						channel.flag_password = msgVarUp_int();
						channel.codec_latency_factor = msgVarUp_int();
						channel.codec_is_unencrypted = msgVarUp_int();
						channel.delete_delay = msgVarUp_int();
						channel.unique_identifier = msgVarUp();
						channel.flag_maxclients_unlimited = msgVarUp_int();
						channel.flag_maxfamilyclients_unlimited = msgVarUp_int();
						channel.flag_maxfamilyclients_inherited = msgVarUp_int();
						channel.needed_talk_power = msgVarUp_int();
						channel.forced_silence = msgVarUp_int();
						channel.name_phonetic = msgVarUp();
						channel.icon_id = msgVarUp_int();
						channel.banner_gfx_url = msgVarUp();
						channel.banner_mode = msgVarUp_int();
					}

					// TODO: handle channel
					//addIfDoesNotExist(notify_schandlerid, channel);
				}

				println(channels.size());
			}

			if(notifyEvent.equalsIgnoreCase("channellistfinished")) {					// Channel list finished
				// Finished channel list for server
				notify_schandlerid = msgVarUp_int();
			}

			if(notifyEvent.equalsIgnoreCase("notifytextmessage")) {						// Text message
				notify_schandlerid = msgVarUp_int();
				notify_targetmode = msgVarUp_int();											// 1 - private | 2 - channel | 3 - server
				notify_msg = msgVarUp();
				if(msgNext().startsWith("target")) notify_target = msgVarUp_int();			// reciever's ID
				notify_invokerid = msgVarUp_int();											// sender's ID
				notify_invokername = msgVarUp();
				notify_invokeruid = msgVarUp();
				// TODO: Handle text message, make sure it's incoming, not going out.

				if((notify_targetmode == 1 || notify_targetmode == 2) && notify_invokerid != getBot_clid(notify_schandlerid)) {
					// TODO: Disabled for now.
					//if(!isBot(notify_invokerid)) sendTextMessage(notify_schandlerid, notify_targetmode, notify_invokerid, "I am not programmed to respond yet.");
				}
				println("am i looping? " + notify_invokerid + " " + getBot_clid(notify_schandlerid));
			}

			if(notifyEvent.equalsIgnoreCase("notifycurrentserverconnectionchanged")) {		// Current server connection changed
				// Probably just changed schandlerid - TODO: deal with it later
			}

			if(notifyEvent.equalsIgnoreCase("notifyconnectstatuschange")) {				// Connect status change
				notify_schandlerid = msgVarUp_int();
				notify_status = msgVarUp();					// disconnected OR connection_established
				notify_error = msgVarUp_int();
				// TODO: test
			}

			/***** Unlisted *****/
			if(notifyEvent.equalsIgnoreCase("notifychannelgrouplist")) {
				//
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientchannelgroupchanged")) {
				msgUpdate();	// schandlerid
				notify_invokerid = msgVarUp_int();
				notify_invokername = msgVarUp();
				notify_cgid = msgVarUp_int();										// Channel group ID
				notify_cid = msgVarUp_int();										// Channel ID
				notify_clid = msgVarUp_int();										// Client ID
				notify_cgi = msgVarUp_int();										// UNKNOWN - Seems to be same as cid
			}

			if(notifyEvent.equalsIgnoreCase("notifychannelsubscribed")) {
				notify_schandlerid = msgVarUp_int();
				notify_cid = msgVarUp_int();
				int notify_es = msgVarUp_int();	// Unknown
			}

			if(notifyEvent.equalsIgnoreCase("notifyclientneededpermissions")) {			// Permission ID and Value
				msgUpdate();	// schandlerid
				notify_permid = msgVarUp_int();
				notify_permvalue = msgVarUp_int();
			}

			if(notifyEvent.equalsIgnoreCase("notifyservergrouplist")) {
				notify_schandlerid = msgVarUp_int();
				// loop
				while(!message.isBlank()) {
					if(message.isBlank()) break;
					notify_sgid = msgVarUp_int();
					notify_name = msgVarUp();
					notify_type = msgVarUp_int();
					notify_iconid = msgVarUp_int();
					notify_savedb = msgVarUp_int();
					notify_sortid = msgVarUp_int();
					notify_namemode = msgVarUp();
					notify_n_modifyp = msgVarUp_int();
					notify_n_member_addp = msgVarUp_int();
					notify_n_member_removep = msgVarUp_int();
					// Add groups into an array
					serverGroups.add(new TeamspeakGroup(notify_type, notify_sgid, notify_name));
				}
				// TODO: Do something with this info.
			}

			if(notifyEvent.equalsIgnoreCase("notifyservergroupclientadded")) {
				// Client was added to server group.
				// Example: notifyservergroupclientadded schandlerid=1 invokerid=10229 invokername=Kristjan invokeruid=7k\/3pNm1UZ2K3erv7KTQ2+WAh+o=
				// sgid=789 clid=48736 name=Stakemal cluid=kNxYc1W\/ha41SSWV+FTroE93LRc=
				schandlerid = msgVarUp_int();
				notify_invokerid = msgVarUp_int();
				notify_invokername = msgVarUp();
				notify_invokeruid = msgVarUp();
				notify_sgid = msgVarUp_int();			// Server Group ID
				notify_clid = msgVarUp_int();
				notify_name = msgVarUp();
				notify_cluid = msgVarUp();
			}

			if(notifyEvent.equalsIgnoreCase("notifyservergroupclientdeleted")) {
				// Client was removed from server group.
				// Example: notifyservergroupclientdeleted schandlerid=1 invokerid=10229 invokername=Kristjan invokeruid=7k\/3pNm1UZ2K3erv7KTQ2+WAh+o=
				// sgid=789 clid=48736 name=Stakemal cluid=kNxYc1W\/ha41SSWV+FTroE93LRc=
				schandlerid = msgVarUp_int();
				notify_invokerid = msgVarUp_int();
				notify_invokername = msgVarUp();
				notify_invokeruid = msgVarUp();
				notify_sgid = msgVarUp_int();			// Server Group ID
				notify_clid = msgVarUp_int();
				notify_name = msgVarUp();
				notify_cluid = msgVarUp();
			}

			//println(originalMsg);
			if(logMessage) log(notifyEvent + "	" + message);
			return;
		}
		
		println(command.toString() + ": " + message);
		switch(this.command) {
			default:		// Also with COMMAND.NONE
				if(msgIsError()) return;
				println("invalid command state when handling message: " + message);
				break;
				
			case QUIT:
				msgError();
				break;
				
			case USE:
				msgError();
				println(message);
				break;
				
			case AUTH:
				if(msgNext().equalsIgnoreCase("error")) {
					msgUpdate();			// Auth always response with error
					id = msgVarUp_int();
					msg = message;
					if(id != 0) {
						println(command.toString() + " error: id=" + id + " msg=" + msg);
					}
					command = COMMAND.NONE;		// Must be set before executing the rest.
					if(id == 0) {
						isAuthenticated = true;
						clientNotifyRegister();

						//whoAmI();
						serverConnectionHandlerList();
					}
				}
				break;

			case CLIENTLIST:
				// Example:
				// clid=8824 cid=2613 client_database_id=6966 client_nickname=Stakemal client_type=0|
				// clid=7809 cid=2648 client_database_id=3174 client_nickname=Kristjan client_type=0
				if(msgNext().equalsIgnoreCase("error")) msgError();
				msgUpdate();
				while(!msg.isBlank()) {
					int clid = msgVarUp_int();
					int cid = msgVarUp_int();
					int cdbid = msgVarUp_int();
					String nickname = msgVarUp();
					int type = msgVarUp_int();
					println(clid + " " + nickname);
					if(clid == bot_clid) {
						this.botName = nickname;
						this.bot_cid = cid;
						break;
					}
				}
				break;
				
			case CLIENTNOTIFYREGISTER:
				msgError();
				break;
				
			case CLIENTNOTIFYUNREGISTER:
				msgError();
				break;
				
			case CLIENTPOKE:
				msgError();
				break;
				
			case MESSAGELIST:
				// Sends list of messages
				// int msgid
				// String cluid		- sender's guid
				// String subject
				// int flag_read	- 0 = unread | 1 = read
				// messages are being received by notifymessagelist
				msgError();
				break;
				
			case MESSAGEUPDATEFLAG:
				msgError();
				break;

			case SENDTEXTMESSAGE:
				msgError();
				break;

			case SERVERCONNECTIONHANDLERLIST:
				if(!message.startsWith("error")) {
					/*println(msgVarUp_int());
					println(msgVarUp_int());*/
					println(Color.Critical + "server connection handler list case start");
					println(Color.Critical + message);
					while(!message.isBlank()) {
						if(message.isBlank()) break;
						if(msgNext().startsWith("schandlerid")) {
							int id = msgVarUp_int();
							// Check if it's already in list
							for (TeamSpeakBot bot : bots) {
								if (bot.getSchandlerid() == id) break;
							}
							bots.add(new TeamSpeakBot(id));		// TODO: Needs to be moves somewhere else, as it can't add bot when no connection is established. Maybe try while connecting?
							println("handler added to list");
						}
						if(msgNext().startsWith("clid")) {
							println(Color.Critical + "Bots in array: " + bots.size());
							println(Color.Critical + "Message: " + message);
							getBot(schandlerid).setClid(msgVarUp_int());		// Client ID
							getBot(schandlerid).setCid(msgVarUp_int());			// Channel ID
							println("set bot ids ln755");
						}
					}
				}
				if(!get_schandlerlist) msgError();
				println(Color.Critical + "In server connection handler list case");
				serverGroupList();		// TODO: Needs to be moved, as it does not work when no connection is established. Needs to execute on connection.
				break;

			case SERVERGROUPLIST:
				println(Color.Error + "server group list ln903 " + message);
				break;

			case WHOAMI:
				if(!msgIsError()) if(msgNext().startsWith("clid=")) {
					this.bot_clid = msgVarUp_int();
					this.bot_cid = msgVarUp_int();
					TeamSpeakBot bot = getBot(schandlerid);
					bot.setClid(bot_clid);
					bot.setCid(bot_cid);
				}
				/*if(msgNext().equalsIgnoreCase("error")) {
					msgUpdate();
					id = msgVarUp_int();
					msg = message;
					if(id != 0) println(command.toString() + " error: id=" + id + " msg=" + msg);
					command = COMMAND.NONE;
				}*/
				break;
		}
		message = "";
	}
	
	/***** Message handling functions *****/
	
	// msgVariable finds another variable within itself
	private boolean msgMultVar = false;
	
	private String msgNext() {
		if(message.contains(" ")) {
			return message.substring(0, message.indexOf(" "));
		} else {
			return message.substring(0);
		}
	}
	
	private void msgUpdate() {
		if(!message.contains(" ") && !message.contains("|")) {
			message = "";
		}
		if(msgMultVar) message = message.substring(message.indexOf("|") + 1);
		else message = message.substring(message.indexOf(" ") + 1);
	}

	/**
	 * Gets variable from message.
	 *
	 * @return
	 */
	private String msgVariable() {
		// Used to crash when variable ended without space.
		int spcIdx = message.indexOf(" ");
		int eqIdx = message.indexOf("=");
		int brIdx = message.indexOf("|");	// not used
		if(spcIdx == -1) spcIdx = message.length();
		if(eqIdx == -1 || eqIdx > spcIdx) {
			//println("Returning empty string from: " + message);
			return "";
		}
		if(message.substring(eqIdx + 1, spcIdx).contains("|") && message.substring(eqIdx + 1, spcIdx).contains("=")) {
			// Contains another variable
			msgMultVar = true;
			return message.substring(message.indexOf("=") + 1, message.indexOf("|"));
		} else msgMultVar = false;	// Wrong place?
		return message.substring(message.indexOf("=") + 1, spcIdx);
	}
	
	/**
	 * Combined msgVariable() and msgUpdate() for streamlining.
	 * 
	 * @return
	 */
	private String msgVarUp() {
		String var = msgVariable();
		msgUpdate();
		return var;
	}

	/**
	 * Same as msgVarUp(), but converts result into integer.
	 *
	 * @return
	 */
	private int msgVarUp_int() {
		String var = msgVarUp();
		int var2 = -1;
		if(var.isBlank()) {
			//println(Color.Error + "msgVarUp_int received empty string for integer. (" + command.toString() + ": " + message + ")");
			//return -1;	returns var2 in the end, -1 is by default
		}
		try {
			var2 = Integer.parseInt(var);
		} catch(NumberFormatException e) {
			println("Error from: " + var);
			e.printStackTrace();
		}
		return var2;
	}

	/**
	 * Same as msgVarUp(), but converts into long.
	 *
	 * @return
	 */
	private long msgVarUp_long() {
		String var = msgVarUp();
		long var2 = -1;
		if(var.isBlank()) {
			//println(Color.Error + "msgVarUp_long received empty string for long. (" + command.toString() + ": " + message + ")");
			//return -1;	Same as with int method
		}
		try {
			var2 = Long.parseLong(var);
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		return var2;
	}

	private List<TeamspeakGroup> msgGroups(String msg) {
		List<TeamspeakGroup> groups = new ArrayList<>();
		int groupId = -1;
		String groupMsg = msg;
		while(!groupMsg.isBlank()) {
			if(groupMsg.contains(",")) {
				//println(Color.Error + groupMsg);
				groupId = Integer.parseInt(groupMsg.substring(0, groupMsg.indexOf(",")));
				//println(Color.Client + groupMsg.indexOf(","));
				groupMsg = groupMsg.substring(groupMsg.indexOf(",") + 1);
			} else {
				groupId = Integer.parseInt(groupMsg.substring(0));
				groupMsg = "";
			}
			if(groupId != -1) groups.add(new TeamspeakGroup(groupId));
			//println(Color.Critical + "added group " + groupId);
			groupId = -1;
		}
		return groups;
	}

	/**
	 * Replaces all spaces with "\s", so it can be sent to client without problems.
	 *
	 * @param msg
	 * @return
	 */
	private String msgReplaceSpaces(String msg) {
		// Replaces spaces with \s
		if(msg.contains(" ")) {
			msg = msg.replaceAll(" ", Character.toString((char) 92) + Character.toString((char) 92) + "s");
		}
		return msg;
	}

	private boolean intToBoolean(int in) {
		if(in == 1) return true;
		return false;
	}

	private int booleanToInt(boolean in) {
		if(in) return 1;
		return 0;
	}

	private void addIfDoesNotExist(int schandlerid, Channel newChannel) {
		TeamSpeakBot bot = getBot(schandlerid);
		if(bot.getChannels() == null) bot.setChannels(new ArrayList<Channel>());
		List<Channel> botChannels = bot.getChannels();
		for(Channel channel : botChannels) {
			if(channel.cid == newChannel.cid) return;
		}
		botChannels.add(newChannel);
		bot.setChannels(botChannels);
		println("added channel " + newChannel.name + " to " + schandlerid);
	}
	
	private void msgError() {	// TODO: Make these 2 error functions better!
		if(msgNext().equalsIgnoreCase("error")) {
			// Example: error id=0 msg=ok
			msgUpdate();
			id = msgVarUp_int();
			msg = message;
			if(id != 0) println(Color.Error + command.toString() + " error: id=" + id + " msg=" + msg);


			// TODO: Add a list of errors and what they mean, do some stuff accordingly. Do it before setting command to none.


			command = COMMAND.NONE;
		} else {
			println(Color.Error + "Was not error: " + message);
			log(Color.Error + "Was not error: " + message);
		}
	}
	
	private boolean msgIsError() {
		if(msgNext().equalsIgnoreCase("error")) {
			msgError();
			return true;
		}
		return false;
	}
	
	public void send(String msg) {
		try {
			if(!isAuthenticated && !msg.startsWith("auth")) println(Color.Error + "Trying to send before authenticated: " + msg);
			output.println(msg);
			lastMsg = LocalDateTime.now();
			println("Output: " + msg);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/***** Client Query Functions *****/
	
	public void quit() {
		println("quit");
		command = COMMAND.QUIT;
		send("quit");
	}
	
	private void use(int schandlerid) {
		command = COMMAND.USE;
		send("use schandlerid=" + schandlerid);
	}
	
	private void auth(String apiKey) {
		command = COMMAND.AUTH;
		send("auth apikey=" + apiKey);
	}
	
	/***** Banning *****/
	
	private void banAdd() {	// TODO: finish function
		command = COMMAND.BANAAD;
		send("");
	}
	
	private void banClient() {
		command = COMMAND.BANCLIENT;
		send("");
	}
	
	/***** Channel *****/
	
	/***** Client *****/

	private void clientList() {
		command = COMMAND.CLIENTLIST;
		send("clientlist");
	}
	
	// TODO: Make a list of desired events.
	
	private void clientNotifyRegister() {
		command = COMMAND.CLIENTNOTIFYREGISTER;
		send("clientnotifyregister schandlerid=" + 0/*schandlerid*/ + " event=any");		// schandlerid=0 - applies to all server connection handlers.
	}
	
	private void clientNotifyRegister(String event) {
		command = COMMAND.CLIENTNOTIFYREGISTER;
		send("clientnotifyregister schandlerid=" + schandlerid + " event=" + event);
	}
	
	private void clientNotifyUnregister() {
		command = COMMAND.CLIENTNOTIFYUNREGISTER;
		send("clientnotifyunregister");
	}
	
	private void clientPoke(int schandlerid, int clid, String msg) {
		if(isBot(clid)) return;			// Makes sure it won't poke another bot
		command = COMMAND.CLIENTPOKE;
		//msg = msgReplaceSpaces(msg);
		use(schandlerid);
		send("clientpoke msg=" + msgReplaceSpaces(msg) + " clid=" + clid);
		println("clientpoke msg=" + msg + " clid=" + clid);
	}

	/**
	 * Set client's server group.
	 *
	 * @param cdbid Client Database ID
	 * @param sgid Server Group ID
	 */
	private void setServerGroup(int cdbid, int sgid) {
		send("servergroupaddclient sgid=" + sgid + " cldbid=" + cdbid);
	}
	
	/***** Complain *****/
	
	/***** Connection *****/
	
	private void connect() {
		// TODO: Finish these functions!
		command = COMMAND.CONNECT;
	}
	
	private void currentSchandlerId() {
		command = COMMAND.CURRENTSCHANDLERID;
	}
	
	private void disconnect() {
		command = COMMAND.DISCONNECT;
	}
	
	/***** File Transfer *****/
	
	/***** Message *****/
	
	private void messageList() {
		command = COMMAND.MESSAGELIST;
		send("messagelist");
	}
	
	private void messageUpdateFlag(int msgid, int flag) {
		// This function is for private messages
		// 0 = unread | 1 = read
		command = COMMAND.MESSAGEUPDATEFLAG;
		send("messageupdateflag msgid=" + msgid + " flag=" + flag);
	}

	/**
	 * Send text message to client/channel/server.
	 *
	 * @param schandlerid
	 * @param targetMode
	 * 1 = Client
	 * 2 = Channel
	 * 3 = Server
	 * @param targetClid
	 * @param msg
	 */
	private void sendTextMessage(int schandlerid, int targetMode, int targetClid, String msg) {
		if(isBot(targetClid)) return;			// Makes sure it won't message another bot.
		// TODO: Check if this works!
		command = COMMAND.SENDTEXTMESSAGE;
		use(schandlerid);
		send("sendtextmessage targetmode=" + targetMode + " target=" + targetClid + " msg=" + msgReplaceSpaces(msg));
	}

	/***** Server *****/

	private void serverConnectionHandlerList() {
		command = COMMAND.SERVERCONNECTIONHANDLERLIST;
		get_schandlerlist = true;
		send("serverconnectionhandlerlist");
		println(Color.Critical + "Getting server connection handlers");
	}

	private void serverGroupList() {
		command = COMMAND.SERVERGROUPLIST;
		send("servergrouplist");
	}
	
	/***** Server Group *****/
	
	private void whoAmI() {
		command = COMMAND.WHOAMI;
		send("whoami");
	}

	/***** Custom functions *****/
	private TeamSpeakBot getBot(int schandlerid) {
		for(TeamSpeakBot bot : bots) {
			if(bot.getSchandlerid() == schandlerid) return bot;
		}
		return null;
	}

	private int getBot_clid(int schandlerid) {
		try {
			return getBot(schandlerid).getClid();
		} catch(NullPointerException e) {
			println("Failed to get bot clid for schandler " + schandlerid);
			e.printStackTrace();
		}
		return -1;
	}

	private int getBot_cid(int schandlerid) {
		try {
			return getBot(schandlerid).getCid();
		} catch(NullPointerException e) {
			println("Failed to get bot cid for schandler " + schandlerid);
			e.printStackTrace();
		}
		return -1;
	}

	// Check if target client is a bot, used to prevent infinite loop if both bots are in same server.
	private boolean isBot(int clid) {
		for(TeamSpeakBot bot : bots) {
			if(bot.getClid() == clid) return true;
		}
		return false;
	}

	/**
	 * Add bot name extension.
	 */
	private void changeName() {
		send("clientupdate client_nickname=" + msgReplaceSpaces(botName +" " + botNameExtension));
		botName = botName + " " + botNameExtension;
	}

	/**
	 * Give bot new name
	 *
	 * @param name
	 */
	private void changeName(String name) {
		send("clientupdate client_nickname=" + msgReplaceSpaces(name));
		botName = name;
	}

	/***** Managment functions *****/


}

enum COMMAND {
	NONE,
	QUIT,
	USE,
	AUTH,
	BANAAD,
	BANCLIENT,
	BANDELALL,
	BANDEL,
	BANLIST,
	CHANNELADDPERM,
	CHANNELCLIENTADDPERM,
	CHANNELCLIENTDELPERM,
	CHANNELCLIENTLIST,
	CHANNELCLIENTPERMLIST,
	CHANNELCONNECTINFO,
	CHANNELCREATE,
	CHANNELDELETE,
	CHANNELDELPERM,
	CHANNELEDIT,
	CHANNELGROUPADD,
	CHANNELGROUPADDPERM,
	CHANNELGROUPCLIENTLIST,
	CHANNELGROUPDEL,
	CHANNELGROUPDELPERM,
	CHANNELGROUPLIST,
	CHANNELGROUPPERMLIST,
	CHANNELLIST,
	CHANNELMOVE,
	CHANNELPERMLIST,
	CHANNELVARIABLE,
	CLIENTADDPERM,
	CLIENTDBDELETE,
	CLIENTDBEDIT,
	CLIENTDBLIST,
	CLIENTDELPERM,
	CLIENTGETDBIDFROMUID,
	CLIENTGETIDS,
	CLIENTGETNAMEFROMDBID,
	CLIENTGETNAMEFROMUID,
	CLIENTGETUIDFROMCLID,
	CLIENTKICK,
	CLIENTLIST,
	CLIENTMOVE,
	CLIENTMUTE,
	CLIENTUNMUTE,
	CLIENTNOTIFYREGISTER,
	CLIENTNOTIFYUNREGISTER,
	CLIENTPERMLIST,
	CLIENTPOKE,
	CLIENTUPDATE,
	CLIENTVARIABLE,
	COMPLAINADD,
	COMPLAINDELALL,
	COMPLAINDEL,
	COMPLAINLIST,
	CONNECT,
	CURRENTSCHANDLERID,
	DISCONNECT,
	FTCREATEDIR,
	FTDELETEFILE,
	FTGETFILEINFO,
	FTGETFILELIST,
	FTINITDOWNLOAD,
	FTINITUPLOAD,
	FTLIST,
	FTRENAMEFILE,
	FTSTOP,
	HASHPASSWORD,
	MESSAGEADD,
	MESSAGEDEL,
	MESSAGEGET,
	MESSAGELIST,
	MESSAGEUPDATEFLAG,
	PERMOVERVIEW,
	SENDTEXTMESSAGE,
	SERVERCONNECTINFO,
	SERVERCONNECTIONHANDLERLIST,
	SERVERGROUPADDCLIENT,
	SERVERGROUPADD,
	SERVERGROUPADDPERM,
	SERVERGROUPCLIENTLIST,
	SERVERGROUPDELCLIENT,
	SERVERGROUPDEL,
	SERVERGROUPDELPERM,
	SERVERGROUPLIST,
	SERVERGROUPPERMLIST,
	SERVERGROUPSBYCLIENTID,
	SERVERVARIABLE,
	SETCLIENTCHANNELGROUP,
	TOKENADD,
	TOKENDELETE,
	TOKENLIST,
	TOKENUSE,
	VERIFYCHANNELPASSWORD,
	VERIFYSERVERPASSWORD,
	WHOAMI
}

class TeamSpeakBot {
	private final int schandlerid;
	private boolean isActive;
	private String address;
	private int port;
	private String botName;
	private int homeChannel;			// Bot's desired channel, will join on startup.
	private boolean stickyChannel;		// Go back to home channel when moved out.
	private int clid;					// Client ID
	private int cid;					// Channel ID
	private List<Channel> channels;

	/***** This has stupid useless functions. *****/
	
	public TeamSpeakBot(int schandlerid) {
		this.schandlerid = schandlerid;
		System.out.println("added bot " + this.schandlerid);
	}

	public int getSchandlerid() {
		return this.schandlerid;
	}

	protected void register() {
		// Register bot to NQA system to get variables
	}

	public void setBotName(String name) {
		this.botName = name;
	}

	public String getBotName() {
		return this.botName;
	}

	public void setClid(int clid) {
		this.clid = clid;
		System.out.println(schandlerid + " new clid " + this.clid);
	}

	public int getClid() {
		return this.clid;
	}

	public void setCid(int cid) {
		this.cid = cid;
		System.out.println(schandlerid + " new cid " + this.cid);
	}

	public int getCid() {
		return this.cid;
	}

	public void setHomeChannel(int newHomeChannelId) {
		this.homeChannel = newHomeChannelId;
	}

	public int getHomeChannel() {
		return this.homeChannel;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public List<Channel> getChannels() {
		return this.channels;
	}

	public Channel getChannel(int cid) {
		for(Channel channel : channels) {
			if(channel.cid == cid) return channel;
		}
		System.out.println("Could not find channel by id: " + cid);
		return null;
	}
}

class ClientList {
	final private int schandlerid;
	private List<TeamspeakClient> clients;

	public int getSchandlerid() {
		return this.schandlerid;
	}

	public ClientList(int schandlerid) {
		this.schandlerid = schandlerid;
	}

	public List<TeamspeakClient> getClients() {
		return this.clients;
	}

	public TeamspeakClient getClientByClid(int clientId) {
		for(TeamspeakClient client : clients) {
			if(client.getClid() == clientId) return client;
		}
		return null;
	}

	public void addClient(TeamspeakClient client) {
		this.clients.add(client);
	}

	public void removeClient(TeamspeakClient client) {
		this.clients.remove(client);
	}

	public void updateClient(TeamspeakClient client) {
		for(TeamspeakClient clnt : clients) {
			if(clnt.getClid() == client.getClid()) {
				clnt = client;
				return;
			}
		}
	}
}

class Channel {
	//TeamSpeak channel. Meant as attempt to reduce the amount of variables
	int cid;								// cid - 								Channel ID
	int pid;								// pid -								Parent ID?
	int cpid;								// cpid - 								Channel Parent ID?
	String name;							// channel_name
	String topic;							// channel_topic -						Description
	int codec;								// channel_codec
	int codec_quality;						// channel_codec_quality
	int maxclients;							// channel_maxclients
	int maxfamilyclients;					// channel_maxfamilyclients -			Maximum child channel clients?
	int order;								// channel_order
	int flag_permanent;						// channel_flag_permanent -				Channel is permanent and will not be deleted when empty
	int flag_semi_permanent;				// channel_flag_semi_permanent -		Channel is semi-permanent and will be deleted on server shutdown/restart
	int flag_default;						// channel_flag_default -				Clients will connect to this channel by default, unless said otherwide in bookmark
	int flag_password;						// channel_flag_password -				Channel is password protected
	int codec_latency_factor;				// channel_codec_latency_factor
	int codec_is_unencrypted;				// Channel_codec_is_unencrypted
	int delete_delay;						// channel_delete_delay -				Empty temporary channel will be automatically deleted after this amount of time has passed (seconds)
	String unique_identifier;				// channel_unique_identifier
	int flag_maxclients_unlimited;			// channel_flag_maxclients_unlimited
	int flag_maxfamilyclients_unlimited;	// channel_flag_maxfamilyclients_unlimited
	int flag_maxfamilyclients_inherited;	// channel_flag_maxfamilyclients_inherited
	int needed_talk_power;					// channel_needed_talk_power
	int forced_silence;						// channel_forced_silence
	String name_phonetic;					// channel_name_phonetic -				Channel name for text-to-speach
	int icon_id;							// channel_icon_id
	String banner_gfx_url;					// channel_banner_gfx_url
	int banner_mode;						// channel_banner_mode

	int flag_are_subscribed;				// channel_flag_are_subscribed -		Current client is subscribed to channel or not
	int total_clients;						// total_clients -						Amount of clients currently in channel
}