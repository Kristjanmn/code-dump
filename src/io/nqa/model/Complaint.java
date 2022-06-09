package io.nqa.model;

public class Complaint {
    private int targetDatabaseId;
    private String targetName;
    private int invokerDatabaseId;
    private String invokerName;
    private String message;
    private Long timestamp;

    public Complaint(int targetDatabaseId, String targetName, int invokerDatabaseId, String invokerName, String message, Long timestamp) {
        this.targetDatabaseId = targetDatabaseId;
        this.targetName = targetName;
        this.invokerDatabaseId = invokerDatabaseId;
        this.invokerName = invokerName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getTargetDatabaseId() {
        return this.targetDatabaseId;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public int getInvokerDatabaseId() {
        return this.invokerDatabaseId;
    }

    public String getInvokerName() {
        return this.invokerName;
    }

    public String getMessage() {
        return this.message;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }
}
