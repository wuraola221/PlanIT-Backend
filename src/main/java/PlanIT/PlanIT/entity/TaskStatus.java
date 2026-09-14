package PlanIT.PlanIT.entity;

import lombok.Getter;

@Getter
public enum TaskStatus {

    TODO("To Do", "Task is ready to be worked on"),
    IN_PROGRESS("In Progress", "Task is currently being worked on"),
    BLOCKED("Blocked", "Task is blocked by dependencies"),
    COMPLETED("Completed", "Task has been completed");

    private final String displayName;
    private final String description;

    TaskStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isActive() {
        return this != COMPLETED;
    }

//    public boolean isInProgress() {
//        return this == IN_PROGRESS;
//    }
}

