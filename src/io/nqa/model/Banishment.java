package io.nqa.model;

// TODO: Finish this class.
public class Banishment {
    private int id;
    private String targetIpAddress;         // Can only be one of those 4
    private String targetName;
    private String targetUniqueId;
    private String targetMyTeamSpeakId;
    private String targetLastNickname;      // Tends to be empty
    private Long createdTimestamp;
    private Long duraction;                 // Duration 0 never expires
    private String invokerName;
    private int invokerDatabaseId;
    private String invokerUniqueId;
    private String reason;
    private int enforcement;                // Seems to always be zero

    /** Add "target type" variable. */
    public Banishment(String target, Long created, Long duration, String invokerName, int invokerDatabaseId, String invokerUniqueId, String reason) {
        // check which target type and assign only one.
        // this.targetSomething = target;
        this.createdTimestamp = created;
        this.duraction = duration;
        this.invokerName = invokerName;
        this.invokerDatabaseId = invokerDatabaseId;
        this.invokerUniqueId = invokerUniqueId;
        this.reason = reason;
    }

    public String getTarget() {
        return "";  //return target based on target type
    }

    public Long getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public Long getDuraction() {
        return this.duraction;
    }

    public String getInvokerName() {
        return this.invokerName;
    }

    public int getInvokerDatabaseId() {
        return this.invokerDatabaseId;
    }

    public String getInvokerUniqueId() {
        return this.invokerUniqueId;
    }

    public String getReason() {
        return this.reason;
    }
}
