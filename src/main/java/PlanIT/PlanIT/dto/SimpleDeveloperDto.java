package PlanIT.PlanIT.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleDeveloperDto {
    private Long id;
    private String fullName;
    private String email;
}
