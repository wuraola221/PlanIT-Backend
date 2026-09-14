package PlanIT.PlanIT.service;


import PlanIT.PlanIT.dto.DeveloperTaskCount;
import PlanIT.PlanIT.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TaskRepository taskRepository;

    public List<DeveloperTaskCount> getDevelopersAndTaskCount(String leadEmail) {
        List<Object[]> results = taskRepository.findDevelopersAndTaskCountByCreatedBy_email(leadEmail);

        return results.stream()
                .map(row -> new DeveloperTaskCount(
                        ((String) row[0]),           // developer email
                        (String) row[1],           // developer name
                        ((Long) row[2])            // task count
                ))
                .toList();
    }
}
