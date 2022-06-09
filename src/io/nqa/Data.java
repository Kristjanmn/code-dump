package io.nqa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import io.nqa.Sys.Color;

public class Data {
	//Maybe keep everything in databases as encrypted, encrypt on write and decrypt on read
	private Sys system;
	private ArrayList<Client> clients = new ArrayList<Client>();
	private ArrayList<ServerClient> serverClients = new ArrayList<ServerClient>();
	private ArrayList<TeamspeakClient> teamspeakClients = new ArrayList<TeamspeakClient>();
	private ArrayList<RosinClient> rosinClients = new ArrayList<RosinClient>();
	
	private ArrayList<String> teamspeakBans = new ArrayList<String>();			//Array of 
	
	private String dataFileName = "data";
	private String rawDataFileName = "data.raw";
	private String cryptoFileName = "data.key";
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String dataLine = "";
	private boolean loading;
	
	
	@SafeVarargs
	private <T> void println(T... ts) {
		for(T t : ts) {
			if(!system.date.isEqual(LocalDate.now())) {
				system.newDate();
			}
			System.out.println(Color.Time + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + Color.Server + " Data: " + Color.Default + t + Color.Input);
			system.appendSessionLog(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + " " + t);
		}
	}
	
	private void log(String str) {
		system.log("Data:		" + str);
	}
	
	@SuppressWarnings("resource")
	public Data(Sys system) {
		this.system = system;
		File cryptoFile = new File(cryptoFileName);
		
		if(cryptoFile.exists()) {
			loadKeys();
		} else {
			generateKeys();
		}
		load();
	}
	
