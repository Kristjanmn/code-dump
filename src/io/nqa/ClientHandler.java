package io.nqa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.nqa.Sys.Color;
import io.nqa.Sys.Role;

public class ClientHandler implements Runnable {
	public Sys system;
	public Socket client;
	private BufferedReader input;
	public PrintWriter output;
	private KeyPair keys;
	private PublicKey clientKey;
	private boolean keysExchanged;
	private String clientKeyString;
	private boolean gettingPublicKey;
	private boolean gettingLargeMessage;
	private String largeMessage;
	
	private String request;
	
	public int keyBits = 4096;		//Default 8192	//make separate function for generating keys
	
	public String name = "Guest";
	public int accessLevel = 0;
	public Role role = Role.Guest;
	
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
	
	//cloud rename it to Client?
	public ClientHandler(Sys system, Socket clientSocket) {
		try {
			this.system = system;
			this.client = clientSocket;
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);
			println(client.getRemoteSocketAddress() + " connected");
			
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(keyBits);
			keys = keyPairGen.generateKeyPair();
		} catch(IOException | NoSuchAlgorithmException e) {
			if(e.getClass() == java.lang.NullPointerException.class) println("Null pointer message: " + e.getMessage());	//client was dropped before function gets called or something
			else e.printStackTrace();
		}
	}
	
	// I should probably update it to newer version, check Sys file method
	@Override
	public void run() {
		try {
			while(true) {
				//String request;
				try {
					request = input.readLine();
					if(keysExchanged) {
						Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
						request = new String(cipher.doFinal(Base64.getDecoder().decode(request)));
					}
				} catch(IOException e) {
					println(Color.Client + name + Color.Server + "@" + client.getRemoteSocketAddress() + ": Connection lost");
					break;
				}
				if(gettingPublicKey) {
					if(!request.contentEquals("EndPublicKey")) {
						clientKeyString = request;
					} else {
						try {
							gettingPublicKey = false;
							keysExchanged = true;
							byte[] publicByte = Base64.getDecoder().decode(clientKeyString);
							KeyFactory fact = KeyFactory.getInstance("RSA");
							clientKey = fact.generatePublic(new X509EncodedKeySpec(publicByte));
							X509EncodedKeySpec spec = fact.getKeySpec(keys.getPublic(), X509EncodedKeySpec.class);
							output.println("BeginPublicKey\n" + Base64.getEncoder().encodeToString(spec.getEncoded()) + "\nEndPublicKey");	//returns client's own public key???
							println("Exchanged keys with " + name + "@" + client.getInetAddress().toString() + ":" + client.getPort());
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
				if(!request.contentEquals("BeginPublicKey") && !request.contentEquals("EndPublicKey") && !gettingPublicKey) {
					println(Color.Client + name + Color.Server + "@" + client.getInetAddress().toString() + ":" + client.getPort() + ": " + Color.Default + request);
					log(name + "@" + client.getInetAddress().toString() + ":" + client.getPort() + ": " + request);
				}
				if(request == null) break;
				int firstSpace = request.indexOf(" ");
				String subString = request.substring(firstSpace+1);
				boolean space = firstSpace != -1;
				if(request.equals("BeginPublicKey")) gettingPublicKey = true;
				else if(request.equals("EndPublicKey")) send(system.server.welcomeMsg);
				//else if(gettingLargeMessage) largeMessage += request;
				else if(request.equals("BeginLargeMessage")) gettingLargeMessage = true;
				else if(request.equals("EndLargeMessage")) {
					gettingLargeMessage = false;
					println(largeMessage);	//???
				} else if(!gettingPublicKey && keysExchanged) {
					if(!request.isBlank()) execRequest();
					
					// Ready for commands
					if(request.startsWith("!commands")) {
						send("Commands:\n"
								+ "!info - displays information about the server or something\n"
								+ "  system - displays information about the system\n"
								+ "  server - displays information about the server\n"
								+ "  teamspeak - displays information about the teamspeak server/bot\n"
								+ "!help - display help message\n"
								+ "!commands - display commands\n"
								+ "!status - current status\n"
								+ "  server\n"
								+ "  teamspeak\n"
								+ "  server\n"
								+ "say - send a text message over server\n"
								+ "!ts - TeamSpeak commands\n"
								+ "  msg - send a text message to TeamSpeak\n"
								+ "!set - set client parameters\n"
								+ "  name - set name\n"
								+ "!start - start a service\n"	//todo
								+ "  teamspeak || ts\n"
								+ "!restart - restart a service\n"	//todo
								+ "  * || all - restart every service\n"
								+ "  running - restart runnign services\n"
								+ "  server - restart the main command/chat server/service\n"
								+ "  teamspeak - restart TeamSpeak bot\n"
								+ "!shutdown - shut a service down\n"
								+ "  system - shuts down the whole system\n"
								+ "  server - shuts down the main command/messaging server/service\n"
								+ "  teamspeak - shuts down the teamspeak bot\n");
					} else if(request.startsWith("!status")) {
						if(space) {
							switch(subString) {
							case "system":
								send("system status here");
								break;
							case "teamspeak":
								send("teamspeak status here");
								break;
							case "server":
								send("server status here");
								break;
							}
							send("status msg here");
						} else {
							send("!status lacks a parameter");
						}
					} else if(request.startsWith("!set")) {
						if(space && subString.startsWith("name")) {
							firstSpace = subString.indexOf(" ");
							subString = subString.substring(firstSpace+1);
							space = firstSpace != -1;
							if(space && !subString.isBlank()) {
								name = subString;
								send("Your name is now " + name);
							} else {
								send("!set name lacks a parameter");
							}
						} else {
							send("!set lacks a parameter, check !commands for commands");
						}
					} else if(request.startsWith("!ts")) {		//rename to teamspeak?
						if(space && subString.startsWith("msg")) {
							firstSpace = subString.indexOf(" ");
							subString = subString.substring(firstSpace+1);
							space = firstSpace != -1;
							if(space && !subString.isBlank()) {
								system.teamspeak.msgSystemToServer(name + "@" + client.getInetAddress().getHostAddress() + ": " + subString);
							} else {
								send("!ts msg lacks a parametere");
							}
						} else {
							send("!ts lacks a parameter, check !commands for commands");
						}
					} else if(request.startsWith("!start")) {
						system.start("");
					} else if(request.startsWith("!restart")) {
						system.restart(0);
					} else if(request.startsWith("!shutdown")) {
						if(space) {
							if(subString.equalsIgnoreCase("system")) {
								system.shutdown();
							} else if(subString.equalsIgnoreCase("teamspeak") || subString.equalsIgnoreCase("ts")) {
								if(system.teamspeak.isAlive) {
									send("Attempting TeamSpeak shutdown");
									//if(system.teamspeak.shutdown()) {		???
										send("TeamSpeak shutdown");
									//}
								} else {
									send("TeamSpeak already offline");
								}
							} else if(subString.equalsIgnoreCase("server")) {
								system.server.shutdown();
								if(system.teamspeak.isAlive) {
//									system.teamspeak.systemToServer("System: Server shutdown!");
								}
							}
						} else {
							send("!shutdown lacks a parameter");
						}
					} else {
						if(!gettingLargeMessage) send("Unknown command, write !help for help");
					}
				} else if(!gettingPublicKey) {
					println(Color.Client + name + Color.Warning + "@" + client.getInetAddress().toString() + " failed to authenticate");//\n") + Color.Default + request);
//					String reply = "Failed to authenticate";
//					output.println(reply);
					output.println(request);
					break;
				}
				if(gettingLargeMessage) largeMessage += request;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			output.close();
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Processes incoming request
	 */
	private void execRequest() {
		switch(reqNext()) {
		default:
			send("Invalid input");
			break;
			
		case "!info":		//make this more informative
			reqUpdate();
			switch(reqNext()) {
			default:
				if(reqNext().isBlank()) send("!info lacks a parameter, use !commands for use information");
				if(!reqNext().isEmpty()) send("!info does not accept '" + reqNext() + "'");
				break;
			case "system":
				//send("system info here");
				send("Not Quite Alright version" + system.version);
				break;
			case "server":
				send("server info ehre");
				send("Server status: " + system.getStatus(reqNext()));
				break;
			case "teamspeak":
				send("teamspeak info here");
				send("Teamspeak status: " + system.getStatus(reqNext()));
				break;
			}
			break;
			
		case "!help":
			reqUpdate();
			switch(reqNext()) {
			case "!help":
				send("!help - Help help the help help the help?");
				break;
			case "!info":
				send("!info - displays information about the specified system or service\n"
						+ "  system - displays the information about the system\n"
						+ "  server - displays the information about the command sever\n"
						+ "  teamspeak - displays information about the teamspeak service");
				break;
			case "!commands":
				send("!commands - displays all the available commands");
				break;
			case "!status":
				send("!status - returns information on the status of the thingy");
				break;
			}
			break;
			
		case "!commands":
			reqUpdate();
			send("commands here");
			break;
		
		case "!status":
			break;
			
		case "say":
			reqUpdate();
			if(!reqNext().isBlank()) outToAll(reqNext());
			break;
		
		case "!set":
			reqUpdate();
			switch(reqNext()) {
			default:
				break;
			case "name":
				reqUpdate();
				if(reqNext().isBlank()) send("no name provided");
				if(!reqNext().isBlank()) {
					name = reqNext();
					send("Your name is now " + name);
				}
				break;
			}
			break;
		}
	}
	
	/**
	 * Get next in request variable
	 * 
	 * @return
	 */
	private String reqNext() {
		if(request.contains(" ")) {
			return request.substring(0, request.indexOf(" "));
		} else {
			return request.substring(0);
		}
	}
	
	/**
	 * Update request variable
	 */
	private void reqUpdate() {
		request = request.substring(request.indexOf(" ") + 1);
	}
	
	public void send(String message) {
		try {
			if(message.getBytes().length > (keyBits / 8 - 11)) sendLarge(message); //8k = 1013; 4k = 501 ;; key bits / 8 - 11 = max bytes
			else {
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, clientKey);
				cipher.update(message.getBytes());
				byte[] ciphered = cipher.doFinal();
				output.println(Base64.getEncoder().encodeToString(ciphered));
			}
		} catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	private void sendLarge(String message) {
		send("BeginLargeMessage");
		String messageSubString = message;
		while(true) {
			if(messageSubString.getBytes().length <= keyBits / 8 - 11) {
				send(messageSubString);
				break;
			}
			send(messageSubString.substring(0, keyBits / 8 - 11));
			messageSubString = messageSubString.substring(keyBits / 8 - 11);
		}
		send("EndLargeMessage");
	}
	
	private void outToAll(String msg) {
		system.server.msgToServer(name + "@" + client.getInetAddress().toString(), msg);
	}
	
	private boolean authenticate(String username, String password) {
		return true;
	}
}
