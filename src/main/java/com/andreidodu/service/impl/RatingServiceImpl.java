package com.andreidodu.service.impl;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.RatingMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.JobInstance;
import com.andreidodu.model.Rating;
import com.andreidodu.model.User;
import com.andreidodu.repository.JobInstanceRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.RatingRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.RatingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final JobInstanceRepository jobInstanceRepository;
    private final RatingMapper ratingMapper;
    private final JobRepository jobRepository;

    @Override
    public Optional<RatingDTO> get(Long jobInstanceId, String raterUsername, Long targetUserId) throws ApplicationException {
        Optional<Rating> ratingOptional = this.ratingRepository.findByJobInstance_idAndUserVoter_usernameAndUserTarget_id(jobInstanceId, raterUsername, targetUserId);
        return Optional.ofNullable(ratingOptional.map(rating -> ratingMapper.toDTO(rating)).orElse(null));
    }

    @Override
    public RatingDTO save(RatingDTO ratingDTO, String raterUsername) throws ApplicationException {
        if (ratingDTO.getId() != null) {
            return updateRating(ratingDTO);
        }

        if (ratingDTO.getUserTargetId() == null) {
            throw new ApplicationException("User target id is null");
        }
        if (ratingDTO.getUserVoterId() == null) {
            throw new ApplicationException("User voter id is null");
        }
        if (ratingDTO.getJobInstanceId() == null) {
            throw new ApplicationException("JobInstance id is null");
        }
        if (ratingDTO.getUserVoterId().equals(ratingDTO.getUserTargetId())) {
            throw new ApplicationException("Voter matches with Target");
        }
        Optional<User> userTargetOpt = this.userRepository.findById(ratingDTO.getUserTargetId());
        if (userTargetOpt.isEmpty()) {
            throw new ApplicationException("Target not found");
        }
        Optional<User> userVoterOpt = this.userRepository.findById(ratingDTO.getUserVoterId());
        if (userVoterOpt.isEmpty()) {
            throw new ApplicationException("Voter not found");
        }

        final Long jobInstanceId = ratingDTO.getJobInstanceId();
        Optional<JobInstance> jobInstanceOptional = jobInstanceRepository.findById(jobInstanceId);
        if (jobInstanceOptional.isEmpty()) {
            throw new ApplicationException("JobInstance not found");
        }
        Job job = jobInstanceOptional.get().getJob();
        Long authorId = job.getPublisher().getId();
        Long workerId = authorId.equals(ratingDTO.getUserTargetId()) ? ratingDTO.getUserVoterId() : ratingDTO.getUserTargetId();

        if (jobInstanceOptional.isEmpty()) {
            throw new ApplicationException("JobInstance not found");
        }

        Rating rating = this.ratingMapper.toModel(ratingDTO);
        rating.setUserTarget(userTargetOpt.get());
        rating.setUserVoter(userVoterOpt.get());
        rating.setJobInstance(jobInstanceOptional.get());
        final Rating ratingSaved = this.ratingRepository.save(rating);
        return this.ratingMapper.toDTO(ratingSaved);
    }

    private RatingDTO updateRating(RatingDTO ratingDTO) throws ApplicationException {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingDTO.getId());
        if (ratingOptional.isEmpty()) {
            throw new ApplicationException("Rating not found");
        }
        Rating rating = ratingOptional.get();
        rating.setRating(ratingDTO.getRating());
        rating.setComment(ratingDTO.getComment());
        return ratingMapper.toDTO(ratingRepository.save(rating));
    }

}
