package com.andreidodu.repository;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.model.Rating;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends CrudRepository<Rating, Long> {
    Optional<Rating> findByJobInstance_idAndUserVoter_usernameAndUserTarget_id(Long jobInstanceId, String raterUsername, Long targetUserId);

    List<Rating> findByUserTarget_username(String usernameTargetUser);
}