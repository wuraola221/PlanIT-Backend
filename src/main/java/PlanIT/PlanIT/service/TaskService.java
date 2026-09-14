package PlanIT.PlanIT.service;

import PlanIT.PlanIT.dto.SimpleDeveloperDto;
import PlanIT.PlanIT.dto.TaskDto;
import PlanIT.PlanIT.entity.ERole;
import PlanIT.PlanIT.entity.ProfileEntity;
import PlanIT.PlanIT.entity.TaskEntity;
import PlanIT.PlanIT.entity.TaskStatus;
import PlanIT.PlanIT.repository.ProfileRepository;
import PlanIT.PlanIT.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import PlanIT.PlanIT.exception.InvalidDeadlineException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProfileRepository profileRepository;


    public TaskDto createTask(TaskDto taskDto, Authentication auth) {

        String creatorEmail = auth.getName();

        ProfileEntity creator = profileRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Lead Developer with email " + creatorEmail + " not found"));

        TaskEntity task = toEntity(taskDto, creator);


        if (taskDto.getAssignedTo() != null) {
            ProfileEntity assignee = profileRepository.findById(taskDto.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Developer with ID " + taskDto.getAssignedTo() + " not found"));
            task.setAssignedTo(assignee);
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        task.setCreatedBy(creator);

        task.setCompletedAt(null);

        if (task.getDeadline() == null || task.getDeadline().isBefore(ChronoLocalDate.from(LocalDate.now())) ) {
           throw new InvalidDeadlineException("Task deadline cannot be in the past");
        }

        TaskEntity savedTask = taskRepository.save(task);

        return toDto(savedTask);

    }

    public  TaskDto updateTask( Long taskId, TaskDto taskDto, Authentication auth) {
        String currentUserEmail = auth.getName();

        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found"));

        ProfileEntity currentUser = profileRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + currentUserEmail + " not found"));

          if (!existingTask.getCreatedBy().getEmail().equals(currentUserEmail)) {
              throw new ResourceNotFoundException("Task with ID " + taskId + " does not belong to current user");
        }

        if (taskDto.getTitle() != null) {
            existingTask.setTitle(taskDto.getTitle());
        }

        if (taskDto.getDescription() != null) {
            existingTask.setDescription(taskDto.getDescription());
        }

        if(taskDto.getTaskType() != null) {
            existingTask.setTaskType(taskDto.getTaskType());
        }

        if (taskDto.getStatus() != null) {
            existingTask.setStatus(taskDto.getStatus());
        }

        if (taskDto.getDeadline() != null) {
            existingTask.setDeadline(taskDto.getDeadline());
        }

        if(taskDto.getStatus() == TaskStatus.COMPLETED && existingTask.getCompletedAt() == null) {
            existingTask.setCompletedAt(LocalDate.now());
        }

//        if(taskDto.getCompletedAt() != null) {
//            existingTask.setCompletedAt(taskDto.getCompletedAt());
//        }

        if (taskDto.getAssignedTo() != null) {
            if (!taskDto.getAssignedTo().equals(existingTask.getAssignedTo().getId())) {

                boolean alreadyAssigned = taskRepository.existsByIdAndAssignedTo_Id(taskId, taskDto.getAssignedTo());
                if (alreadyAssigned) {
                    throw new IllegalStateException("Developer is already assigned to this task");
                }
                ProfileEntity newAssignee = profileRepository.findById(taskDto.getAssignedTo())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Developer with ID " + taskDto.getAssignedTo() + " not found"));
                existingTask.setAssignedTo(newAssignee);
            }
        } else if (taskDto.getAssignedTo() == null && existingTask.getAssignedTo() != null) {
            // If assignedTo is explicitly set to null, unassign the task
            existingTask.setAssignedTo(existingTask.getAssignedTo());
        }

           return toDto(taskRepository.save(existingTask)) ;
    }

    public TaskDto deleteTask(Long taskId, Authentication auth) {
        String currentUserEmail = auth.getName();
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found"));

        if(!existingTask.getCreatedBy().getEmail().equals(currentUserEmail)) {
            throw new ResourceNotFoundException("Task with ID " + taskId + " does not belong to current user");
        }

        taskRepository.deleteById(taskId);

        return toDto(existingTask);
    }

    public List<TaskDto> getTasksByLead(String leadEmail) {
        List<TaskEntity> taskEntities = taskRepository.findByCreatedBy_email(leadEmail);

        return taskEntities.stream().map(this::toDto).toList();
    }

    public long getTaskCountByLead(String leadEmail) {
        return taskRepository.countByCreatedBy_email(leadEmail);
    }


    public List<SimpleDeveloperDto> getAllDevelopersForDropdown() {
        List<Object[]> results = taskRepository.findAllDevelopers(ERole.DEVELOPER);

        return results.stream()
                .map(row -> SimpleDeveloperDto.builder()
                        .id((Long) row[0])
                        .fullName((String) row[1])
                        .email((String) row[2])
                        .build())
                .collect(Collectors.toList());
    }


    public TaskDto updateTaskStatus(Long taskId, TaskStatus newStatus) {
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with ID " + taskId + " not found"));

        existingTask.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED ) {
            existingTask.setCompletedAt(LocalDate.now());
        }

        TaskEntity updatedTask = taskRepository.save(existingTask);
        return toDto(updatedTask);
    }

    public List<TaskEntity> getTasksByDeveloperEmail(String developerEmail) {
        return taskRepository.findByAssignedToEmail(developerEmail);
    }

    public TaskDto toDto(TaskEntity task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .taskType(task.getTaskType())
                .assignedTo(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .createdBy(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .deadline((task.getDeadline()))
                .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getFullName() : null)
                .completedAt(task.getCompletedAt() != null ? task.getCompletedAt() : null)

                .build();
    }

    public TaskEntity toEntity(TaskDto taskDto, ProfileEntity creator) {
        ProfileEntity assignee = null;
        ProfileEntity assignee2 = null;
        if (taskDto.getAssignedTo() != null && taskDto.getAssignedTo() > 0) {
            Optional<ProfileEntity> optionalProfileEntity = profileRepository.findById(taskDto.getAssignedTo());

            if (optionalProfileEntity.isPresent()) {
                assignee = optionalProfileEntity.get();
            } else {
                throw new ResourceNotFoundException("Developer with ID " + taskDto.getAssignedTo() + " not found");
            }
        }

        return TaskEntity.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus())
                .taskType(taskDto.getTaskType())
                .createdBy(creator)
                .assignedTo(assignee)
                .assignedToName(assignee2)
                .deadline((taskDto.getDeadline() != null ? taskDto.getDeadline() : LocalDate.now().plusDays(3)))
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDate.now())
                .build();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}

