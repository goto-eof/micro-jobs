package com.andreidodu.controller;

import com.andreidodu.constants.JobConst;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.dto.JobListPageDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.service.JobInstanceService;
import com.andreidodu.service.JobService;
import com.andreidodu.service.JwtService;
import com.andreidodu.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(value = "/api/v1/job/private")
@RequiredArgsConstructor
public class JobPrivateController {

    final private JobService jobService;

    final private JwtService jwtService;

    final private RoomService roomService;
    final private JobInstanceService jobInstanceService;

    @GetMapping("/jobId/{jobId}")
    public ResponseEntity<JobDTO> getPrivate(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.getPrivate(jobId, jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

    @PostMapping("/approve/jobId/{jobId}")
    public ResponseEntity<JobDTO> approveJob(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.changeJobStatus(jobId, JobConst.STATUS_PUBLISHED, jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator)));
    }

    @PostMapping("/unpublish/jobId/{jobId}")
    public ResponseEntity<JobDTO> unpublishJob(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.changeJobStatus(jobId, JobConst.STATUS_UNPUBLISHED, jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator)));
    }

    @GetMapping("/jobType/{jobType}/page/{page}")
    public ResponseEntity<JobListPageDTO> getAllPrivatePaginated(@PathVariable Integer jobType, @PathVariable Integer page, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        final String username = this.jwtService.extractUsernameFromAuthorizzation(authorization);
        JobListPageDTO result = new JobListPageDTO(this.jobService.getAllPrivate(username, jobType, page), this.jobService.countAllPrivateByTypeAndUsername(username, jobType));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/jobType/{jobType}/page/{page}")
    public ResponseEntity<JobListPageDTO> getAllPrivateAdminPaginated(@PathVariable Integer jobType, @PathVariable Integer page, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        final String username = this.jwtService.extractUsernameFromAuthorizzation(authorization);
        JobListPageDTO result = new JobListPageDTO(this.jobService.getAllPrivateByTypeAndStatus(jobType, Arrays.asList(JobConst.STATUS_CREATED, JobConst.STATUS_UPDATED, JobConst.STATUS_UNPUBLISHED), username, page), this.jobService.countAllPrivateByTypeAndStatus(jobType, Arrays.asList(JobConst.STATUS_CREATED, JobConst.STATUS_UPDATED), username));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/jobId/{jobId}")
    public ResponseEntity<JobDTO> getPrivateByAdmin(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.getPrivateByAdmin(jobId, jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

    @PostMapping
    public ResponseEntity<JobDTO> save(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody JobDTO jobDTO) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.save(jobDTO, this.jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

    @PutMapping("/jobId/{jobId}")
    public ResponseEntity<JobDTO> update(@PathVariable Long jobId, @RequestBody JobDTO jobDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        return ResponseEntity.ok(this.jobService.update(jobId, jobDTO, this.jwtService.extractUsernameFromAuthorizzation(authorization)));
    }

    @DeleteMapping("/jobId/{jobId}")
    public ResponseEntity<String> delete(@PathVariable Long jobId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ApplicationException {
        this.jobService.delete(jobId, this.jwtService.extractUsernameFromAuthorizzation(authorization));
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/roomId/{roomId}")
    public ResponseEntity<JobDTO> getJobByRoomId(@PathVariable Long roomId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationAdministrator) throws ApplicationException {
        return ResponseEntity.ok(this.roomService.getJobByRoomId(jwtService.extractUsernameFromAuthorizzation(authorizationAdministrator), roomId));
    }

}
