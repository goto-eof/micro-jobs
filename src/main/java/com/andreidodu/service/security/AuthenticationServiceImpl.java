package com.andreidodu.service.security;


import com.andreidodu.dto.*;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.UserPictureMapper;
import com.andreidodu.model.*;
import com.andreidodu.repository.TokenRepository;
import com.andreidodu.repository.UserPictureRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.AuthenticationService;
import com.andreidodu.service.JwtService;
import com.andreidodu.service.RatingService;
import com.andreidodu.util.ImageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserPictureRepository userPictureRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final UserPictureMapper userPictureMapper;
    private final RatingService ratingService;

    public AuthenticationResponseDTO register(RegisterRequestDTO request) throws NoSuchAlgorithmException, IOException {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .status(0)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
        var savedUser = userRepository.save(user);
        if (request.getPicture() != null) {
            UserPicture userPicture = base64ImageToJobPictureModel(request, savedUser);
            this.userPictureRepository.save(userPicture);
        }
        var jwtToken = jwtServiceImpl.generateToken(user);
        var refreshToken = jwtServiceImpl.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private UserPicture base64ImageToJobPictureModel(RegisterRequestDTO registerRequestDTO, User savedUser) throws NoSuchAlgorithmException, IOException {
        final String base64ImageFull = registerRequestDTO.getPicture();
        final byte[] imageBytesData = ImageUtil.convertBase64StringToBytes(base64ImageFull);
        final String fullFileName = ImageUtil.calculateFileName(savedUser.getId().toString(), base64ImageFull, imageBytesData);
        ImageUtil.writeImageOnFile(fullFileName, imageBytesData);
        return createUserPictureModel(fullFileName, savedUser, registerRequestDTO);
    }


    private UserPicture createUserPictureModel(final String fullFileName, final User savedUser, RegisterRequestDTO request) {
        UserPicture userPicture = new UserPicture();
        userPicture.setPictureName(fullFileName);
        userPicture.setUser(savedUser);
        return userPicture;
    }

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) throws ApplicationException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtServiceImpl.generateToken(user);
        var refreshToken = jwtServiceImpl.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        ratingService.updateUserRating(request.getUsername());

        return AuthenticationResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponseDTO refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new AuthenticationResponseDTO();
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtServiceImpl.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByUsername(userEmail)
                    .orElseThrow();
            if (jwtServiceImpl.isTokenValid(refreshToken, user)) {
                var accessToken = jwtServiceImpl.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponseDTO.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                return authResponse;
            }
        }
        return new AuthenticationResponseDTO();
    }

    @Transactional
    public UserProfileDTO retrieveProfile(String username) throws ApplicationException {
        final Optional<User> userOpt = this.userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ApplicationException("user not found");
        }
        final User user = userOpt.get();

        var builder = UserProfileDTO.builder()
                .id(user.getId())
                .description(user.getDescription())
                .username(user.getUsername())
                .status(user.getStatus())
                .email(user.getEmail())
                .stars(user.getRating())
                .lastname(user.getLastname())
                .firstname(user.getFirstname())
                .role(user.getRole());

        var picture = user.getUserPicture();
        if (picture != null) {
            UserPictureDTO userPictureDTO = new UserPictureDTO();
            userPictureDTO.setId(picture.getId());
            userPictureDTO.setUserId(user.getId());
            userPictureDTO.setPictureName(picture.getPictureName());
            builder.picture(userPictureDTO);
        }

        return builder.build();
    }
}
