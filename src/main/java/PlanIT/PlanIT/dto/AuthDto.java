package PlanIT.PlanIT.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthDto {
    private long id;
    private String email;
    private String password;
    private String token;
    private String message;
    private Long expiresIn;


    public static AuthDto success(String token, Long expiresIn, ProfileDto profileDto  ) {
        AuthDto dto = new AuthDto();
        dto.setId(profileDto.getId());
        dto.setToken(token);
        dto.setExpiresIn(expiresIn);
        dto.setMessage("Login successful");
        return dto;
    }

    public static AuthDto error(String message) {
        AuthDto dto = new AuthDto();
        dto.setMessage(message);
        return dto;
    }

    public static AuthDto logoutSuccess (String message) {
        AuthDto dto = new AuthDto();
        dto.setMessage("Logout successful");
        return dto;
    }
}
