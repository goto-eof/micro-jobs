package com.andreidodu.service;

import com.andreidodu.dto.JobDTO;
import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.exception.ValidationException;

import java.util.Optional;

public interface JobInstanceService {

    JobInstanceDTO requestWork(Long jobId, String workerUsername, Long customerId) throws ValidationException;

    Optional<JobInstanceDTO> getJobInstanceInfo(Long jobId, String workerUsername, Long customerId) throws ValidationException;
}
