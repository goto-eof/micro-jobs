package com.andreidodu.service;

import com.andreidodu.dto.JobDTO;
import com.andreidodu.exception.ApplicationException;

import java.util.List;

public interface JobService {
    JobDTO getPrivate(final Long id, final String username) throws ApplicationException;

    JobDTO getPrivateByAdmin(Long jobId, String username) throws ApplicationException;

    List<JobDTO> getAllPublic(int type, int page) throws ApplicationException;

    List<JobDTO> getAllPrivate(String username, int type, int page) throws ApplicationException;

    List<JobDTO> getAllPrivateByTypeAndStatus(int type, List<Integer> statuses, String username, int page) throws ApplicationException;

    // TODO include in the getAll result
    long countAllPrivateByTypeAndStatus(int type, List<Integer> statuses, String username) throws ApplicationException;

    void delete(Long id, String username) throws ApplicationException;

    JobDTO save(JobDTO jobDTO, String username) throws ApplicationException;

    JobDTO changeJobStatus(Long jobId, int jobStatus, String usernameAdministrator) throws ApplicationException;

    JobDTO update(Long id, JobDTO jobDTO, String owner) throws ApplicationException;

    long countAllPublicByType(int type);

    long countAllPrivateByTypeAndUsername(String username, int type);

    JobDTO getPublic(Long id) throws ApplicationException;

}
