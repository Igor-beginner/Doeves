package md.brainet.doeves.task;

public class TaskResponse{
    private String message;
    private Task task;


    public TaskResponse(Task task) {
        this.task = task;
    }

    public TaskResponse(String message) {
        this.message = message;

    }


    public TaskResponse(String message, Task task) {
        this.message = message;
        this.task = task;
    }

    public String getMessage() {
        return message;
    }

    public Task getTask() {
        return task;
    }
}
