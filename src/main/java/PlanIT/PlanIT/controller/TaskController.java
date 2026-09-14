package PlanIT.PlanIT.controller;

import PlanIT.PlanIT.dto.SimpleDeveloperDto;
import PlanIT.PlanIT.dto.TaskDto;
import PlanIT.PlanIT.entity.TaskEntity;
import PlanIT.PlanIT.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

   @PreAuthorize( "hasAuthority('LEAD_DEVELOPER')")
    @PostMapping("/create-task")
   public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) {
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       return ResponseEntity.status(HttpStatus.CREATED)
               .body(taskService.createTask(taskDto, auth));
   }

   @PreAuthorize( "hasAuthority('LEAD_DEVELOPER')")
    @PutMapping("/update-task/{id}")
   public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       return ResponseEntity.ok(taskService.updateTask(id, taskDto, auth));
   }

    @PreAuthorize( "hasAuthority('DEVELOPER')")
    @PatchMapping ("/update-task-status")
    public ResponseEntity<TaskDto> updateTaskStatus(@RequestBody TaskDto taskDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(taskService.updateTaskStatus(taskDto.getId(), taskDto.getStatus()));
    }

   @PreAuthorize( "hasAuthority('LEAD_DEVELOPER')")
    @GetMapping("/lead/{leadEmail}")
    public List<TaskDto> getTasksByLead(@PathVariable String leadEmail) {
        return taskService.getTasksByLead(leadEmail);
    }

    @PreAuthorize("hasAuthority('LEAD_DEVELOPER')")
    @GetMapping("/lead/{leadEmail}/count")
    public ResponseEntity<Long> getTaskCountByLead(@PathVariable String leadEmail) {
        long count = taskService.getTaskCountByLead(leadEmail);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAuthority('LEAD_DEVELOPER')")
    @DeleteMapping("/delete-task/{id}")
    public ResponseEntity<TaskDto> deleteTask(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(taskService.deleteTask(id, auth));
    }

    @PreAuthorize("hasAuthority('LEAD_DEVELOPER')")
    @GetMapping("/developers/dropdown")
    public ResponseEntity<List<SimpleDeveloperDto>> getDevelopersForDropdown() {
        List<SimpleDeveloperDto> developers = taskService.getAllDevelopersForDropdown();
        return ResponseEntity.ok(developers);
    }

    @PreAuthorize("hasAnyAuthority('DEVELOPER', 'LEAD_DEVELOPER')")
    @GetMapping("/developer/{developerEmail}")
    public List<TaskDto> getTasksByDeveloperEmail(@PathVariable("developerEmail") String developerEmail) {
        return taskService.getTasksByDeveloperEmail(developerEmail)
                .stream()
                .map(taskService::toDto)
                .toList();
    }


}
