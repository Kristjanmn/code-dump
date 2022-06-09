package io.nqa.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable {
    /**
     * Reads incoming messages.
     */

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String message;
    private boolean authenticated;

    private EventHandler eventHandler;

    @Override
    public void run() {
        try {
            System.out.println("start");
            while(true) {
                // Expect NullPointerException here
                this.message = input.readLine();
                System.out.println(message);
                if(message == null) break;  // bot is dead

                if(!message.isBlank()) {
                    if(message.startsWith("notify")) eventHandler.handleEvent(message);
                } else {
                    // Handle other type of incoming messages.
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Thread thread = new Thread(this);

    public Connection() {
        eventHandler = EventHandler.getEventHandler();
    }

    public static volatile Connection connection;
    public static Connection getConnection() {
        if(connection == null) {
            connection = new Connection();
        }
        return connection;
    }

    public void connect(String address, int port, String apiKey) {
        try {
            this.socket = new Socket(address, port);
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);

            thread.start();
            authenticate(apiKey);
            //send("clientnotifyregister schandlerid=0 event=any");
            send("serverconnectionhandlerlist");
            send("use schandlerid=1");
            send("whoami");
            send("serverconnectinfo");
            send("use schandlerid=2");
            send("whoami");
            send("serverconnectinfo");
            send("use schandlerid=3");
            send("whoami");
            send("serverconnectinfo");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        System.out.println("out: " + msg);
        this.output.println(msg);
    }

    private void authenticate(String apiKey) {
        send("auth apikey=" + apiKey);
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

    private String msgVariable() {
        // Used to crash when variable ended without space.
        int spcIdx = message.indexOf(" ");
        int eqIdx = message.indexOf("=");
        int brIdx = message.indexOf("|");	// not used
        if(spcIdx == -1) spcIdx = message.length();
        if(eqIdx == -1 || eqIdx > spcIdx) {
            System.out.println("Returning empty string from: " + message);
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

    private int msgVarUp_int() {
        String var = msgVarUp();
        int var2 = -1;
        if(var.isBlank()) {
            //System.out.println(Sys.Color.Error + "msgVarUp_int received empty string for integer. (" + command.toString() + ": " + message + ")");
            //return -1;	returns var2 in the end, -1 is by default
        }
        try {
            var2 = Integer.parseInt(var);
        } catch(NumberFormatException e) {
            System.out.println("Error from: " + var);
            e.printStackTrace();
        }
        return var2;
    }

    private long msgVarUp_long() {
        String var = msgVarUp();
        long var2 = -1;
        if(var.isBlank()) {
            //System.out.println(Sys.Color.Error + "msgVarUp_long received empty string for long. (" + command.toString() + ": " + message + ")");
            //return -1;	Same as with int method
        }
        try {
            var2 = Long.parseLong(var);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
        return var2;
    }

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
}
