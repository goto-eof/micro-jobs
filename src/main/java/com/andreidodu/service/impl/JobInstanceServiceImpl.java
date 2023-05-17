package com.andreidodu.service.impl;

import com.andreidodu.constants.JobInstantConst;
import com.andreidodu.controller.JobInstanceController;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.dto.JobInstanceDTO;
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

    @Override
    public JobInstanceDTO requestWork(Long jobId, String workerUsername, Long customerId) throws ValidationException {
        validateJobInstanceInput(jobId, customerId);
        Optional<JobInstance> foundJobInstance = jobInstanceRepository.findByJob_idAndUserWorker_UsernameAndUserCustomer_id(jobId, workerUsername, customerId);
        if (foundJobInstance.isPresent()) {
            throw new ValidationException("Job already requested");
        }
        Optional<User> worker = userRepository.findByUsername(workerUsername);
        if (worker.isEmpty()) {
            throw new ValidationException("no user found with the worker username passed as parameter");
        }
        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new ValidationException("no job found with the jobId passed as parameter");
        }
        Optional<User> customer = userRepository.findById(customerId);
        if (customer.isEmpty()) {
            throw new ValidationException("no user found with the customerId passed as parameter");
        }

        JobInstance jobInstance = new JobInstance();
        jobInstance.setJob(job.get());
        jobInstance.setUserCustomer(customer.get());
        jobInstance.setUserWorker(worker.get());
        jobInstance.setStatus(JobInstantConst.STATUS_CUSTOMER_WORK_REQUEST);

        JobInstance savedJobInstance = jobInstanceRepository.save(jobInstance);

        return jobInstanceMapper.toDTO(savedJobInstance);
    }

    private static void validateJobInstanceInput(Long jobId, Long customerId) throws ValidationException {
        if (jobId == null) {
            throw new ValidationException("jobId is mandatory");
        }
        if (customerId == null) {
            throw new ValidationException("customerId is mandatory");
        }
    }

    @Override
    public Optional<JobInstanceDTO> getJobInstanceInfo(Long jobId, String workerUsername, Long customerId) throws ValidationException {
        validateJobInstanceInput(jobId, customerId);
        Optional<JobInstance> foundJobInstance = jobInstanceRepository.findByJob_idAndUserWorker_UsernameAndUserCustomer_id(jobId, workerUsername, customerId);
        return Optional.ofNullable(foundJobInstance.map(jobInstance -> jobInstanceMapper.toDTO(jobInstance)).orElse(null));
    }

//    @Override
//    public JobInstanceDTO get(Long id) throws ApplicationException {
//        Optional<JobInstance> modelOpt = this.jobInstanceRepository.findById(id);
//        if (modelOpt.isEmpty()) {
//            throw new ApplicationException("JobInstance not found");
//        }
//        return this.jobInstanceMapper.toDTO(modelOpt.get());
//    }
//
//    @Override
//    public void delete(Long id) {
//        this.jobInstanceRepository.deleteById(id);
//    }
//
//    @Override
//    public JobInstanceDTO save(JobInstanceDTO jobInstanceDTO) throws ApplicationException {
//        if (jobInstanceDTO.getUserCustomerId() == null) {
//            throw new ValidationException("Customer is mandatory");
//        }
//        if (jobInstanceDTO.getUserWorkerId() == null) {
//            throw new ValidationException("Worker is mandatory");
//        }
//        if (jobInstanceDTO.getJobId() == null) {
//            throw new ValidationException("Job is mandatory");
//        }
//        if (jobInstanceDTO.getUserCustomerId().equals(jobInstanceDTO.getUserWorkerId())) {
//            throw new ValidationException("Worker can't match with Customer");
//        }
//        Optional<User> userWorkerOpt = this.userRepository.findById(jobInstanceDTO.getUserWorkerId());
//        if (userWorkerOpt.isEmpty()) {
//            throw new ApplicationException("Worker not found");
//        }
//        Optional<User> userCustomerOpt = this.userRepository.findById(jobInstanceDTO.getUserCustomerId());
//        if (userCustomerOpt.isEmpty()) {
//            throw new ApplicationException("Customer not found");
//        }
//        Optional<Job> jobOpt = this.jobRepository.findById(jobInstanceDTO.getJobId());
//        if (jobOpt.isEmpty()) {
//            throw new ApplicationException("Job not found");
//        }
//        final JobInstance jobInstance = new JobInstance(userWorkerOpt.get(), userCustomerOpt.get(), jobOpt.get(), JobInstantConst.STATUS_CREATED);
//        return this.jobInstanceMapper.toDTO(this.jobInstanceRepository.save(jobInstance));
//    }
//
//    @Override
//    public JobInstanceDTO update(Long id, JobInstanceDTO jobInstanceDTO) throws ApplicationException {
//        if (!id.equals(jobInstanceDTO.getId())) {
//            throw new ValidationException("id not matching");
//        }
//        Optional<JobInstance> userOpt = this.jobInstanceRepository.findById(id);
//        if (userOpt.isEmpty()) {
//            throw new ApplicationException("jobInstance not found");
//        }
//        userOpt.get().setStatus(jobInstanceDTO.getStatus());
//        JobInstance userSaved = this.jobInstanceRepository.save(userOpt.get());
//        return this.jobInstanceMapper.toDTO(userSaved);
//
//    }


}
