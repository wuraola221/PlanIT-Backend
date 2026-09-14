package PlanIT.PlanIT.repository;

import PlanIT.PlanIT.dto.TaskDto;
import PlanIT.PlanIT.entity.ERole;
import PlanIT.PlanIT.entity.ProfileEntity;
import PlanIT.PlanIT.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByAssignedTo(ProfileEntity assignedTo);

    List<TaskEntity> findByAssignedToId(Long assignedTo);

    boolean existsByIdAndAssignedTo_Id(Long taskId, Long developerId);

    long countByCreatedBy_email(String leadEmail);


    @Query("SELECT DISTINCT p.id, p.fullName, p.email " +
            "FROM ProfileEntity p " +
            "WHERE p.role.name = :role " +
            "ORDER BY p.fullName")
    List<Object[]> findAllDevelopers(@Param("role") ERole role);

    List<TaskEntity> findByCreatedBy_email(String leadEmail);

    List<TaskEntity> findByAssignedToEmail(String developerEmail);



    @Query("SELECT t.assignedTo.email, t.assignedTo.fullName, COUNT(t) " +
            "FROM TaskEntity t " +
            "WHERE t.createdBy.email = :leadEmail " +
            "GROUP BY t.assignedTo.email, t.assignedTo.fullName")
    List<Object[]> findDevelopersAndTaskCountByCreatedBy_email(String leadEmail);
}

