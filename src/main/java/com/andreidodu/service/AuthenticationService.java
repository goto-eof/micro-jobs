package com.andreidodu.service;

import com.andreidodu.dto.*;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public interface AuthenticationService {


    AuthenticationResponseDTO register(RegisterRequestDTO request) throws NoSuchAlgorithmException, IOException;

    AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) throws ApplicationException;

    AuthenticationResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    UserProfileDTO retrieveProfile(String username) throws ApplicationException;

}
