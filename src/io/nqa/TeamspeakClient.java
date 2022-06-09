package io.nqa;

import java.time.LocalDateTime;
import java.util.List;

public class TeamspeakClient {
	private String cuid;						//Client Unique ID
	private int cid;							//Channel ID
	private int cdbid;							//Client Database ID
	private int clid;							//Client ID
	private String nickname;					//Last nickname
	private String description;
	private String ip;							//Last IP address
	private LocalDateTime firstSeen;			//First time connected
	private LocalDateTime lastSeen;				//Last time connected
	private boolean isConnected;				//Is the client currently connected?
	// Reorganize those damn variables into an better order.
	private List<TeamspeakGroup> serverGroups;

	/**
	 * Init empty client, is only used to initiate before a loop, where existing client will be assigned.
	 */
	public TeamspeakClient() {
		//
	}
	
	public TeamspeakClient(String cuid, int cdbid, LocalDateTime firstSeen) {
		this.cuid = cuid;
		this.cdbid = cdbid;
		this.firstSeen = firstSeen;
	}
	
	public String getCuid() {
		return this.cuid;
	}
	
	public void setCid(int cid) {
		this.cid = cid;
	}
	
	public int getCid() {
		return this.cid;
	}
	
	public int getCdbid() {
		return this.cdbid;
	}
	
	public void setClid(int clid) {
		this.clid = clid;
	}
	
	public int getClid() {
		return this.clid;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return this.nickname;
	}

	public void setDescription(String newDescription) {
		this.description = newDescription;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public LocalDateTime getFirstSeen() {
		return this.firstSeen;
	}
	
	public void setLastSeen(LocalDateTime lastSeen) {
		this.lastSeen = lastSeen;
	}
	
	public LocalDateTime getLastSeen() {
		return this.lastSeen;
	}
	
	public void setIsConnected(boolean connected) {
		isConnected = connected;
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}

	public void setServerGroups(List<TeamspeakGroup> groups) {
		this.serverGroups = groups;
	}

	public List<TeamspeakGroup> getServerGroups() {
		return this.serverGroups;
	}

	public void addServergroup(TeamspeakGroup group) {
		this.serverGroups.add(group);
	}

	public void removeServerGroup(int groupId) {
		for(TeamspeakGroup group : serverGroups) {
			if(group.getGroupId() == groupId) this.serverGroups.remove(group);
		}
	}
}
