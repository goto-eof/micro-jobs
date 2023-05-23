package com.andreidodu.service;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;

import java.util.Optional;

public interface RatingService {
    Optional<RatingDTO> get(Long jobInstanceId, String raterUsername, Long targetUserId) throws ApplicationException;

    RatingDTO save(RatingDTO ratingDTO, String raterUsername) throws ApplicationException;

    void updateUserRating(String usernameTargetUser) throws ApplicationException;
}
