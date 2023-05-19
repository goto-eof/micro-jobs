package com.andreidodu.service.impl;

import com.andreidodu.constants.JobInstanceConst;
import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.mapper.JobInstanceMapper;
import com.andreidodu.mapper.JobMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.JobInstance;
import com.andreidodu.model.User;
import com.andreidodu.repository.JobInstanceRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.JobInstanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class JobInstanceServiceImpl implements JobInstanceService {

    private final JobInstanceRepository jobInstanceRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobInstanceMapper jobInstanceMapper;
    private final JobMapper jobMapper;


    private static void validateJobInstanceInput(Long jobId, Integer jobInstanceStatus) throws ValidationException {
        if (jobInstanceStatus == null) {
            throw new ValidationException("jobInstanceStatus is mandatory");
        }
        if (jobId == null) {
            throw new ValidationException("jobId is mandatory");
        }
    }


    @Override
    public JobInstanceDTO getJobInstanceInfo(Long jobId, String workerUsername, Long workerId) throws ValidationException {
        Optional<JobInstance> foundJobInstance = jobInstanceRepository.findByJob_idAndUserWorker_id(jobId, workerId);
        return foundJobInstance.map(jobInstance -> jobInstanceMapper.toDTO(jobInstance))
                .orElseGet(() -> this.jobInstanceMapper.toDTO(createJobInstance(jobId, workerId)));
    }

    @Override
    public JobInstanceDTO workProviderChangeJobInstanceStatus(Long jobId, String workProviderUsername, Long workerId, Integer jobInstanceStatus) throws ValidationException {
        Optional<JobInstance> jobInstanceOptional = jobInstanceRepository.findByJob_idAndUserWorker_id(jobId, workerId);
        if (jobInstanceOptional.isEmpty()) {
            throw new ValidationException("jobInstanceNotFound");
        }
        final JobInstance jobInstance = jobInstanceOptional.get();
        jobInstance.setStatus(jobInstanceStatus);
        final JobInstance updatedJobInstance = jobInstanceRepository.save(jobInstance);
        return jobInstanceMapper.toDTO(updatedJobInstance);
    }

    @Override
    public JobInstanceDTO workerChangeJobInstanceStatus(Long jobId, String workerUsername, Integer jobInstanceStatus) throws ApplicationException {
        Optional<JobInstance> foundJobInstanceOptional = jobInstanceRepository.findByJob_idAndUserWorker_Username(jobId, workerUsername);
        if (foundJobInstanceOptional.isEmpty() && JobInstanceConst.STATUS_CREATED != jobInstanceStatus) {
            throw new ApplicationException("no jobInstance found");
        }
        if (foundJobInstanceOptional.isEmpty()) {
            foundJobInstanceOptional = Optional.of(createJobInstance(jobId, workerUsername));
        }
        final JobInstance jobInstance = foundJobInstanceOptional.get();
        jobInstance.setStatus(jobInstanceStatus);
        return jobInstanceMapper.toDTO(this.jobInstanceRepository.save(jobInstance));
    }


    private JobInstance createJobInstance(Long jobId, String username) {
        Optional<Job> jobOptional = this.jobRepository.findById(jobId);
        Optional<User> userOptional = this.userRepository.findByUsername(username);

        Job job = jobOptional.get();
        User worker = userOptional.get();

        JobInstance jobInstance = new JobInstance();
        jobInstance.setStatus(JobInstanceConst.STATUS_CREATED);
        jobInstance.setJob(job);
        jobInstance.setUserWorker(worker);
        jobInstance.setUserCustomer(job.getPublisher());

        return this.jobInstanceRepository.save(jobInstance);
    }


    private JobInstance createJobInstance(Long jobId, Long workerId) {
        Optional<Job> jobOptional = this.jobRepository.findById(jobId);
        Optional<User> workerOptional = this.userRepository.findById(workerId);

        Job job = jobOptional.get();
        User worker = workerOptional.get();

        JobInstance jobInstance = new JobInstance();
        jobInstance.setStatus(JobInstanceConst.STATUS_CREATED);
        jobInstance.setJob(job);
        jobInstance.setUserWorker(worker);
        jobInstance.setUserCustomer(job.getPublisher());

        return this.jobInstanceRepository.save(jobInstance);
    }

}
