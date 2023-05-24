package com.andreidodu.service.impl;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.mapper.RatingMapper;
import com.andreidodu.model.JobInstance;
import com.andreidodu.model.Rating;
import com.andreidodu.model.User;
import com.andreidodu.repository.JobInstanceRepository;
import com.andreidodu.repository.RatingRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.RatingService;
import com.andreidodu.util.CommonValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final JobInstanceRepository jobInstanceRepository;
    private final RatingMapper ratingMapper;

    private static Supplier<ApplicationException> supplyUserTargetNotFoundException = () -> new ApplicationException("userTarget not found");
    private static Supplier<ApplicationException> supplyUserVoterNotFoundException = () -> new ApplicationException("userVoter not found");
    private static Supplier<ApplicationException> supplyJobInstanceNotFoundException = () -> new ApplicationException("JobInstance not found");
    private Function<Rating, RatingDTO> saveRatingAndReturnDTO;

    @PostConstruct
    private void postConstruct() {
        saveRatingAndReturnDTO = rating -> this.ratingMapper.toDTO(this.ratingRepository.save(rating));
    }

    @Override
    public Optional<RatingDTO> get(Long jobInstanceId, String raterUsername, Long targetUserId) throws ApplicationException {
        validateJobInstanceId(jobInstanceId);
        validateTargetUserId(targetUserId);

        Optional<Rating> ratingOptional = retrieveRating(jobInstanceId, raterUsername, targetUserId);

        return Optional.ofNullable(ratingOptional.map(rating -> ratingMapper.toDTO(rating))
                .orElse(null));
    }

    private static void validateTargetUserId(Long targetUserId) throws ValidationException {
        if (CommonValidationUtil.isNull.test(targetUserId)) {
            throw new ValidationException("targetUserId is null");
        }
    }

    private static void validateJobInstanceId(Long jobInstanceId) throws ValidationException {
        if (CommonValidationUtil.isNull.test(jobInstanceId)) {
            throw new ValidationException("jobInstanceId is null");
        }
    }

    private Optional<Rating> retrieveRating(Long jobInstanceId, String raterUsername, Long targetUserId) {
        return this.ratingRepository.findByJobInstance_idAndUserVoter_usernameAndUserTarget_id(jobInstanceId, raterUsername, targetUserId);
    }

    @Override
    public RatingDTO save(RatingDTO ratingDTO, String raterUsername) throws ApplicationException {
        if (!CommonValidationUtil.isNull.test(ratingDTO.getId())) {
            return updateRating(ratingDTO);
        }

        validateSaveRatingInput(ratingDTO);

        User userTarget = retrieveUser(ratingDTO.getUserTargetId())
                .orElseThrow(supplyUserTargetNotFoundException);

        User userVoter = retrieveUser(ratingDTO.getUserVoterId())
                .orElseThrow(supplyUserVoterNotFoundException);

        JobInstance jobInstance = retrieveJobInstance(ratingDTO.getJobInstanceId())
                .orElseThrow(supplyJobInstanceNotFoundException);

        Rating rating = createRatingModel(ratingDTO, userTarget, userVoter, jobInstance);

        return saveRatingAndReturnDTO.apply(rating);
    }

    private Optional<JobInstance> retrieveJobInstance(Long jobInstanceId) {
        return jobInstanceRepository.findById(jobInstanceId);
    }

    private Optional<User> retrieveUser(Long userId) {
        return this.userRepository.findById(userId);
    }

    private Rating createRatingModel(RatingDTO ratingDTO, User userTarget, User userVoter, JobInstance jobInstance) {
        Rating rating = this.ratingMapper.toModel(ratingDTO);
        rating.setUserTarget(userTarget);
        rating.setUserVoter(userVoter);
        rating.setJobInstance(jobInstance);
        return rating;
    }

    private static void validateSaveRatingInput(RatingDTO ratingDTO) throws ApplicationException {
        validateTargetUserId(ratingDTO.getUserTargetId());
        validateJobInstanceId(ratingDTO.getJobInstanceId());
        if (CommonValidationUtil.isNull.test(ratingDTO.getUserVoterId())) {
            throw new ApplicationException("User voter id is null");
        }
        if (CommonValidationUtil.isSameId.test(ratingDTO.getUserVoterId(), ratingDTO.getUserTargetId())) {
            throw new ApplicationException("Voter matches with Target");
        }
    }

    private RatingDTO updateRating(RatingDTO ratingDTO) throws ApplicationException {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingDTO.getId());
        validateRatingExistence(ratingOptional);
        Rating rating = ratingOptional.get();
        rating.setRating(ratingDTO.getRating());
        rating.setComment(ratingDTO.getComment());

        return saveRatingAndReturnDTO.apply(rating);
    }

    private static void validateRatingExistence(Optional<Rating> ratingOptional) throws ApplicationException {
        if (ratingOptional.isEmpty()) {
            throw new ApplicationException("Rating not found");
        }
    }

    @Override
    public void updateUserRating(final String usernameTargetUser) throws ApplicationException {
        User userTarget = retrieveUserByUsername(usernameTargetUser)
                .orElseThrow(supplyUserTargetNotFoundException);

        List<Rating> ratingList = retrieveRatingList(usernameTargetUser);
        if (ratingList.isEmpty()) {
            return;
        }
        double rating = calculateNewRating(ratingList);

        userTarget.setRating(rating);

        userRepository.save(userTarget);
    }

    private static double calculateNewRating(List<Rating> ratingList) {
        Integer sumOfAllVotes = ratingList.stream().map(rating -> rating.getRating()).reduce(0, Integer::sum);
        return ((double) sumOfAllVotes) / ratingList.size();
    }

    private List<Rating> retrieveRatingList(String usernameTargetUser) {
        return this.ratingRepository.findByUserTarget_username(usernameTargetUser);
    }

    private Optional<User> retrieveUserByUsername(String usernameTargetUser) {
        return userRepository.findByUsername(usernameTargetUser);
    }


}
