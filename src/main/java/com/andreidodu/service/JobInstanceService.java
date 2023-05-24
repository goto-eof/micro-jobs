package com.andreidodu.service;

import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;

import java.util.Optional;

public interface JobInstanceService {

    JobInstanceDTO getJobInstanceInfo(Long jobId, String workerUsername, Long workerId) throws ValidationException;

    JobInstanceDTO workProviderChangeJobInstanceStatus(Long jobId, String workerUsername, Long workerId, Integer jobInstanceStatus) throws ValidationException, ApplicationException;

    JobInstanceDTO workerChangeJobInstanceStatus(Long jobId, String workerUsername, Integer jobInstanceStatus) throws ApplicationException;

}