	private void generateKeys() {
		try {
			byte[] cryptoPublicByte = Base64.getDecoder().decode("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");
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PublicKey cryptoPublic = factory.generatePublic(new X509EncodedKeySpec(cryptoPublicByte));
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(4 * 1024);	//16
			KeyPair keys = keyPairGen.generateKeyPair();
			//KeyFactory factory = KeyFactory.getInstance("RSA");
			publicKey = keys.getPublic();
			String publicKeyString = Base64.getEncoder().encodeToString(factory.getKeySpec(publicKey, X509EncodedKeySpec.class).getEncoded());
			privateKey = keys.getPrivate();
			String privateKeyString = Base64.getEncoder().encodeToString(factory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded());
			//println("keys: " + publicKeyString + "\n" + privateKeyString);		//correct
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cryptoPublic);
			cipher.update(publicKeyString.getBytes());
			publicKeyString = Base64.getEncoder().encodeToString(cipher.doFinal());
			int split = privateKeyString.length() / 2;
			String private1 = privateKeyString.substring(0, split);
			String private2 = privateKeyString.substring(split);
			cipher.update(private1.getBytes());
			private1 = Base64.getEncoder().encodeToString(cipher.doFinal());
			cipher.update(private2.getBytes());
			private2 = Base64.getEncoder().encodeToString(cipher.doFinal());
			FileWriter fileWriter = new FileWriter(cryptoFileName);
			fileWriter.write(publicKeyString + "\n" + private1 + "\n" + private2);
			fileWriter.close();
		} catch(InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	private void loadKeys() {
		try {
			File cryptoFile = new File(cryptoFileName);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			byte[] cryptoPrivateByte = Base64.getDecoder().decode("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");
			PrivateKey cryptoPrivate = factory.generatePrivate(new PKCS8EncodedKeySpec(cryptoPrivateByte));
			Scanner scanner = new Scanner(cryptoFile);
			String publicKeyString = scanner.nextLine();
			String privateKeyString1 = scanner.nextLine();
			String privateKeyString2 = scanner.nextLine();
			scanner.close();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, cryptoPrivate);
			cipher.doFinal(Base64.getDecoder().decode(publicKeyString));
			byte[] publicByte = Base64.getDecoder().decode(cipher.doFinal(Base64.getDecoder().decode(publicKeyString)));
			byte[] privateByte = Base64.getDecoder().decode((new String(cipher.doFinal(Base64.getDecoder().decode(privateKeyString1))) + new String(cipher.doFinal(Base64.getDecoder().decode(privateKeyString2)))).getBytes());
			//KeyFactory factory = KeyFactory.getInstance("RSA");
			publicKey = factory.generatePublic(new X509EncodedKeySpec(publicByte));
			privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateByte));
			if(publicKey == null || privateKey == null) println("keys are null");
		} catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs save(false, false).
	 */
	public void save() {
		save(false, false);
	}
	
	// WRITE ALL THE NEEDED DATA INTO ARRAYS IN MEMORY AND OVER TIME OR ON EXIT SAVE ALL THE DATA IN A FILE IN ENCRYPTED FORM, LIKE LARGE MESSAGE AS BYTES!!!
	// Use some sort of key, hardcoded or hide it somewhere 
	public void save(boolean saveRaw, boolean keepRaw) {
		//println("start save");
		if(loading) return;		// Prevent saving when loading to avoid infinite loop
		try {
			String clientEncrypted = "";
			String clientUnencrypted = "";
			String serverEncrypted = "";
			String serverUnencrypted = "";
			String teamspeakEncrypted = "";
			String teamspeakUnencrypted = "";
			String rosinEncrypted = "";
			String rosinUnencrypted = "";
//			println("starting save");
			
			//create file
			File dataFile = new File(dataFileName);
			dataFile.createNewFile();
			
			//Client
			for(Client client : clients) {
				String data = "BeginClient" + " guid=" + client.guid + " username=" + client.username + " password=" + client.password + " serverGuid=" + client.serverGuid + " teamspeakGuid=" + client.teamspeakGuid + " firstSeen=" + client.firstSeen + " lastSeen=" + client.lastSeen + " EndClient";
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				cipher.update(data.getBytes());
				clientUnencrypted += data + "\n";
				clientEncrypted += Base64.getEncoder().encodeToString(cipher.doFinal()) + "\n";
			}
			
			//Server
			for(ServerClient client : serverClients) {
				String data = "BeginServerClient" + " guid=" + client.guid + " nickname=" + client.nickname + " firstSeen=" + client.firstSeen + " lastSeen=" + client.lastSeen + " EndServerClient";
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				cipher.update(data.getBytes());
				teamspeakUnencrypted += data + "\n";
				teamspeakEncrypted += Base64.getEncoder().encodeToString(cipher.doFinal()) + "\n";
			}
			
			//TeamSpeak
			for(TeamspeakClient client : teamspeakClients) {
				String data = "BeginTeamspeakClient" + " cuid=" + client.getCuid() + " cdbid=" + client.getCdbid() + " nickname='" + client.getNickname().replaceAll(" ", "_") + "' ip=" + client.getIp() + " firstSeen=" + client.getFirstSeen() + " lastSeen=" + client.getLastSeen() + " EndTeamspeakClient";
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				cipher.update(data.getBytes());
				teamspeakUnencrypted += data + "\n";
				teamspeakEncrypted += Base64.getEncoder().encodeToString(cipher.doFinal()) + "\n";
			}
			
			//Rosinad
			for(RosinClient client : rosinClients) {
				String data = "BeginRosinClient" + " guid=" + client.getGuid() + " id=" + client.getId() + " username=" + client.getUsername() + " password=" + client.getPassword() + " displayname=" + client.getDisplayName() + " EndRosinClient";	//TODO: add array stuff aswell
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				cipher.update(data.getBytes());
				rosinUnencrypted += data + "\n";
				rosinEncrypted += Base64.getEncoder().encodeToString(cipher.doFinal()) + "\n";
			}
			
			FileWriter dataWriter = new FileWriter(dataFileName);
			if(saveRaw) {		//for changing encryption and whatnot
				println(Color.Critical + "Saving raw data");
				if(!keepRaw) dataFile.delete();		//dont know if i should try to delete key in here aswell
				dataWriter = new FileWriter(rawDataFileName);		//don't know why it says it's not closed
				dataWriter.write(clientUnencrypted + serverUnencrypted + teamspeakUnencrypted + rosinUnencrypted);
				dataWriter.close();
				if(!keepRaw) system.restart(0);
			}
			else dataWriter.write(clientEncrypted + serverEncrypted + teamspeakEncrypted + rosinEncrypted);
			dataWriter.close();
		} catch(IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			println(Color.Critical + "An error occured while saving");
		}
	}
	
	public void load() {
		loading = true;
		LocalDateTime startTime = LocalDateTime.now();
		File dataFile = new File(dataFileName);
		File rawDataFile = new File(rawDataFileName);
		boolean loadRaw = false;
		if(rawDataFile.exists()) {
			loadRaw = true;
			if(dataFile.exists()) dataFile.delete();
			dataFile = new File(rawDataFileName);
			println(Color.Warning + "Loading unencrypted data");
			log("Loading unencrypted data");
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		if(dataFile.exists()) {
			if(!dataFile.canRead()) println(Color.Error + "Can't read data file");
			clients.clear();
			serverClients.clear();
			teamspeakClients.clear();
			rosinClients.clear();
			try {
				Scanner scanner = new Scanner(dataFile);
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine();				//make it use some append shit instead, so that variable name is defined on same line as variable
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
					//println(privateKey.toString().length() + "\n" + line);
					if(!loadRaw && !line.startsWith("Begin") && !line.isBlank()) dataLine = new String(cipher.doFinal(Base64.getDecoder().decode(line)));
					else dataLine = line;
					
					if(dataLine.startsWith("BeginClient")) {
						Client client = new Client();
						lineUpdate();
						while(!dataLine.startsWith("EndClient")) {
							if(dataLine.isBlank()) break;
							if(dataLine.startsWith("guid")) client.guid = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("username")) client.username = lineVariable();
							else if(dataLine.startsWith("password")) client.password = lineVariable();
							else if(dataLine.startsWith("serverGuid")) client.serverGuid = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("teamspeakGuid")) client.teamspeakGuid = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("firstSeen")) client.firstSeen = LocalDateTime.parse(lineVariable(), formatter);
							else if(dataLine.startsWith("lastSeen")) client.lastSeen = LocalDateTime.parse(lineVariable(), formatter);
							else System.out.println("DATA: Unknown client line: " + dataLine);
							lineUpdate();
						}
						updateClient(client);
					}
					
					if(dataLine.startsWith("BeginServerClient")) {
						ServerClient client = new ServerClient();
						lineUpdate();
						while(!dataLine.startsWith("EndServerClient")) {
							if(dataLine.isBlank()) break;
							if(dataLine.startsWith("guid")) client.guid = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("nickname")) client.nickname = lineVariable();
							else if(dataLine.startsWith("firstSeen")) client.firstSeen = LocalDateTime.parse(lineVariable(), formatter);
							else if(dataLine.startsWith("lastSeen")) client.lastSeen = LocalDateTime.parse(lineVariable(), formatter);
							else System.out.println("DATA: Unknown client line: " + dataLine);
							lineUpdate();
						}
						updateServerClient(client);
					}
					
					if(dataLine.startsWith("BeginTeamspeakClient")) {
						String cuid = "";
						int cdbid = 0;
						int clid = 0;
						String nickname = "";
						String ip = "";
						LocalDateTime firstSeen = LocalDateTime.MIN;
						LocalDateTime lastSeen = LocalDateTime.MIN;
						
						lineUpdate();
						while(!dataLine.startsWith("EndTeamspeakClient")) {
							if(dataLine.isBlank()) break;
							if(dataLine.startsWith("cuid")) cuid = lineVariable();
							else if(dataLine.startsWith("cdbid")) cdbid = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("nickname")) nickname = lineVariable().substring(1, lineVariable().length() - 1).replaceAll("_", " ");
							else if(dataLine.startsWith("ip")) ip = lineVariable();
							else if(dataLine.startsWith("firstSeen")) firstSeen = LocalDateTime.parse(lineVariable(), formatter);
							else if(dataLine.startsWith("lastSeen")) lastSeen = LocalDateTime.parse(lineVariable(), formatter);
							else System.out.println("DATA: Unknown teamspeak line: " + dataLine);
							lineUpdate();
						}
						TeamspeakClient teamspeakClient = new TeamspeakClient(cuid, cdbid, firstSeen);
						teamspeakClient.setClid(clid);
						teamspeakClient.setNickname(nickname);
						teamspeakClient.setIp(ip);
						teamspeakClient.setLastSeen(lastSeen);
						updateTeamspeakClient(teamspeakClient);
					}
					
					if(line.startsWith("BeginRosinClient")) {
						String guid = "";
						int id = 0;
						String username = "";
						String password = "";
						String displayName = "";
						int status = 0;
						LocalDateTime registered = LocalDateTime.MIN;
						LocalDateTime lastSeen = LocalDateTime.MAX;
						
						lineUpdate();
						while(!dataLine.startsWith("EndRosinClient")) {
							if(dataLine.isBlank()) break;
							if(dataLine.startsWith("guid")) guid = lineVariable();
							else if(dataLine.startsWith("id")) id = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("username")) username = lineVariable();
							else if(dataLine.startsWith("password")) password = lineVariable();
							else if(dataLine.startsWith("displayname")) displayName = lineVariable();
							else if(dataLine.startsWith("status")) status = Integer.parseInt(lineVariable());
							else if(dataLine.startsWith("registered")) registered = LocalDateTime.parse(lineVariable(), formatter);
							else if(dataLine.startsWith("lastSeen")) lastSeen = LocalDateTime.parse(lineVariable(), formatter);
							//add arrays
							lineUpdate();
						}
						
						// If user is deleted
						if(status == 3) {
							username = "";
							RosinClient client = new RosinClient(guid, id, username, LocalDateTime.MIN);
							updateRosinClient(client);
						} else if(guid.isBlank() || id == 0 || username.isBlank() || password.isBlank() || displayName.isBlank()) {
							log("Could not add rosin " + guid + "/" + username + " into database: missing variable(s)");
						} else {	//TODO: I should prolly init all the variables in the client
							if(registered.isEqual(LocalDateTime.MIN)) log(username + " egistered date is set to minimum");
							if(lastSeen.isEqual(LocalDateTime.MIN)) log(username + " lastSeen date is set to minimum");
							RosinClient client = new RosinClient(guid, id, username, LocalDateTime.MIN);
							client.setRegistered(registered);
							client.setLastSeen(lastSeen);
							updateRosinClient(client);
						}
					}
				}
				scanner.close();
				loading = false;
				
				if(loadRaw) {
					if(dataFile.exists()) dataFile.delete();
					File keyFile = new File(cryptoFileName);
					if(keyFile.exists()) keyFile.delete();
					generateKeys();
					save();
					system.restart(0);
				}
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				println(Color.Critical + "Error loading data file");
			}
		}
		else println(Color.Error + "Could not load data from file: File does not exist");
		long loadingTime = startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
		println(Color.Success + "Finished loading data in " + loadingTime + " seconds");
		log("Finished loading data in " + loadingTime + " seconds");
	}
	
	private void lineUpdate() {
		dataLine = dataLine.substring(dataLine.indexOf(" ") + 1);
	}
	
	private String lineVariable() {
		return dataLine.substring(dataLine.indexOf("=") + 1, dataLine.indexOf(" "));
	}
	
	public void purge(Boolean hard, String confirmation) {
		clients.clear();
		serverClients.clear();
		teamspeakClients.clear();
		if(hard) {
			File file = new File(dataFileName);
			file.delete();
		}
	}
	
	public static String generateGuid() {
		int guidLength = 32;
		String guidCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%&*()-_=+[{]}|<>/";
		String guid = "";
		for(int i = 0; i < guidLength; i++) {
			int character = (int) Math.floor(Math.random() * guidCharacters.length());
			guid += guidCharacters.substring(character, character+1);
		}
		return guid;
	}
	
	/**
	 * Fetches list of all clients from database.
	 * 
	 * @return
	 */
	public ArrayList<Client> getClients() {
		return clients;
	}
	
	/**
	 * Fetches list of all Server clients from database.
	 * 
	 * @return
	 */
	public ArrayList<ServerClient> getServerClients() {
		return serverClients;
	}
	
	/**
	 * Fetches list of all TeamSpeak clients from database	(not TeamSpeak server's database)
	 * 
	 * @return
	 */
	public ArrayList<TeamspeakClient> getTeamspeakClients() {
		return teamspeakClients;
	}
	
	/**
	 * Fetch list of all Rosin clients from database.
	 * 
	 * @return
	 */
	public ArrayList<RosinClient> getRosinClients() {
		return rosinClients;
	}
	
	public String registerUser(String username, String password) {
		boolean userExists = false;
		if(username.isBlank() || password.isBlank()) return "No username or password was given";
		for(int i = 0; i < getClients().size(); i++) {
			if(username == getClients().get(i).username) userExists = true;
		}
		if(!userExists) {
			int guidLength = 32;		// generateGUID function exists above
			String guidCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%&*()-_=+[{]}|<>/";
			String guid = "";
			for(int i = 0; i < guidLength; i++) {
				int character = (int) Math.floor(Math.random() * guidCharacters.length());
				guid += guidCharacters.substring(character, character+1);
//				println(character + " Total: " + guid + " TEST " + guidCharacters.substring(character, character+1));
			}
			println(guid);
		} else return "Username already taken";
		return "fail@data.107";
	}
	
	// TODO: make other update and register functions check if it has done so successfully?
	public void registerRosin(String username, String password) {
		int id = rosinClients.size() + 1;	//make some loop to make sure that another rosin with same id does not already exist
		RosinClient client = new RosinClient(generateGuid(), id, username, LocalDateTime.MIN);
		client.setPassword(password);
		client.setRegistered(LocalDateTime.now());
		
		// Some default variables
		client.setDisplayName(username);
		
		rosinClients.add(client);
		save();
	}
	
	public boolean updateClient(Client client) {
		return true;
	}
	
	/**
	 * Updates Server database with provided ServerClient.
	 * If the client isn't already in the database, then it will add it.
	 * 
	 * @param client
	 */
	public void updateServerClient(ServerClient client) {
		boolean clientExists = false;
		for(int i = 0; i < serverClients.size(); i++) {
			if(client.guid == serverClients.get(i).guid) {
				serverClients.set(i, client);
				clientExists = true;
			}
		}
		if(!clientExists) serverClients.add(client);
		save();
	}
	
	/**
	 * Updates TeamSpeak database with provided TeamspeakClient.
	 * If the client isn't already in the database, then it will add it.
	 * 
	 * @param client
	 */
	public void updateTeamspeakClient(TeamspeakClient client) {
		boolean clientExists = false;
		for(int i = 0; i < teamspeakClients.size(); i++) {
			if(client.getCuid().contentEquals(teamspeakClients.get(i).getCuid())) {
				teamspeakClients.set(i, client);
				clientExists = true;
			}
		}
		if(!clientExists) teamspeakClients.add(client);
		save();
	}
	
	public void updateRosinClient(RosinClient client) {
		boolean clientExists = false;
		for(int i = 0; i < rosinClients.size(); i++) {
			if(client.getGuid().equals(rosinClients.get(i).getGuid())) {
				rosinClients.set(i, client);
				clientExists = true;
			}
		}
		if(!clientExists) rosinClients.add(client);
		save();
	}
	
	public void setSessionId(int rosinId, String sessionId) {
		// update rosin and just set sessionid
	}
	
	public class Client {
		int guid;							//Generated Unique ID
		String username;					//Client's username used to log in
		String password;					//Client's hashed password
		int serverGuid;						//ServerClient 'guid' if client has connected
		int teamspeakGuid;					//Teamspeak 'guid' if client has connected
		LocalDateTime firstSeen;			//First time connected with this user
		LocalDateTime lastSeen;				//Last time connected, including Server/Teamspeak, if they are connected to client
		
		public int getGuid() {
			return this.guid;
		}
	}
	
	public class ServerClient {
		int guid;							//Generated Unique ID
		String nickname;					//Last nickname?
		LocalDateTime firstSeen;			//First time connected
		LocalDateTime lastSeen;				//Last time connected
		boolean isConnected;				//Is the client currently connected?
		
		public int getGuid() {
			return this.guid;
		}
	}
	
	// Maybe add description aswell
	/*public static class TeamspeakClient {
		String cuid;						//Client Unique ID
		int cid;							//Channel ID
		int cdbid;							//Client Database ID
		int clid;							//Client ID
		String nickname;					//Last nickname
		String ip;							//Last IP address
		LocalDateTime firstSeen;			//First time connected
		LocalDateTime lastSeen;				//Last time connected
		boolean isConnected;				//Is the client currently connected?
		
		public String getCuid() {
			return this.cuid;
		}
	}*/
}
