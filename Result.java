//Result.java

import java.io.Serializable;

public class Result implements Serializable {
    private String password;
    private long timeTaken;
    private int threadId;
    private int serverId;
    private String targetHash; // This is optional, depending on the constructor you want

    // Constructor with the targetHash parameter
    public Result(String password, long timeTaken, int threadId, int serverId, String targetHash) {
        this.password = password;
        this.timeTaken = timeTaken;
        this.threadId = threadId;
        this.serverId = serverId;
        this.targetHash = targetHash; // Optional field
    }

    // Getters
    public String getPassword() {
        return password;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getServerId() {
        return serverId;
    }

    public String getTargetHash() {
        return targetHash;
    }
}
