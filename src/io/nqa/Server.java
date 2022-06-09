package io.nqa;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.nqa.Sys.Color;

public class Server {
	public Sys system;
	private ServerSocket listener;
	private int port = 14788;
	public ArrayList<ClientHandler> clients = new ArrayList<>();
	public int poolThreads = 64;
	private ExecutorService pool;
	PrintWriter output;
	BufferedReader input;
	public boolean isAlive;
	private boolean keepAlive;
	public int keepAliveRate = 10000;
	public String welcomeMsg = "Welcome to the NQA server!";
	
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!system.date.isEqual(LocalDate.now())) {
				system.newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.Server + " Server: " + Color.Default + t + Color.Input);
			system.appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	private void log(String str) {
		system.log("Server:	" + str);
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			keepAlive();
		}
	};
	Thread thread = new Thread(runnable);
	
	public Server(Sys system) {
		this.system = system;
	}
	
	public void initialize() {
		try {
	//		if(system.serviceEnabledServer) {
				println(Color.Success + "Initializing");
				log("Initializing");
				keepAlive = true;
				pool = Executors.newFixedThreadPool(poolThreads);
				listener = new ServerSocket(port);
				thread.setName("Thread-Server");
				thread.start();
				isAlive = true;
	//		} else {
	//			println(Color.Error + "Server service is disabled");
	//		}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void keepAlive() {		//make it check if clients are still connected and drop them??
		while(keepAlive) {
			/**
			 * Check if a client has dropped connection and kick them off.
			 */
			/*if(clients.size() > 0) for(int i = 0; i < clients.size(); i++) {
//				println(clients.size() + " clients connected");		// do something with this
				if(!clients.get(i).client.isConnected()) println("disconnected");	//teest thos at home
//				clients.get(i).client.close();
			}*/
			if(clients.size() > 0) for(ClientHandler clientHandler : clients) {
				if(!clientHandler.client.isConnected() || clientHandler.client.isClosed()) {
					println(clientHandler.name + " disconnected");
					log(clientHandler.name + " disconnected");
				}
			}
			try {
				Socket client = listener.accept();
				println(client.getInetAddress());
				ClientHandler clientThread = new ClientHandler(system, client);
				clients.add(clientThread);
				pool.execute(clientThread);
				Thread.sleep(keepAliveRate);
			} catch(Exception e) {
				if(e.getClass() == java.net.SocketException.class) println(Color.Error + e.getMessage());
				else e.printStackTrace();
			}
		}
		println(Color.Critical + "keepAlive thread terminated");
	}
	
	public void msgToServer(String sender, String msg) {
		if(!sender.isBlank() && !msg.isBlank() && clients.size() > 0) {
			for(int i = 0; i < clients.size(); i++) {			//This is wrong, make it check if client is even online, otherwise it will crash the entire program
				clients.get(i).send(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + Color.Client + sender + Color.Default + ": " + msg + Color.Default);
			}
			println(Color.Client + sender + ": " + Color.Default + msg + Color.Default);
		}
		println("'msgToServer' disabled -ln:96");
	}
	
	public void shutdown() {
		try {
			keepAlive = false;
			thread.interrupt();
			pool.shutdownNow();
			listener.close();
			isAlive = false;
		} catch(NullPointerException | IOException e) {
			if(e.getClass() != NullPointerException.class) e.printStackTrace();
		}
		println(Color.Critical + "Shutdown");
		log("Shutdown");
	}
}
