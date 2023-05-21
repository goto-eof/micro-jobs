package com.andreidodu.service;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.exception.ApplicationException;

import java.util.Optional;

public interface RatingService {
    Optional<RatingDTO> get(Long jobInstanceId, String raterUsername, Long targetUserId) throws ApplicationException;

    void delete(Long id);

    RatingDTO save(RatingDTO ratingDTO, String raterUsername) throws ApplicationException;

    RatingDTO update(Long id, RatingDTO ratingDTO) throws ApplicationException;
}
