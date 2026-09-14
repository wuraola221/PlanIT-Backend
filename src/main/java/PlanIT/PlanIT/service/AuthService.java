package PlanIT.PlanIT.service;

import PlanIT.PlanIT.dto.AuthDto;
import PlanIT.PlanIT.dto.ProfileDto;
import PlanIT.PlanIT.entity.ProfileEntity;
import PlanIT.PlanIT.exception.InvalidPasswordException;
import PlanIT.PlanIT.exception.UserAlreadyExistsException;
import PlanIT.PlanIT.repository.ProfileRepository;
import PlanIT.PlanIT.util.JwtUtil;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j

public class AuthService {
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;




    public ResponseEntity<AuthDto> login(AuthDto authDto) {
        try {
            // Validate credentials
            if (authDto.getEmail() == null || authDto.getPassword() == null) {
                log.warn("Login attempt with missing credentials");
                return ResponseEntity.badRequest()
                        .body(AuthDto.error("Email and password are required"));
            }

            // Find user by email
            Optional<ProfileEntity> userOpt = profileRepository.findByEmail(authDto.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("Login attempt with non-existent email: {}", authDto.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDto.error("Invalid email or password"));
            }

            ProfileEntity user = userOpt.get();

            // Check if account is active
            if (!user.getIsActive()) {
                log.warn("Login attempt with inactive account: {}", authDto.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(AuthDto.error("Account is not activated. Please check your email for activation link"));
            }

            // Verify password
            if (!passwordEncoder.matches(authDto.getPassword(), user.getPassword())) {
                log.warn("Failed login attempt for email: {}", authDto.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDto.error("Invalid email or password"));
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().getName().toString(),
                    user.getFullName()
            );
            long expiresIn = jwtUtil.getExpirationTime();

            // Map ProfileEntity to ProfileDto
            ProfileDto profileDto = new ProfileDto();
            profileDto.setId(user.getId());
            profileDto.setEmail(user.getEmail());
            profileDto.setFullName(user.getFullName());
            profileDto.setRole(user.getRole().getName());

            log.info("Successful login for user: {}", user.getEmail());

            // Return success response with token and profile
            return ResponseEntity.ok(AuthDto.success(token, expiresIn, profileDto));

        } catch (Exception e) {
            log.error("Login failed for email: {}, Error: {}", authDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthDto.error("Login failed due to an unexpected error"));
        }
    }

    public ResponseEntity<AuthDto> logout (HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Logout attempt without valid Authorization header");
                return ResponseEntity.badRequest()
                        .body(AuthDto.error("Authorization header is required"));
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // Validate token before blacklisting
            if (!jwtUtil.isTokenValid(token, jwtUtil.extractEmail(String.valueOf(token)))) {
                log.warn("Logout attempt with invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthDto.error("Invalid token"));
            }

            // Extract user email from token for logging
            String userEmail = jwtUtil.extractEmail(token);

            jwtUtil.invalidateToken(token);

            log.info("User {} successfully logged out", userEmail);

            return ResponseEntity.ok(AuthDto.logoutSuccess("Successfully logged out"));

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthDto.error("Logout failed due to an unexpected error"));

        }
    }
}
