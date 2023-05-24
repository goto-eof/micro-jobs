package com.andreidodu.controller;

import com.andreidodu.dto.JobInstanceDTO;
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

    @GetMapping("/jobId/{jobId}/workerId/{workerId}")
    public ResponseEntity<JobInstanceDTO> getJobInstanceInfo(@PathVariable Long jobId,@PathVariable Long workerId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.getJobInstanceInfo(jobId, jwtService.extractUsernameFromAuthorizzation(authorization), workerId));
    }

    @PostMapping("/jobId/{jobId}/workerId/{workerId}/jobInstanceStatus/{jobInstanceStatus}")
    public ResponseEntity<JobInstanceDTO> workProviderChangeJobInstanceStatus(@PathVariable Long jobId,@PathVariable Long workerId, @PathVariable Integer jobInstanceStatus, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.workProviderChangeJobInstanceStatus(jobId, jwtService.extractUsernameFromAuthorizzation(authorization), workerId, jobInstanceStatus));
    }

    @PostMapping("/jobId/{jobId}/jobInstanceStatus/{jobInstanceStatus}")
    public ResponseEntity<JobInstanceDTO> workerChangeJobInstanceStatus(@PathVariable Long jobId, @PathVariable Integer jobInstanceStatus, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobInstanceService.workerChangeJobInstanceStatus(jobId, jwtService.extractUsernameFromAuthorizzation(authorization), jobInstanceStatus));
    }

}
