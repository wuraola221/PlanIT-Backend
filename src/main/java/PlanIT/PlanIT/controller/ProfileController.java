package PlanIT.PlanIT.controller;

import PlanIT.PlanIT.dto.AuthDto;
import PlanIT.PlanIT.dto.ProfileDto;
import PlanIT.PlanIT.dto.ResetPasswordRequest;
import PlanIT.PlanIT.entity.ProfileEntity;
import PlanIT.PlanIT.service.AuthService;
import PlanIT.PlanIT.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;


import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class ProfileController {
    private final ProfileService profileService;
    private final AuthService authService;



    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registerProfile(@Valid @RequestBody ProfileDto profileDto) {
            return profileService.registerProfile(profileDto);

    }

    @GetMapping("/activate")
    public RedirectView activateProfile(@RequestParam String token) {
        boolean activated = profileService.activateProfile(token);
        if (activated) {
            // Redirect to login page with a success flag
            return new RedirectView("http://localhost:3000/auth/login?activated=true");
        } else {
            // Redirect to login page with a failure flag
            return new RedirectView("http://localhost:3000/auth/login?activated=false");
        }
//        return ResponseEntity.ok("Activated");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@RequestBody AuthDto authDto) {
        return authService.login(authDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthDto> logout(HttpServletRequest request) {
//        log.info("Logout request received");
        return authService.logout(request);
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, String>> resendActivationLink(@RequestBody Map<String, String> request) {
        return profileService.resendActivationLink(request.get("email"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
       return profileService.initiateForgotPassword(request.get("email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return profileService.resetPassword(request);
    }







}