package io.nqa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import io.nqa.TeamspeakClient;
import io.nqa.Sys.Color;
@SuppressWarnings("static-access")

//find better name?
public class Sys {
	public LocalDate date = LocalDate.MIN;	//.now();		//has to be first to avoid NullPointerException
	private String logFileName = "log";
	private File logFile = new File(logFileName);
	public static String version = "0.8.1 - TS ONLY";
	public Server server = new Server(this);
	public Data data = new Data(this);
	public TeamSpeak3 teamspeak = new TeamSpeak3(this);
	public Rosinad rosinad = new Rosinad(this);
	private String name = "SysAdm";
	private String command = "";
	
	public boolean serviceEnabledServer = true;
	public boolean serviceEnabledTeamspeak = true;
	
	public int saveIntervalInSeconds = 5;
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!date.isEqual(LocalDate.now())) {
				newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.Default + " " + t + Color.Input);
			appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	/**
	 * Saves text into log file with timestamp
	 * 
	 * @param line
	 */
	public void log(String line) {
		try {
			//logFile = new File(logFileName);
			if(!logFile.exists()) logFile.createNewFile();
			FileWriter logWriter = new FileWriter(logFile, true);
			BufferedWriter bufferedWriter = new BufferedWriter(logWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter);
			printWriter.println(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + line);
			
			printWriter.flush();
			printWriter.close();
			bufferedWriter.close();
			logWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	//maybe this works, because log(true) does not
	public void newDate() {
		//println("NEW DATE");
		if(date.equals(LocalDate.now())) return;
		try {
			date = LocalDate.now();
			System.out.println(Color.Time + date + " " + date.getDayOfWeek() + Color.Default);
			appendSessionLog(date + " " + date.getDayOfWeek());
			
			//logFile = new File(logFileName);
			if(!logFile.exists()) logFile.createNewFile();
			FileWriter logWriter = new FileWriter(logFile, true);
			BufferedWriter bufferedWriter = new BufferedWriter(logWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter);
			printWriter.println(date + " " + date.getDayOfWeek());
			
			printWriter.flush();
			printWriter.close();
			bufferedWriter.close();
			logWriter.close();
			//println("SYSTEM: LOG NEWDATE END");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void appendSessionLog(String str) {
		Main.sessionLog.append(str);
	}
	
	/**
	 * Get current session console log.
	 * For external application use only!
	 * 
	 * @return
	 */
	public String getSessionLog() {
		return Main.sessionLog.toString();
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			keepAlive();			//rename?
		}
	};
	Thread thread = new Thread(runnable);
	
	private void keepAlive() {
		while(true) try {
			if(!date.isEqual(LocalDate.now())) {
				/*date = LocalDate.now();
				System.out.println(Color.Time + date + " " + date.getDayOfWeek() + Color.Default);*/
				newDate();
			}
			thread.sleep(5*60*1000);			//saves every 5 minutes
			data.save();
			//data.save(false, false);
		} catch(InterruptedException e) {
			if(e.getClass() == java.lang.InterruptedException.class) println(e.getMessage());	//thread interrupted
			else e.printStackTrace();
		}
	}
	
	/**
	 * Initialize whole system and it's services
	 */
	public void initialize() {
		try {
			thread.setName("Thread-System");
			thread.start();
			//System.out.println(Color.Time + date + " " + date.getDayOfWeek() + Color.Default);
			//logFile.createNewFile();
			println(Color.Client + "Not Quite Alright" + Color.Default + " version " + Color.Success + version);
			log("Not Quite Alright version " + version);
			println(Color.Success + "Initializing");
			log("System:	Initializing");
			data.load();
			//server.initialize();
			teamspeak.initialize();
			//rosinad.initialize();
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			while(true) {
				command = keyboard.readLine();
				if(!command.isBlank()) execCommand();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * CAUTION!
	 */
	private void purge(boolean hardPurge) {
		println(Color.Critical + "PURGE INITIATED");
		if(hardPurge) println("Call Data to immediately empty memory and delete any data files.");
		else println("Call Data to save and encrypt data into a new file with some sort of calculated method and send decrypt instructions to SysAdm and tell Data to delete any saved files and flush memory.");
		if(hardPurge) data.purge(hardPurge, "dasdas");
		else println("is not hardd");
	}
	
	// MAKE SOMETHING HERE TO HAVE IT AUTHENTICATE WITH DATABASE AND CHECK IF USER CAN EXECUTE COMMANDS AND DO STUFF ACCORDING TO PERMISSION!!!
	
	
	private void authenticate() {		//not implemented
		println("authenticating");
		// do stuff
	}
	
	public void requestExec() {
//		execCommand();
	}
	
	// make something to auth for use so it can be called from outside class,, maybe,,
	
	/**
	 * Cases:
	 * help
	 * info
	 * status
	 * start
	 * shutdown
	 * restart
	 * purge
	 * list
	 * announce
	 * service (not implemented)
	 */
	private void execCommand() {
		switch(cmdNext()) {
		default:
			println(Color.Warning + "Invalid command");
			break;
		case "help":
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(!cmdNext().isBlank()) println("help: invalid paramter " + cmdNext());
				if(cmdNext().isBlank()) println("help: lacks a parameter");
				println(Color.Warning + "help: lacks a parameter");
				break;
			case "help":
				println("No help for you! >:(");
				break;
			case "list":
				println("list <id>\n"
						+ "clients");
			}
			server.msgToServer(name, "this is fucking stupid");
			break;
		case "info":
			println(Color.System + "Not Quite Alright system control panel version " + version);
			//data.save(false, false);
			//data.registerUser("test", "test");
			break;
		case "status":
			cmdUpdate();
			switch(cmdNext()) {
			default:
//				println(Color.Success + "obviously something is working");
				println(getStatus(""));
				break;
			case "system":
//				println(Color.Success + "it should be working as you can give orders and see this text");
				println(getStatus(cmdNext()));
				break;
			case "server":
//				if(server.isAlive) println(Color.Success + "Server is alive");
//				if(!server.isAlive) println(Color.Error + "Server is dead");
				println(getStatus(cmdNext()));
				break;
			case "teamspeak":
//				if(teamspeak.isAlive) println(Color.Success + "TeamSpeak is alive.. change that shit to check for stuff");
//				if(!teamspeak.isAlive) println(Color.Error + "TeamSpeak is dead");
				println(getStatus(cmdNext()));
				break;
			}
			break;
		case "start":
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(!cmdNext().isBlank()) println(Color.Warning + "start: invalid parameter " + cmdNext());
				if(cmdNext().isBlank()) println(Color.Warning + "start: missing a parameter");
				break;
			case "server":
				if(!server.isAlive) {
					server = new Server(this);
					server.system = this;
					server.initialize();
				} else println("Server is alive. Use 'restart server' instead");
				break;
			case "teamspeak":
				if(!teamspeak.isAlive) {
					teamspeak = new TeamSpeak3(this);
					teamspeak.system = this;
					teamspeak.initialize();
				} else println("TeamSpeak is alive. Use 'restart teamspeak' instead");
				break;
			case "rosinad":
				println("incomplete sys::165");
				break;
			}
			break;
		case "shutdown":
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(!cmdNext().isBlank()) {
					println("'shutdown' invalid parameter " + cmdNext() + ", check 'help shutdown'");
				} else println("'shutdown' lacks a parameter, check 'help shutdown'");
				println(Color.Warning + "shutdown: invalid or missing parameter");
				break;
			case "system":
				shutdown();
				break;
			case "server":
				if(server.isAlive) server.shutdown();
				break;
			case "teamspeak":
				if(teamspeak.isAlive) teamspeak.shutdown();
				break;
			}
			break;
		case "restart":
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(!cmdNext().isBlank()) {
					println("'restart' invalid parameter " + cmdNext());
				} else println("'restart' lacks a parameter, check 'help restart'");
				break;
			case "system":
				restart(0);
				break;
			case "running":
				restart(1);
				break;
			case "server":
				if(!server.isAlive) println(Color.Error + "Server is dead, run 'start server' instead");
				if(server.isAlive) restart(2);
				break;
			case "teamspeak":
				if(!teamspeak.isAlive) println(Color.Error + "TeamSpeak is dead, run 'start teamspeak' instead");
				if(teamspeak.isAlive) restart(3);
				break;
			case "rosinad":
				restart(4);
				break;
			}
			break;
		case "purge":
			cmdUpdate();
			boolean hard = false;
			if(cmdNext() == "true" | cmdNext() == "1") hard = true;
			purge(hard);
			break;
		case "save":
			LocalDateTime startTime = LocalDateTime.now();
			cmdUpdate();
			data.save(false, false);
			long loadingTime = startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
			println(Color.Success + "Force saved data in " + loadingTime + " seconds");
			break;
		case "saveRaw":
			data.save(true, true);
			println("Saved raw data");
			break;
		case "reencrypt":
			println(Color.Critical + "DATA REENCRYPTION STARTED");
			data.save(true, false);
			break;
		case "list":
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(cmdNext().isBlank()) println("'list' lacks a parameter");
				else println(Color.Warning + "'list' invalid parameter " + cmdNext());
				break;
			case "clients":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					if(!cmdNext().isBlank()) println("'list clients' invalid parameter " + cmdNext());
					else {
						println("list clients here");
					}
					break;
				case "server":
					String clients = cmdNext() + ":\n";
					if(server.clients.size() > 0) {
						for(int i = 0; i < server.clients.size(); i++) clients += server.clients.get(i).name + "\n";
						println(clients);
						break;
					}
					println("There are no clients connected to " + cmdNext());
					break;
				case "teamspeak":
					if(data.getTeamspeakClients().size() == 0) println("Nobody is connected to teamspeak");			//no need?
					for(int i = 0; i < data.getTeamspeakClients().size(); i++) {
						DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
						TeamspeakClient client = data.getTeamspeakClients().get(i);
						
						String timeSince = "";
						long years = client.getLastSeen().until(LocalDateTime.now(), ChronoUnit.YEARS);
						long months = client.getLastSeen().until(LocalDateTime.now(), ChronoUnit.MONTHS);
						long days = client.getLastSeen().until(LocalDateTime.now(), ChronoUnit.DAYS);
						long hours = client.getLastSeen().until(LocalDateTime.now(), ChronoUnit.HOURS);
						long minutes = client.getLastSeen().until(LocalDateTime.now(), ChronoUnit.MINUTES);
						if(years > 0) {
							if(years > 1) timeSince += years + " years ";
							else timeSince += years + " year ";
						}
						if(months > 0) {
							if(months - (years * 12) > 1) timeSince += (months - (years * 12)) + " months ";
							else timeSince += (months - (years * 12)) + " month ";
						}
						if(days > 0) {
							if(days - (months * 30) > 1) timeSince += (days - (months * 30)) + " days ";
							else timeSince += (days - (months * 30)) + " day ";		//avg month or whatever
						}
						if(hours > 0) {
							if(hours - (days * 24) > 1) timeSince += (hours - (days * 24)) + " hours ";
							else timeSince += (hours - (days * 24)) + " hour ";
						}
						if(minutes > 0) {
							if(minutes - (hours * 60) > 1) timeSince += (minutes - (hours * 60)) + " minutes ";
							else timeSince += (minutes - (hours * 60)) + " minute ";
						}
						if(timeSince.isBlank()) timeSince = "just now)";
						else timeSince += "ago)";
						
						String online = Color.Error + "Offline" + " Last seen on " + dateformat.format(client.getLastSeen()) + " (" + timeSince;
						if(data.getTeamspeakClients().get(i).isConnected()) online = Color.Success + "Online";
						println(Color.Client + client.getNickname() + Color.Default + ": " + online);
					}
					break;
				case "rosinad":
					System.out.println((Integer) data.getRosinClients().size() + "\n" + (Integer) rosinad.connections.size());
					for(Rosinad.Connection connection : rosinad.connections) {
						println(connection.rosin.isConnected() + " " + connection.rosin.isClosed());
					}
					break;
				}
				break;
			case "servers":
				cmdUpdate();
				if(!cmdNext().isBlank()) {
					switch(cmdNext()) {
					default:
						if(!cmdNext().isBlank()) println("'list servers' invalid parameter " + cmdNext());
						else {
							println("list clients here");
						}
						break;
					case "teamspeak":
						println("teamspeak servers here");
						break;
					}
				}
				break;
			case "commands":
				println("Commands:\n"
						+ "kaka\n"
						+ "puuks");
				break;
			}
			break;
		case "announce":
			cmdUpdate();
			if(!cmdNext().isBlank()) {
				server.msgToServer("Announcement from " + name, command);
				teamspeak.msgSystemToServer("Announcement from " + name + ": " + command);
			} else println("announce: can't send blank message");
			break;
		case "service":
			cmdUpdate();
			switch(cmdNext() ) {
			default:
				if(cmdNext().isBlank()) println("'service' lacks a parameter");
				else println("'service' invalid parameter " + cmdNext());
				break;
			case "enable":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					if(cmdNext().isBlank()) println("'service enable' lacks a parameter");
					else println("'service enable' invalid parameter " + cmdNext());
					break;
				case "server":
					if(serviceEnabledServer) println(Color.Success + "Server already enabled");
					else {
						serviceEnabledServer = true;
						println(Color.Success + "Server enabled");
					}
					break;
				case "teamspeak":
					if(serviceEnabledTeamspeak) println(Color.Success + "Teamspeak already enabled");
					else {
						serviceEnabledTeamspeak = true;
						println(Color.Success + "Teamspeak enabled");
					}
					break;
				}
				break;
			case "disable":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					if(cmdNext().isBlank()) println("'service enable' lacks a variable");
					else println("'service disable' invalid variable " + cmdNext());
					break;
				case "server":
					if(serviceEnabledServer) println(Color.Warning + "Server already disabled");
					else {
						serviceEnabledServer = false;
						println(Color.Warning + "server disabled"); 
					}
					break;
				case "teamspeak":
					if(serviceEnabledTeamspeak) println(Color.Warning + "Teamspeak already disabled");
					else {
						serviceEnabledTeamspeak = false;
						println(Color.Warning + "teamspeak disabled"); 
					}
					break;
				}
				break;
			}
			break;
		case "teamspeak":
			if(teamspeak == null) {
				println("cant issue teamspeak commands as teamspeak is not set ln 535");
				break;
			}
			cmdUpdate();
			switch(cmdNext()) {
			default:
				if(cmdNext().isBlank()) println(Color.Warning + "'teamspeak' lacks a variable");
				else println(Color.Warning + "teamspeak: invalid parameter " + cmdNext());
				break;
			case "set":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					if(cmdNext().isBlank()) println(Color.Warning + "'teamspeak set' lacks a variable");
					else println(Color.Warning + "'teamspeak set' invalid parameter " + cmdNext());
					break;
				case "name":
					cmdUpdate();
					switch(cmdNext()) {
					default:
						if(cmdNext().isBlank()) break;
						teamspeak.setQueryName(cmdNext());
						break;
					}
					break;
				}
			case "bot":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					println("invalid sys-ts-bot command ln 561");
					break;
				case "start":
					println("starting ts3 bot");
					teamspeak.startBot();
					break;
				case "stop":
					println("stoping ts3 bot");
					teamspeak.stopBot();
					break;
				case "send":
					cmdUpdate();
					teamspeak.botHandler.send(cmdNext());
					break;
				}
				break;
			}
			break;
		}
		command = null;
	}
	
	/**
	 * Kick someone by IP address on everything
	 * 
	 * @param ipAddress
	 */
	public void kick(String ipAddress) {
		println("kick client on IP address: " + ipAddress);	//maybe sue clientID instead
	}
	
	/**
	 * Ban someone based on IP address
	 * 
	 * @param ipAddress
	 */
	public void ban(String ipAddress) {
		println("ban client from IP address " + ipAddress);
	}
	
	/**
	 * Start a service
	 * 
	 * @param service	Service Name
	 */
	public void start(String service) {
		println("start service " + service);
	}
	
	/**
	 * Restart selected services
	 * 
	 * Service list:
	 * 		0 - Entire system
	 * 		1 - Only running services	//not implemented
	 * 		2 - Server
	 * 		3 - TeamSpeak
	 * 
	 * @param service	Service ID
	 */
	public void restart(int service) {				//replace with Service enum
		switch(service) {
		default:
			if(!cmdNext().isBlank()) {
				println("No service defined!\n"
						+ "0. Entire system\n"
						+ "1. Running services\n"
						+ "2. Command/chat server\n"
						+ "3. TeamSpeak");
			} else println(Color.Critical + "'restart' invalid service " + service);
			break;
		case 0:
			println(Color.Critical + "System restart initiated");
			if(data != null) data.save(false, false);
			if(server != null) server.shutdown();
			server = null;
			if(teamspeak != null) teamspeak.shutdown();
			teamspeak = null;
			if(rosinad != null) rosinad.shutdown();
			rosinad = null;
			Main.restart();
			break;
		case 1:
			println("service 2 = running applications");
			break;
		case 2:
			println(Color.Server + "Server restart initiated");
			if(server != null) server.shutdown();
			server = null;
			server = new Server(this);
			server.system = this;
			server.initialize();
			break;
		case 3:
			println(Color.TeamSpeak + "TeamSpeak restart initiated");
			if(teamspeak != null) teamspeak.shutdown();
			teamspeak = null;
			teamspeak = new TeamSpeak3(this);
			teamspeak.system = this;
			teamspeak.initialize();
			break;
		case 4:
			println("Rosinad restart init");
			if(rosinad != null) rosinad.shutdown();
			rosinad = null;
			rosinad = new Rosinad(this);
			rosinad.system = this;
			rosinad.initialize();
			break;
		}
	}
	
	/**
	 * Initiate shutdown procedure for all subsystems.
	 * Throws a fuck ton of InterruptedExceptions, not going to deal with that shit right now for sure!
	 */
	public void shutdown() {
		println(Color.Critical + "Shutdown initiated");
		if(data != null) data.save(false, false);
		if(teamspeak != null) teamspeak.shutdown();
		if(server != null) server.shutdown();
		println(Color.Success + "Shutdown complete!");
		System.exit(0);
	}
	
	private String cmdNext() {
		if(command.contains(" ")) {
			return command.substring(0, command.indexOf(" "));
		} else {
			return command.substring(0);
		}
	}
	
	private void cmdUpdate() {
		command = command.substring(command.indexOf(" ") + 1);
	}
	
	public void announce(String msg) {
		if(!msg.isBlank()) {	// needs edit
			server.msgToServer("Announcement from " + Color.SysAdm + name, msg);
			teamspeak.msgSystemToServer("Announcement from " + name + ": " + msg);
		} else println("announce: can't send blank message");
	}
	
	// shitty commands start here
	
	/**
	 * Get service status based on service name
	 * 
	 * @param service - Service name as String
	 * @return - String containing status message
	 */
	public String getStatus(String service) {	//check if or waht color reciever supports...
		String status = "";
		String statusServer = "dead";
		String statusTeamSpeak = "dead";
		if(server.isAlive) statusServer = "good";
		if(teamspeak.isAlive) statusTeamSpeak = "good";
		switch(service) {
		default:
			status = "";
		case "system":
			status = "System:\n"
					+ "  Server: " + statusServer + "\n"
					+ "  TeamSpeak: " + statusTeamSpeak;
			break;
		case "server":
			status = "Server: " + statusServer;
			break;
		case "teamspeak":
			status = "TeamSpeak: " + statusTeamSpeak;
			break;
		}
		return status;
	}
	
	//DATA
	//Pass requests and commands to Data
	//This is to make sure that data stored in Data can not be manipulated with
	//I need to add authentification here
	//Maybe not needed?
	
	private void save() {		//used in Data
		
	}
	
	public class Color {
		public static final String Default = "\033[0m";
		public static final String Background = "\u001b[48;5;0m";
		public static final String Input = "\u001b[38;5;11m" + Background;
		public static final String Error = "\u001b[38;5;1m";
		public static final String Critical = "\u001b[48;5;1m";
		public static final String Warning = "\u001b[38;5;208m";
		public static final String Debug = "\u001b[38;5;172m";
		public static final String Success = "\u001b[38;5;2m";
		public static final String Time = "\u001b[38;5;102m";
		public static final String System = "\u001b[38;5;3m";
		public static final String Server = "\u001b[38;5;9m";
		public static final String TeamSpeak = "\u001b[38;5;39m";
		
		// roles
		public static final String Client = "\u001b[38;5;159m";
		public static final String SysAdm = "\u001b[38;6;20m";
		public static final String Administrator = SysAdm;
		public static final String Moderator = SysAdm;
		public static final String User = Client;
		public static final String Guest = Client;
	}
	
	public enum Service {		//prolly not needed
		System,
		Server,
		TeamSpeak
	}
	
	public enum Role {
		Administrator,
		Moderator,
		User,
		Guest
	}
	
	public enum LogType {		//prolly not needed
		Critical,
		Error,
		Warning,
		Info,
		Debug,
		Standard
	}
	
	public enum Priority {
		Critical,
		High,
		Normal,
		Low
	}
	
	public enum Status {
		ServiceGood,
		ServiceDown,
		ClientGood,
		ClientWarn,
		ClientBanned,
		ClientImmune
	}
	
	public enum ClientEvent {
		Connect,
		Disconnect,
		Ban
	}
}

/* TODO
login auth thru other services */