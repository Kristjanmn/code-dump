package io.nqa.model;

public class Message {
    private int id;
    private String clientUniqueId;
    private String subject;
    private String message;
    private Long timestamp;

    public Message(int messageId, String clientUniqueId, String subject, String message, Long timestamp) {
        this.id = messageId;
        this.clientUniqueId = clientUniqueId;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return this.id;
    }

    public String getClientUniqueId() {
        return this.clientUniqueId;
    }

    public String getMessage() {
        return this.message;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }
}
