package com.andreidodu.service.impl;

import com.andreidodu.constants.JobInstanceConst;
import com.andreidodu.dto.JobInstanceDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.mapper.JobInstanceMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.JobInstance;
import com.andreidodu.model.User;
import com.andreidodu.repository.JobInstanceRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.JobInstanceService;
import com.andreidodu.util.CommonValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class JobInstanceServiceImpl implements JobInstanceService {

    private final JobInstanceRepository jobInstanceRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobInstanceMapper jobInstanceMapper;

    private static Supplier<ApplicationException> supplyJobInstanceNotFoundException = () -> new ApplicationException("JobInstance not found");
    private static Supplier<ApplicationException> supplyJobNotFoundException = () -> new ValidationException("Job not found");
    private static Supplier<ApplicationException> supplyUserNotFountException = () -> new ApplicationException("User not found");
    private Function<JobInstance, JobInstanceDTO> saveAndReturnJobInstanceDTO;

    @PostConstruct
    private void postConstruct() {
        this.saveAndReturnJobInstanceDTO = (jobInstance) -> jobInstanceMapper.toDTO(jobInstanceRepository.save(jobInstance));
    }

    @Override
    public JobInstanceDTO getJobInstanceInfo(Long jobId, String workerUsername, Long workerId) throws ValidationException {
        validateJobId(jobId);
        validateWorkerId(workerId);

        Optional<JobInstance> jobInstanceOptional = retrieveJobInstanceByJobIdAndWorkerId(jobId, workerId);

        return jobInstanceOptional.map(jobInstance -> jobInstanceMapper.toDTO(jobInstance))
                .orElseGet(() -> this.jobInstanceMapper.toDTO(createJobInstance(jobId, workerId)));
    }

    private Optional<JobInstance> retrieveJobInstanceByJobIdAndWorkerId(Long jobId, Long workerId) {
        return jobInstanceRepository.findByJob_idAndUserWorker_id(jobId, workerId);
    }

    private static void validateWorkerId(Long workerId) throws ValidationException {
        if (CommonValidationUtil.isNull.test(workerId)) {
            throw new ValidationException("workerId is null");
        }
    }

    private static void validateJobId(Long jobId) throws ValidationException {
        if (CommonValidationUtil.isNull.test(jobId)) {
            throw new ValidationException("jobId is null");
        }
    }

    @Override
    public JobInstanceDTO workProviderChangeJobInstanceStatus(Long jobId, String workProviderUsername, Long workerId, Integer jobInstanceStatus) throws ApplicationException, ValidationException {
        validateJobId(jobId);
        validateWorkerId(workerId);
        validateJobInstanceStatus(jobInstanceStatus);

        JobInstance jobInstance = retrieveJobInstanceByJobIdAndWorkerId(jobId, workerId)
                .orElseThrow(supplyJobInstanceNotFoundException);

        jobInstance.setStatus(jobInstanceStatus);

        return saveAndReturnJobInstanceDTO.apply(jobInstance);
    }

    private static void validateJobInstanceStatus(Integer jobInstanceStatus) throws ValidationException {
        if (CommonValidationUtil.isNull.test(jobInstanceStatus)) {
            throw new ValidationException("Invalid JobInstance status");
        }
    }

    @Override
    public JobInstanceDTO workerChangeJobInstanceStatus(Long jobId, String workerUsername, Integer jobInstanceStatus) throws ApplicationException {
        validateJobId(jobId);
        validateJobInstanceStatus(jobInstanceStatus);

        Optional<JobInstance> foundJobInstanceOptional = retrieveJobInstanceByJobIdAndWorkerUsername(jobId, workerUsername);
        validateJobInstanceForChangeStatus(jobInstanceStatus, foundJobInstanceOptional);

        if (foundJobInstanceOptional.isEmpty()) {
            foundJobInstanceOptional = Optional.of(createJobInstance(jobId, workerUsername));
        }

        final JobInstance jobInstance = foundJobInstanceOptional.get();
        jobInstance.setStatus(jobInstanceStatus);
        return jobInstanceMapper.toDTO(this.jobInstanceRepository.save(jobInstance));
    }

    private Optional<JobInstance> retrieveJobInstanceByJobIdAndWorkerUsername(Long jobId, String workerUsername) {
        return jobInstanceRepository.findByJob_idAndUserWorker_Username(jobId, workerUsername);
    }

    private static void validateJobInstanceForChangeStatus(Integer jobInstanceStatus, Optional<JobInstance> foundJobInstanceOptional) throws ApplicationException {
        if (foundJobInstanceOptional.isEmpty() && isJobInstanceStatusInvalid(jobInstanceStatus)) {
            throw new ApplicationException("no jobInstance found");
        }
    }

    private static boolean isJobInstanceStatusInvalid(Integer jobInstanceStatus) {
        return JobInstanceConst.STATUS_CREATED != jobInstanceStatus;
    }


    private JobInstance createJobInstance(Long jobId, String username) throws ApplicationException {
        Job job = retrieveJob(jobId)
                .orElseThrow(supplyJobNotFoundException);

        User worker = retrieveUserByUsername(username)
                .orElseThrow(supplyUserNotFountException);


        JobInstance jobInstance = new JobInstance();
        jobInstance.setStatus(JobInstanceConst.STATUS_CREATED);
        jobInstance.setJob(job);
        jobInstance.setUserWorker(worker);
        jobInstance.setUserCustomer(job.getPublisher());

        return this.jobInstanceRepository.save(jobInstance);
    }

    private Optional<Job> retrieveJob(Long jobId) {
        return this.jobRepository.findById(jobId);
    }

    private Optional<User> retrieveUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }


    private JobInstance createJobInstance(Long jobId, Long workerId) {
        Optional<Job> jobOptional = retrieveJob(jobId);
        Optional<User> workerOptional = retrieveUserById(workerId);

        Job job = jobOptional.get();
        User worker = workerOptional.get();

        JobInstance jobInstance = new JobInstance();
        jobInstance.setStatus(JobInstanceConst.STATUS_CREATED);
        jobInstance.setJob(job);
        jobInstance.setUserWorker(worker);
        jobInstance.setUserCustomer(job.getPublisher());

        return this.jobInstanceRepository.save(jobInstance);
    }

    private Optional<User> retrieveUserById(Long workerId) {
        return this.userRepository.findById(workerId);
    }

}
