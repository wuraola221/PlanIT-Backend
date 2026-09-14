package PlanIT.PlanIT.dto;

import PlanIT.PlanIT.entity.TaskStatus;
import PlanIT.PlanIT.entity.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import  java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Service
public class TaskDto {
    private long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskType taskType;
    private Long assignedTo;
    private String assignedToName;
    private Long createdBy;
    private LocalDate deadline;
    private LocalDate completedAt;

}
