package PlanIT.PlanIT.entity;

import lombok.Getter;

@Getter
public enum TaskType {
    BUG("Bug", "Issues that need to be fixed"),
    INCIDENT("Incident", "Urgent issues affecting system"),
    MAINTENANCE("Maintenance", "Routine maintenance tasks"),
    FEATURE("Feature", "New feature development");

    private final String displayName;
    private final String description;

    TaskType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
