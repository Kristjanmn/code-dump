package io.nqa;

public class TeamspeakServer {
	private final String address;
	private final int port;
	private String username;
	private String password;
	private String nickname;
	
	public TeamspeakServer(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public String getServerName() {
		return "";
	}
}
