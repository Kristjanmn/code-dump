package io.nqa;

public class TeamspeakGroup {
    private int type = -1;
    /**
     * Types
     * 0 = Template
     * 1 = Normal
     * 2 = Query
     */
    private int groupId;
    private String name = "Unnamed";

    public TeamspeakGroup(int groupId) {
        this.groupId = groupId;
    }

    public TeamspeakGroup(int type, int groupId, String name) {
        this.type = type;
        this.groupId = groupId;
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public String getName() {
        return this.name;
    }
}
