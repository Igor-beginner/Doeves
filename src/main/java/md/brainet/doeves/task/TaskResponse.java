package md.brainet.doeves.task;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskResponse{
    private int id;
    private String message;

    public TaskResponse(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public TaskResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }
}
