package io.nqa;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class RosinClient {
	private final String guid;					//Generated Unique ID
	private final int id;						//Client ID					simplified ID version, not needed??		Maybe just used username instead?
	private final String username;				//Login username
	private String password;					//Hashed password
	private String displayName;					//Clients display name, visible for other clients
	private int age;
	private Date dateOfBirth;
	private String[] images;					//Array of images
	//swiped rosins and stuff??
	private List<Integer> swiped;				//Array of swiped simplified IDs
	private List<Integer> liked;				//Array of liked simplified IDs
	private List<Integer> likedBy;				//Array of simplified IDs, which have liked this Rosin
	private List<String> messages;				//Array of messages			need better implementation? prolly encrypted somehow
	//extra shit
	private int status = 0;						//0 - normal, 1 - locked, 2 - banned, 3 - deleted	//3 is just to occupy memory space for id variable..??
	private LocalDateTime registered;
	private LocalDateTime lastSeen;
	boolean isConnected;
	//wrong place?
	String lastSessionId;
	
	public RosinClient(String guid, int id, String username, LocalDateTime registered) {
		this.guid = guid;
		this.id = id;
		this.username = username;
		this.registered = registered;
	}
	
	public String getGuid() {
		return this.guid;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	public int getAge() {
		return this.age;
	}
	
	public void setDateOfBirth(Date date) {
		this.dateOfBirth = date;
	}
	
	public Date getDateOfBirth() {
		return this.dateOfBirth;
	}
	
	// images
	// Swipes
	
	public List<Integer> getSwiped() {
		return swiped;
	}
	
	// Likes
	
	public List<Integer> getLikes() {
		return liked;
	}
	
	public List<Integer> getLiked() {
		return likedBy;
	}
	
	// Messages - Maybe put in another database and stuff?
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public void setRegistered(LocalDateTime time) {
		this.registered = time;
	}
	
	public LocalDateTime getRegistered() {
		return this.registered;
	}
	
	public void setLastSeen(LocalDateTime time) {
		this.lastSeen = time;
	}
	
	public LocalDateTime getLastSeen() {
		return this.lastSeen;
	}
}
