package com.andreidodu.service.impl;

import com.andreidodu.dto.JobPictureDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.JobPictureMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.JobPicture;
import com.andreidodu.repository.JobPictureRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.service.JobPictureService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class JobPictureServiceImpl implements JobPictureService {

    private final JobPictureRepository jobPictureRepository;
    private final JobRepository jobRepository;
    private final JobPictureMapper jobPictureMapper;

    @Override
    public JobPictureDTO get(Long id) throws ApplicationException {
        Optional<JobPicture> jobPictureOptional = this.jobPictureRepository.findById(id);
        validateJobPictureExistence(jobPictureOptional);
        return this.jobPictureMapper.toDTO(jobPictureOptional.get());
    }

    private static void validateJobPictureExistence(Optional<JobPicture> jobPictureOptional) throws ApplicationException {
        if (jobPictureOptional.isEmpty()) {
            throw new ApplicationException("JobPicture not found");
        }
    }

    @Override
    public void delete(Long id) {
        this.jobPictureRepository.deleteById(id);
    }

    @Override
    public JobPictureDTO save(JobPictureDTO jobPictureDTO) throws ApplicationException {
        Optional<Job> job = this.jobRepository.findById(jobPictureDTO.getJobId());
        validateJobExistence(job);
        JobPicture model = this.jobPictureMapper.toModel(jobPictureDTO);
        model.setJob(job.get());
        final JobPicture jobPicture = this.jobPictureRepository.save(model);
        return this.jobPictureMapper.toDTO(jobPicture);
    }

    private static void validateJobExistence(Optional<Job> job) throws ApplicationException {
        if (job.isEmpty()) {
            throw new ApplicationException("Job not found");
        }
    }

    @Override
    public JobPictureDTO update(Long id, JobPictureDTO jobPictureDTO) throws ApplicationException {
        validateJobPictureIdMatching(id, jobPictureDTO);

        Optional<JobPicture> userOptional = this.jobPictureRepository.findById(id);
        validateUserExistence(userOptional);

        JobPicture jobPicture = userOptional.get();
        this.jobPictureMapper.getModelMapper().map(jobPictureDTO, jobPicture);

        JobPicture userSaved = this.jobPictureRepository.save(jobPicture);
        return this.jobPictureMapper.toDTO(userSaved);

    }

    private static void validateJobPictureIdMatching(Long id, JobPictureDTO jobPictureDTO) throws ApplicationException {
        if (!isJobPictureIdsSame(id, jobPictureDTO)) {
            throw new ApplicationException("id not matching");
        }
    }

    private static void validateUserExistence(Optional<JobPicture> userOptional) throws ApplicationException {
        if (userOptional.isEmpty()) {
            throw new ApplicationException("job not found");
        }
    }

    private static boolean isJobPictureIdsSame(Long id, JobPictureDTO jobPictureDTO) {
        return id.equals(jobPictureDTO.getId());
    }

}
