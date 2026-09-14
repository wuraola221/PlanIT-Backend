package PlanIT.PlanIT.service;


import PlanIT.PlanIT.dto.ProfileDto;
import PlanIT.PlanIT.dto.ResetPasswordRequest;
import PlanIT.PlanIT.entity.ERole;
import PlanIT.PlanIT.entity.ProfileEntity;
import PlanIT.PlanIT.entity.RoleEntity;
import PlanIT.PlanIT.exception.InvalidPasswordException;
import PlanIT.PlanIT.exception.UserAlreadyExistsException;
import PlanIT.PlanIT.repository.ProfileRepository;
import PlanIT.PlanIT.repository.RoleRepository;
import PlanIT.PlanIT.util.PasswordValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;



    private final RoleRepository roleRepository; // Add this repository

    public ResponseEntity<ProfileDto> registerProfile(ProfileDto profileDto) {
        try {

            if (profileRepository.findByEmail(profileDto.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with email" + profileDto.getEmail() +" already exists");
            }

            if (!PasswordValidator.isValid(profileDto.getPassword())) {
                throw new InvalidPasswordException("Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number and one special character (!@#$%^&*()-+).");
            }

            profileDto.setPassword(passwordEncoder.encode(profileDto.getPassword()));

            ProfileEntity profile = toEntity(profileDto);
            profile.setIsActive(false);

            generateActivationToken(profile);
            ProfileEntity savedProfile = profileRepository.save(profile);

            emailService.sendActivationEmail(
                    profile.getEmail(),
                    profile.getFullName(),
                    profile.getActivationToken());

            log.info("Activation email sent successfully to: {}", profile.getEmail());
            return ResponseEntity.ok(toDto(savedProfile));

        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation during registration for email: {}", profileDto.getEmail());
            throw new RuntimeException("User with email " + profileDto.getEmail() + " already exists");
        } catch (MailException e) {
            log.error("Email service failed for registration: {}, Error: {}", profileDto.getEmail(), e.getMessage());
            throw new RuntimeException("Registration completed but activation email could not be sent");
        }
    }

    public boolean activateProfile (String token) {
        try {
            Optional<ProfileEntity> profile = profileRepository.findByActivationToken(token);
            if (profile.isEmpty()) {
                log.warn("Invalid activation token: {}", token);
                return false;
            }
            ProfileEntity profileEntity = profile.get();
            if (profileEntity.getTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Expired activation token for user: {}", profile.get().getEmail());
                return false;
            }

            profileEntity.setIsActive(true);
            profileRepository.save(profileEntity);


            log.info("User activated successfully: {}", profileEntity.getEmail());
            return true;

        } catch (Exception e){
            log.error("Activation failed for token: {}, Error: {}", token, e.getMessage());
            throw new RuntimeException("Activation failed for token: " + token + " due to an unexpected error");


        }
    }


    public ResponseEntity<Map<String, String>> resendActivationLink(String email) {
        try {
            ProfileEntity profile = findProfileByEmail(email);

            if (profile.getIsActive()) {
                log.warn("Attempt to resend activation for already active user: {}", email);
                throw new RuntimeException("User account is already activated");
            }

            generateActivationToken(profile);
            profileRepository.save(profile);
            sendActivationEmail(profile);

            log.info("Activation link resent successfully to: {}", email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Activation link has been resent to your email");
            return ResponseEntity.ok(response);


        } catch (MailException e) {
            log.error("Email service failed for resend activation: {}, Error: {}", email, e.getMessage());
            throw new RuntimeException("Failed to resend activation email");
        } catch (RuntimeException e) {
            log.error("Unexpected error during resend activation for email: {}, Error: {}", email, e.getMessage());
            throw e; // Re-throw RuntimeException as is
        } catch (Exception e) {
            log.error("Unexpected error during resend activation for email: {}, Error: {}", email, e.getMessage());
            throw new RuntimeException("Failed to resend activation link due to an unexpected error");
        }
    }

    public ResponseEntity<Map<String, String>>  initiateForgotPassword(String email) {
        try {
            ProfileEntity profile = findProfileByEmail(email);

            if (!profile.getIsActive()) {
                log.warn("Forgot password attempt for inactive user: {}", email);
                throw new RuntimeException("User account is not activated. Please activate your account first");
            }

            generatePasswordResetToken(profile);
            profileRepository.save(profile);
            sendPasswordResetEmail(profile);

            log.info("Password reset email sent successfully to: {}", email);


            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link has been sent to your email");

            return ResponseEntity.ok(response);

        } catch (MailException e) {
            log.error("Email service failed for forgot password: {}, Error: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        } catch (RuntimeException e) {
            log.error("Unexpected error during forgot password for email: {}, Error: {}", email, e.getMessage());
            throw e; // Re-throw RuntimeException as is
        } catch (Exception e) {
            log.error("Unexpected error during forgot password for email: {}, Error: {}", email, e.getMessage());
            throw new RuntimeException("Failed to initiate password reset due to an unexpected error");
        }
    }

    public ResponseEntity<Map<String, Object>> resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        boolean reset = resetPassword(request.getToken(), request.getNewPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("success", reset);
        response.put("message", reset ? "Password reset successfully" : "Invalid or expired reset token");


        return reset ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }


    public boolean resetPassword(String token, String newPassword) {
        try {
            Optional<ProfileEntity> profileOpt = profileRepository.findByPasswordResetToken(token);
            if (profileOpt.isEmpty()) {
                log.warn("Invalid password reset token: {}", token);
                return false;
            }

            ProfileEntity profile = profileOpt.get();
            if (isTokenExpired(profile.getPasswordResetTokenExpiry())) {
                log.warn("Expired password reset token for user: {}", profile.getEmail());
                return false;
            }

            updatePassword(profile, newPassword);
            clearPasswordResetToken(profile);
            profile.setPassword(passwordEncoder.encode(newPassword));
            profileRepository.save(profile);

            log.info("Password reset successfully for user: {}", profile.getEmail());
            return true;

        } catch (Exception e) {
            log.error("Password reset failed for token: {}, Error: {}", token, e.getMessage());
            throw new RuntimeException("Password reset failed due to an unexpected error");
        }
    }


    public ProfileEntity toEntity(ProfileDto profileDto) {
        ProfileEntity profile = ProfileEntity.builder()
                .id(profileDto.getId())
                .fullName(profileDto.getFullName())
                .email(profileDto.getEmail())
                .password(passwordEncoder.encode(profileDto.getPassword()))
                .build();

        if (profileDto.getRole() != null) {
            RoleEntity roleEntity = roleRepository.findByName(profileDto.getRole())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + profileDto.getRole()));

            profile.setRole(roleEntity);
        }
        return profile;
    }



    public ProfileEntity findProfileByEmail(String email) {
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
    }

    private void generateActivationToken(ProfileEntity profile) {
        profile.setActivationToken(UUID.randomUUID().toString());
        profile.setTokenExpiry(LocalDateTime.now().plusHours(24));
    }


    private void sendActivationEmail(ProfileEntity profile) {
        emailService.sendActivationEmail(
                profile.getEmail(),
                profile.getFullName(),
                profile.getActivationToken()
        );
        log.info("Activation email sent successfully to: {}", profile.getEmail());
    }

    private void generatePasswordResetToken(ProfileEntity profile) {
        profile.setPasswordResetToken(UUID.randomUUID().toString());
        profile.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(2)); // Shorter expiry for security
    }

    private boolean isTokenExpired(LocalDateTime tokenExpiry) {
        return tokenExpiry != null && tokenExpiry.isBefore(LocalDateTime.now());
    }

    private void updatePassword(ProfileEntity profile, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        profile.setPassword(encodedPassword);
    }

    private void clearPasswordResetToken(ProfileEntity profile) {
        profile.setPasswordResetToken(null);
        profile.setPasswordResetTokenExpiry(null);
    }
    private void sendPasswordResetEmail(ProfileEntity profile) {
        emailService.sendPasswordResetEmail(
                profile.getEmail(),
                profile.getFullName(),
                profile.getPasswordResetToken()
        );
//        log.info("Password reset email sent successfully to: {}", profile.getEmail());
    }






    public ProfileDto toDto(ProfileEntity profileEntity) {
        return ProfileDto.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .role(profileEntity.getRole() != null ? profileEntity.getRole().getName() : null)
                .profileImageUrl(profileEntity.getProfileImageUrl() != null ? profileEntity.getProfileImageUrl() : "")
                .build();
    }



}