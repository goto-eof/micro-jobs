package com.andreidodu.controller;

import com.andreidodu.constants.ApplicationConst;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.dto.ServerResultDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.service.JobInstanceService;
import com.andreidodu.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/jobInstance/private")
@RequiredArgsConstructor
public class JobInstanceController {
    final private JwtService jwtService;
    final private JobInstanceService jobInstanceService;

    @GetMapping("/jobId/{jobId}/customerId/{customerId}")
    public ResponseEntity<Optional<JobInstanceDTO>> getJobInstanceInfo(@PathVariable Long jobId, @PathVariable Long customerId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.getJobInstanceInfo(jobId, jwtService.extractUsernameFromAuthorizzation(authorization), customerId));
    }

    @PostMapping("/jobId/{jobId}/customerId/{customerId}")
    public ResponseEntity<JobInstanceDTO> requestWork(@PathVariable Long jobId, @PathVariable Long customerId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ValidationException {
        return ResponseEntity.ok(this.jobInstanceService.requestWork(jobId, jwtService.extractUsernameFromAuthorizzation(authorization), customerId));
    }
}
