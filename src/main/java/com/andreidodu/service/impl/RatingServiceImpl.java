package com.andreidodu.service.impl;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.RatingMapper;
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

        validateSaveRatingInput(ratingDTO);
        Optional<User> userTargetOpt = this.userRepository.findById(ratingDTO.getUserTargetId());
        validateUserExistence(userTargetOpt);
        Optional<User> userVoterOpt = this.userRepository.findById(ratingDTO.getUserVoterId());
        validateUserExistence(userVoterOpt);

        final Long jobInstanceId = ratingDTO.getJobInstanceId();
        Optional<JobInstance> jobInstanceOptional = jobInstanceRepository.findById(jobInstanceId);
        validateJobInstanceExistence(jobInstanceOptional);

        Rating rating = createRatingModel(ratingDTO, userTargetOpt, userVoterOpt, jobInstanceOptional);
        final Rating ratingSaved = this.ratingRepository.save(rating);
        return this.ratingMapper.toDTO(ratingSaved);
    }

    private Rating createRatingModel(RatingDTO ratingDTO, Optional<User> userTargetOpt, Optional<User> userVoterOpt, Optional<JobInstance> jobInstanceOptional) {
        Rating rating = this.ratingMapper.toModel(ratingDTO);
        rating.setUserTarget(userTargetOpt.get());
        rating.setUserVoter(userVoterOpt.get());
        rating.setJobInstance(jobInstanceOptional.get());
        return rating;
    }

    private static void validateJobInstanceExistence(Optional<JobInstance> jobInstanceOptional) throws ApplicationException {
        if (jobInstanceOptional.isEmpty()) {
            throw new ApplicationException("JobInstance not found");
        }
    }

    private static void validateUserExistence(Optional<User> userTargetOpt) throws ApplicationException {
        if (userTargetOpt.isEmpty()) {
            throw new ApplicationException("User not found");
        }
    }

    private static void validateSaveRatingInput(RatingDTO ratingDTO) throws ApplicationException {
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
    }

    private RatingDTO updateRating(RatingDTO ratingDTO) throws ApplicationException {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingDTO.getId());
        validateRatingExistence(ratingOptional);
        Rating rating = ratingOptional.get();
        rating.setRating(ratingDTO.getRating());
        rating.setComment(ratingDTO.getComment());
        return ratingMapper.toDTO(ratingRepository.save(rating));
    }

    private static void validateRatingExistence(Optional<Rating> ratingOptional) throws ApplicationException {
        if (ratingOptional.isEmpty()) {
            throw new ApplicationException("Rating not found");
        }
    }

}
