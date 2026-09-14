package PlanIT.PlanIT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DeveloperTaskCount {

    private String  developerEmail;
    private String developerName;
    private Long taskCount;
}
