package com.andreidodu.repository;

import com.andreidodu.model.JobInstance;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JobInstanceRepository extends CrudRepository<JobInstance, Long> {
    Optional<JobInstance> findByJob_idAndUserWorker_UsernameAndUserCustomer_id(Long jobId, String workerUsername, Long customerId);
}