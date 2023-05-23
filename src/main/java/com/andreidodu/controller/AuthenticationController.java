package com.andreidodu.controller;

import com.andreidodu.dto.AuthenticationRequestDTO;
import com.andreidodu.dto.AuthenticationResponseDTO;
import com.andreidodu.dto.RegisterRequestDTO;
import com.andreidodu.dto.UserProfileDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.AuthenticationService;
import com.andreidodu.service.JwtService;
import com.andreidodu.service.security.LogoutServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    private final LogoutServiceImpl logoutService;

    final private JwtService jwtService;

    @PostMapping("/register")

    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody RegisterRequestDTO request) throws NoSuchAlgorithmException, IOException {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody AuthenticationRequestDTO request) throws ApplicationException {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> retrieveProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.service.retrieveProfile(this.jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

    @PostMapping("/refreshToken")
    public AuthenticationResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return service.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logoutService.logout(request, response, authentication);
    }

}
