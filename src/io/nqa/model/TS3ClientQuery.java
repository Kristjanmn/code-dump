package io.nqa.model;

public class TS3ClientQuery {
    /**
     * Make somehing to read address, port and apikey from a file (maybe GUI in future).
     */

    // TODO: Make it support multiple servers.

    private String address = "localhost";
    private int port = 25639;
    private String apiKey = "VQRY-3GLO-ZBDE-AF4Q-SC50-FS01";
    Connection connection = Connection.getConnection();

    public TS3ClientQuery() {
        System.out.println("query");
        connection.connect(address, port, apiKey);
    }

    public TS3ClientQuery(String address, int port, String apikey) {
        connection.connect(address, port, apikey);
    }
}
