package com.andreidodu.mapper;

import com.andreidodu.dto.RatingDTO;
import com.andreidodu.model.Rating;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper extends ModelMapperCommon<Rating, RatingDTO> {

    public RatingMapper() {
        super(Rating.class, RatingDTO.class);
    }

    @PostConstruct
    public void postConstruct() {
        super.getModelMapper().typeMap(Rating.class, RatingDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getUserTarget().getId(), RatingDTO::setUserTargetId);
            mapper.map(src -> src.getUserVoter().getId(), RatingDTO::setUserVoterId);
        });
    }

}
