package io.nqa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;

import io.nqa.Sys.Color;

public class Rosinad {
	public Sys system;
	private ServerSocket listener;
	private int port = 14769;
	public int poolThreads = 64;
	private ExecutorService pool;
	public boolean keepAlive;
	public int keepAliveRate = 10000;
	private boolean isAlive;			//doesn't seem to be used
	private int threadTimeoutInMinutes = 15;			//completely worthless
	public List<Connection> connections = new ArrayList<Connection>();
	// TODO: change from Rosin class to Data.RosinClient class.
	public List<Rosin> rosinad = new ArrayList<Rosin>();
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!system.date.isEqual(LocalDate.now())) {
				system.newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.Server + " Rosinad: " + Color.Default + t + Color.Input);
			system.appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	private void log(String str) {
		system.log("Rosinad:	" + str);
	}
	
	Runnable runnableRosinad = new Runnable() {
		@Override
		public void run() {
			keepAlive();
		}
	};
	Thread threadRosinad = new Thread(runnableRosinad);
	
	Runnable runnableConnections = new Runnable() {
		@Override
		public void run() {
			checkConnections();
		}
	};
	Thread threadConnections = new Thread(runnableConnections);
	
	public Rosinad(Sys system) {
		this.system = system;
		//initialize();
	}
	
	public void initialize() {
		println(Color.Success + "Initializing");
		log("Initializing");
		try {
			keepAlive = true;
			pool = Executors.newFixedThreadPool(64);
			listener = new ServerSocket(port);
			threadRosinad.setName("Thread-Rosinad");
			threadRosinad.start();
			threadConnections.setName("Thread-Rosinad-Connections");
			threadConnections.start();
			isAlive = true;
		} catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < 100; i++) {
			Rosin rosin = new Rosin();
			rosin.id = i;
			rosin.displayname = "rosin nr. " + i;
			rosin.login = "rosinlogin" + i;
			rosin.age = (int) (Math.random() * 100);
			rosin.location = "rosin location " + i;
			rosinad.add(rosin);
		}
	}
	
	private void keepAlive() {
		while(keepAlive) {
			for(Connection connection : connections) {		//this work??
				if(connection.rosin.isClosed()) {
					println(connection.rosin.getInetAddress() + " disconnected");
					log(connection.rosin.getInetAddress() + " disconnected");
				}
//				connection.output.println("test");
//				println("poop");
			}
			try {
				if(connections.size() > 0) keepAliveRate = 200;
				else keepAliveRate = 10000;
				Socket connection = listener.accept();
				println(connection.getInetAddress());
				Connection rosinThread = new Connection(connection);
				rosinThread.rosinad = this;
				connections.add(rosinThread);
				pool.execute(rosinThread);
//				println("rosin sleeping");
				Thread.sleep(keepAliveRate);
			} catch(Exception e) {
				if(e.getClass() == java.net.SocketException.class) println(Color.Error + e.getMessage());
				else e.printStackTrace();
			}
		}
		println(Color.Warning + "keepAlive thread terminating!");
		shutdown();
	}
	
	/**
	 * Since android does not notify server when it drops connection I am forced to check if there has been any activity between server and client to drop dead connections
	 */
	private void checkConnections() {		//mayhaps make it check for sessionId
		while(keepAlive) {
			//println("active connections: " + connections.size());
			for(int i = 0; i < connections.size(); i++) {
				if(connections.get(i).lastMessage.until(LocalDateTime.now(), ChronoUnit.MINUTES) > threadTimeoutInMinutes) {
					try {
						println("Dropping connection to client due to inactivity");
						connections.get(i).rosin.close();
						connections.get(i).input.close();
						connections.get(i).output.close();
						connections.remove(i);
					} catch(IOException e) {
						e.printStackTrace();
					}
				} else {
					//maybe add mre shit here
					connections.get(i).output.println("get sessionid");
				}
			}
			try {
				threadConnections.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		println("ROSINAD CHECK CONNECTION FAILED");
	}
	
	public void shutdown() {
		try {
			keepAlive = false;
			threadRosinad.interrupt();
			pool.shutdownNow();
			listener.close();
			isAlive = false;
		} catch(NullPointerException | IOException e) {
			println(e.getMessage());
			//e.printStackTrace();
		}
		println(Color.Critical + "Shutdown");
		log("Shutdown");
	}
	
	public class Connection implements Runnable {
		Rosinad rosinad;
		RosinClient client;
		protected Socket rosin;
		private BufferedReader input;
		public PrintWriter output;
		private String request = "";
		LocalDateTime lastMessage = LocalDateTime.now();
		private String sessionId;
		
		private KeyPair keys;
		private PublicKey clientKey;
		
		public void run() {
			while(true) {
				//String request;
				try {
					request = input.readLine();
					if(request == null) break;
					requestHandler(/*request*/);
				} catch(IOException e) {
//					println(Color.Client + name + Color.Server + "@" + client.getRemoteSocketAddress() + ": Connection lost");
					println("dropped conn");
					rosinad.connections.remove(this);
					break;
				}
			}
		}
		
		public Connection(Socket rosin) {
			try {
				this.rosin = rosin;
				input = new BufferedReader(new InputStreamReader(rosin.getInputStream()));
				output = new PrintWriter(rosin.getOutputStream(), true);
				println(rosin.getRemoteSocketAddress() + " connected");
				log(rosin.getRemoteSocketAddress() + " connected");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		private void requestHandler(/*String request*/) {			//////somehow streamline the response
			
			// pre-define some variables, because complier reasons
			String username = "";
			String password = "";
			int rosinId;
			
			println("request: " + request);
//			output.println(getRandomRosin());
			//Rosin r = getRandomRosin();
			//if(request.equals("get rosin random")) output.println("BeginRosin " + r.id + " '" + r.displayname + "' " + r.age + " '" + r.location + "' EndRosin");
			switch(cmdNext()) {
			default:
				println("request handler default: " + request);
				break;
			case "notify":
				println("pong from client");
				cmdUpdate();
				switch(cmdNext()) {
				default:
					println("Unknown notify message " + cmdNext());
					log("Unknown notify message " + cmdNext());
					break;
				case "sessionid":
					cmdUpdate();
					println("add notify sessionid stuff");
					String clientSessionId = cmdNext();
					if(!sessionId.contentEquals(clientSessionId)) {
						output.println("logout");
						clientSessionId = Data.generateGuid();
						output.println("set sessionid " + clientSessionId);
					}
					println(clientSessionId);
					break;
				}
				break;
			case "get":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					break;
				case "rosin":
					cmdUpdate();
					switch(cmdNext()) {
					default:
						// In case the remaining message is not an integer
						try {
							if(cmdNext().isBlank()) {
								log("get rosin lacks target id");
								break;
							}
							int id = Integer.parseInt(cmdNext());
							for(RosinClient r : rosinad.system.data.getRosinClients()) {
								
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						break;
					case "random":
						Rosin r = getRandomRosin();
						if(r == null) {
							output.println("out of rosins biatch");
							break;
						}
						output.println("BeginRosin " + r.id + " '" + r.displayname + "' " + r.age + " '" + r.location + "' EndRosin");
						break;
					case "self":
						if(client == null) {
							output.println("could not fetch self");
							break;
						}
						// TODO: fix the damn rosin thing so i can send all the needed stuff
						output.println("BeginRosin " + client.getId() + " '" + client.getDisplayName() + "' " + 24 + " '" + "locatiing" + "' EndRosin");
						break;
					}
					break;
				}
				break;
				
				// User related functions
			case "register":		//clearly needs more than just these
				cmdUpdate();
				username = cmdNext();
				cmdUpdate();
				password = cmdNext();
				cmdUpdate();
				String email = cmdNext();
				respond("register", register(username, password, email));
				break;
				
			case "login":
				cmdUpdate();
				username = cmdNext();
				cmdUpdate();
				password = cmdNext();
				int loginCode = login(username, password);
				if(loginCode == 0) {
					sessionId = Data.generateGuid();
					println("response login 0 " + sessionId);
					output.println("response login 0 " + sessionId);
				}
				respond("login", loginCode);
				break;
				
			// Update profile information
			case "update":
				cmdUpdate();
				switch(cmdNext()) {
				default:
					if(cmdNext().isBlank()) println(client.getUsername() + " provided empty update message");
					else println(client.getUsername() + " provided invalid update: " + cmdNext());
					break;
				case "displayname":
					cmdUpdate();
					client.setDisplayName(request);
					break;
				case "age":
					cmdUpdate();
					client.setAge(Integer.parseInt(cmdNext()));
					break;
				case "dateofbirth":
					cmdUpdate();
					client.setDateOfBirth(Date.valueOf(cmdNext()));
					break;
				}
				break;
				
			// Swiped functions
			case "swiped":
				cmdUpdate();
				rosinId = Integer.parseInt(cmdNext());
				cmdUpdate();
				switch(cmdNext()) {
				default:
					println("ERROR: swiped with no definition " + rosinId);
					break;
				case "like":
					println(username + " liked " + rosinId);
					break;
				case "superlike":
					println(username + " superliked " + rosinId);
					break;
				case "dislike":		//rename?
					println(username + " disliked " + rosinId);
					break;
				}
				break;
			}
			lastMessage = LocalDateTime.now();
		}
		
		private void respond(String function, int responseCode) {
			println("sending response " + function + " " + responseCode);
			output.println("response " + function + " " + responseCode);
		}
		
		// manage session somehow, so that it times out after not having any activity for some time, keep checking if rosin has correct session id
		
		private String cmdNext() {
			if(request.contains(" ")) {
				return request.substring(0, request.indexOf(" "));
			} else {
				return request.substring(0);
			}
		}
		
		private void cmdUpdate() {
			request = request.substring(request.indexOf(" ") + 1);
		}
		
		private Rosin getRosin(int id) {
			for(int i = 0; i < rosinad.rosinad.size(); i++) {
				if(rosinad.rosinad.get(i).id == id) return rosinad.rosinad.get(i);
			}
			println("ERROR: could not get rosin in getRosin " + id);
			return null;
		}
		
		/**
		 * Get random rosin from the list of registered rosins
		 * Returns random rosin
		 * 
		 * @return
		 */
		private Rosin getRandomRosin() {
			//TODO: re-do this entire function, maybe instead of returnng a rosin it sends a response code or whatever
			int rosinId;
			//TODO: add checking to see if user has swiped everybody
			while(true) {
				if(client.getSwiped().size() == rosinad.rosinad.size() - 1) {
					// all have been swiped
					return new Rosin();
				}
				rosinId = (int) (Math.random() * rosinad.rosinad.size());
				if(!client.getSwiped().contains(rosinId) && rosinId != client.getId()) {
					break;
				}
				
			}
			return getRosin(rosinId);
			//TODO: make checking server sided
		}
		
		/**
		 * Register an the user.
		 * Returns integer value as status
		 * 	Response code:
		 * 		0	-	no errors, registration successful
		 * 		1	-	something went wrong, not anything related to other codes
		 * 		11	-	username already taken
		 * 
		 * @param username
		 * @param password
		 * @param email
		 * @return
		 */
		private int register(String username, String password, String email) {
			if(username.isBlank() || password.isBlank()) return 1;
			boolean clientExists = false;
			for(RosinClient rosin : system.data.getRosinClients()) {
				if(rosin.getUsername().equalsIgnoreCase(username)) {
					clientExists = true;
					break;
				}
			}
			if(clientExists) {
				log("Failed to register '" + username + "': username taken");
				return 11;
			}
			system.data.registerRosin(username, password);
			log(username + " registered");
			return 0;
		}
		
		/**
		 * Log in with provided username and password.
		 * Return integer value of status
		 * 	Response code:
		 * 		0	-	Success
		 * 		1	-	undefined error
		 * 		11	-	invalid username
		 * 		12	-	invalid password
		 * 		21	-	locked
		 * 		22	-	banned
		 * 
		 * @param username
		 * @param password
		 * @return response code
		 */
		private int login(String username, String password) {
			if(username.isBlank() || password.isBlank()) return 1;
			for(RosinClient rosin : system.data.getRosinClients()) {
				if(rosin.getUsername().equalsIgnoreCase(username)) {
					if(rosin.getPassword().equals(password)) {
						if(rosin.getStatus() == 0) {				//account status normal
							log(username + " logged in");
							return 0;
						} else if(rosin.getStatus() == 1) {
							log(username + " login failed: account locked");
							return 21;
						} else if(rosin.getStatus() == 2) {		//account banned
							log(username + " login failed: account banned");
							return 22;
						}
					} else {
						log(username + " login failed: invalid password");
						return 12;
					}
				}
			}
			log(username + " login failed: invalid username");
			return 11;
		}
	}
	
	public class Rosin {
		private int id;
		private String login;
		private String displayname;
		private int age;
		private String location;
	}
}