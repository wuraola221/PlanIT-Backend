package PlanIT.PlanIT.dto;

import PlanIT.PlanIT.entity.ERole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    private long Id;
    private ERole name;
}
