package com.andreidodu.service;

import com.andreidodu.dto.JobPictureDTO;
import com.andreidodu.exception.ApplicationException;

public interface JobPictureService {
    JobPictureDTO get(Long id) throws ApplicationException;

    void delete(Long id) throws ApplicationException;

    JobPictureDTO save(JobPictureDTO jobPictureDTO) throws ApplicationException;

    JobPictureDTO update(Long id, JobPictureDTO jobPictureDTO) throws ApplicationException;
}
