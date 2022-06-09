package io.nqa;

public class Main {
	public static StringBuilder sessionLog = new StringBuilder();
	private static Sys system = new Sys();
	
	/**
	 * Useless little static piece of shit
	 * Use it to start the real program
	 */
	public static void main(String[] args) {
		system.initialize();
	}
	
	public static void restart() {
		system = null;
		system = new Sys();
		system.initialize();
	}
}
