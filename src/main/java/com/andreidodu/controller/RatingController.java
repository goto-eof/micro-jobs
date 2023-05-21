package com.andreidodu.controller;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.JwtService;
import com.andreidodu.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/rating")
@RequiredArgsConstructor
public class RatingController {

    final private RatingService ratingService;
    final private JwtService jwtService;

    @GetMapping("/jobInstanceId/{jobInstanceId}/targetUserId/{targetUserId}")
    public ResponseEntity<Optional<RatingDTO>> get(@PathVariable Long jobInstanceId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable Long targetUserId) throws ApplicationException {
        return ResponseEntity.ok(this.ratingService.get(jobInstanceId, jwtService.extractUsernameFromAuthorizzation(authorization), targetUserId));
    }

    @PostMapping
    public ResponseEntity<RatingDTO> save(@RequestBody RatingDTO ratingDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.ratingService.save(ratingDTO, jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

}
