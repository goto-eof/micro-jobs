package com.andreidodu.service.impl;

import com.andreidodu.constants.ApplicationConst;
import com.andreidodu.constants.JobConst;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.dto.JobPictureDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.mapper.JobMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.JobPicture;
import com.andreidodu.model.Role;
import com.andreidodu.model.User;
import com.andreidodu.repository.JobPageableRepository;
import com.andreidodu.repository.JobPictureRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.JobService;
import com.andreidodu.util.ImageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class JobServiceImpl implements JobService {
    private static Integer MAX_NUMBER_ATTACHED_PICTURES = 5;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobPageableRepository jobPageableRepository;
    private final JobPictureRepository jobPictureRepository;
    private final JobMapper jobMapper;

    @Override
    public JobDTO getPrivate(final Long id, final String username) throws ApplicationException {
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new ApplicationException("Job not found"));
        User publisher = job.getPublisher();
        if (!isSameUsername(username, publisher)) {
            throw new ApplicationException("User not match");
        }
        return this.jobMapper.toDTO(job);
    }

    private static boolean isSameUsername(String username, User publisher) {
        return publisher.getUsername().equals(username);
    }

    @Override
    public JobDTO getPublic(Long id) throws ApplicationException {
        Job job = this.jobRepository.findByIdAndStatus(id, JobConst.STATUS_PUBLISHED).orElseThrow(() -> new ApplicationException("Job not found"));
        return this.jobMapper.toDTO(job);
    }

    @Override
    public JobDTO getPrivateByStatus(Long id, Integer jobStatus, String username) throws ApplicationException {
        User administrator = this.userRepository.findByUsername(username).orElseThrow(() -> new ApplicationException("User not found"));
        if (!isAdminSameRole(administrator)) {
            throw new ValidationException("User is not admin");
        }
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new ApplicationException("Job not found"));
        return this.jobMapper.toDTO(job);
    }

    private static boolean isAdminSameRole(User administrator) {
        return Role.ADMIN.equals(administrator.getRole());
    }

    @Override
    public List<JobDTO> getAllPublic(int type, int page) throws ApplicationException {
        validateJobType(type);
        Pageable secondPageWithFiveElements = PageRequest.of(page, 10);
        List<Job> models = this.jobPageableRepository.findByTypeAndStatusIn(type, Arrays.asList(JobConst.STATUS_PUBLISHED), secondPageWithFiveElements);
        return this.jobMapper.toListDTO(models);
    }

    @Override
    public long countAllPublicByType(int type) {
        return this.jobRepository.countByTypeAndStatusIn(type, Arrays.asList(JobConst.STATUS_PUBLISHED));
    }

    @Override
    public List<JobDTO> getAllPrivate(String username, int type, int page) throws ApplicationException {
        validateJobType(type);
        Pageable secondPageWithFiveElements = PageRequest.of(page, 10);
        List<Job> models = this.jobPageableRepository.findByTypeAndPublisher_username(type, username, secondPageWithFiveElements);
        return this.jobMapper.toListDTO(models);
    }

    @Override
    public long countAllPrivateByTypeAndUsername(String username, int type) {
        return this.jobRepository.countByTypeAndPublisher_username(type, username);
    }

    @Override
    public List<JobDTO> getAllPrivateByTypeAndStatus(int type, List<Integer> statuses, String username, int page) throws ApplicationException {
        validateJobType(type);
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new ApplicationException("User not found"));
        if (!isAdminSameRole(user)) {
            throw new ApplicationException("User is not admin");
        }
        Pageable pageable = PageRequest.of(page, 10);
        List<Job> models = this.jobPageableRepository.findByTypeAndStatusIn(type, statuses, pageable);
        return this.jobMapper.toListDTO(models);
    }

    private static void validateJobType(Integer jobType ) throws ValidationException {
        if (!Arrays.asList(JobConst.TYPE_REQUEST, JobConst.TYPE_OFFER).contains(jobType)){
            throw new ValidationException("Invalid job type");
        }
    }

    @Override
    public long countAllPrivateByTypeAndStatus(int type, List<Integer> statuses, String username) throws ApplicationException {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new ApplicationException("User not found"));
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new ApplicationException("User is not admin");
        }
        return this.jobRepository.countByTypeAndStatusIn(type, statuses);
    }

    @Override
    public void delete(Long jobId, String username) throws ApplicationException {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new ApplicationException("User not found"));
        Job job = this.jobRepository.findById(jobId).orElseThrow(() -> new ApplicationException("Job does not exists"));
        // TODO add also the administrator here
        if (!isSameUsername(job.getPublisher().getUsername(), user)) {
            throw new ValidationException("You are nto allowed to do this operation");
        }
        deleteFilesFromDisk(job.getJobPictureList());
        this.jobRepository.deleteByIdAndPublisher_Username(jobId, username);
    }

    private void deleteFilesFromDisk(List<JobPicture> jobPictureList) {
        jobPictureList.forEach(jobPicture -> {
            File file = new File(ApplicationConst.FILES_DIRECTORY + "/" + jobPicture.getPictureName());
            file.delete();
        });
    }

    @Override
    public JobDTO save(JobDTO jobDTO, String username) throws ApplicationException {
        validateMaNumberOfAttachments(jobDTO);
        Optional<User> userOpt = this.userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ApplicationException("User not found");
        }
        jobDTO.setStatus(JobConst.STATUS_CREATED);
        Job model = this.jobMapper.toModel(jobDTO);
        model.setPublisher(userOpt.get());
        Job job = this.jobRepository.save(model);
        Iterable<JobPicture> savedJobPictureList = saveJobPictureModelList(jobDTO.getJobPictureList(), job);
        final Optional<String> mainPictureName = extractMainJobPicture(savedJobPictureList);
        if (mainPictureName.isPresent()) {
            job.setPicture(mainPictureName.get());
            job = this.jobRepository.save(job);
        }
        return this.jobMapper.toDTO(job);
    }

    private Optional<String> extractMainJobPicture(Iterable<JobPicture> savedJobPictureList) {
        Iterator<JobPicture> jobPictureIterator = savedJobPictureList.iterator();
        if (jobPictureIterator.hasNext()) {
            return Optional.of(jobPictureIterator.next().getPictureName());
        }
        return Optional.empty();
    }

    private Iterable<JobPicture> saveJobPictureModelList(final List<JobPictureDTO> jobPictureDTOList, Job job) {
        if (jobPictureDTOList != null && jobPictureDTOList.size() > 0) {
            final List<JobPicture> listOfModels = jobPictureDTOList.stream()
                    .map(jobPictureDTO -> jobPictureDTO.getContent())
                    .map(base64ImageFull -> {
                        try {
                            return base64ImageToJobPictureModel(job, base64ImageFull);
                        } catch (IOException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
            if (listOfModels.size() > 0) {
                return this.jobPictureRepository.saveAll(listOfModels);
            }
        }
        return new ArrayList<>();
    }

    private JobPicture base64ImageToJobPictureModel(Job job, String base64ImageFull) throws NoSuchAlgorithmException, IOException {
        final byte[] imageBytesData = ImageUtil.convertBase64StringToBytes(base64ImageFull);
        final String fullFileName = ImageUtil.calculateFileName(job.getId().toString(), base64ImageFull, imageBytesData);
        ImageUtil.writeImageOnFile(fullFileName, imageBytesData);
        return createJobPictureModel(fullFileName, job);
    }


    private JobPicture createJobPictureModel(final String fullFileName, final Job job) {
        JobPicture modelJobPicture = new JobPicture();
        modelJobPicture.setPictureName(fullFileName);
        modelJobPicture.setJob(job);
        return modelJobPicture;
    }

    @Override
    public JobDTO changeJobStatus(Long jobId, int jobStatus, String usernameAdministrator) throws ApplicationException {
        User administrator = this.userRepository.findByUsername(usernameAdministrator).orElseThrow(() -> new ApplicationException("User not found"));
        validateRoleIsAdmin(administrator);

        Job job = this.jobRepository.findById(jobId).orElseThrow(() -> new ApplicationException("Job not found"));
        job.setStatus(jobStatus);
        Job jobSaved = this.jobRepository.save(job);

        return this.jobMapper.toDTO(jobSaved);
    }

    private static void validateRoleIsAdmin(User administrator) throws ApplicationException {
        if (administrator.getRole() != Role.ADMIN) {
            throw new ApplicationException("User is not admin");
        }
    }

    @Override
    public JobDTO update(Long id, JobDTO jobDTO, String owner) throws ApplicationException {
        validateJobIdMatch(id, jobDTO);
        validateMaNumberOfAttachments(jobDTO);
        Job job = this.jobRepository.findById(id).orElseThrow(() -> new ApplicationException("job not found"));
        if (!isSameUsername(owner, job.getPublisher())) {
            throw new ApplicationException("wrong user");
        }
        this.jobMapper.getModelMapper().map(jobDTO, job);
        job.setStatus(JobConst.STATUS_UPDATED);
        Job jobSaved = this.jobRepository.save(job);
        deleteJobPicturesNotInDTOList(jobDTO, jobSaved);
        Iterable<JobPicture> savedJobPictureList = saveJobPicturesInDTOList(jobDTO, jobSaved);
        final Optional<String> mainPictureName = extractMainJobPicture(savedJobPictureList);
        if (mainPictureName.isPresent()) {
            job.setPicture(mainPictureName.get());
            job = this.jobRepository.save(job);
        }
        return this.jobMapper.toDTO(jobSaved);
    }

    private static void validateMaNumberOfAttachments(JobDTO jobDTO) throws ValidationException {
        if (jobDTO.getJobPictureList().size() > MAX_NUMBER_ATTACHED_PICTURES) {
            throw new ValidationException("Maximum number of pictures allowed is " + MAX_NUMBER_ATTACHED_PICTURES);
        }
    }

    private static void validateJobIdMatch(Long id, JobDTO jobDTO) throws ValidationException {
        if (!id.equals(jobDTO.getId())) {
            throw new ValidationException("id not matching");
        }
    }

    private Iterable<JobPicture> saveJobPicturesInDTOList(JobDTO jobDTO, Job jobSaved) {
        List<JobPictureDTO> jobPictureDTOListToBeSaved = retrieveJobPictureDTOToBeSaved(jobDTO);
        return saveJobPictureModelList(jobPictureDTOListToBeSaved, jobSaved);
    }

    private void deleteJobPicturesNotInDTOList(JobDTO jobDTO, Job jobSaved) {
        List<JobPicture> jobPictureList = jobSaved.getJobPictureList();
        List<String> jobPictureListOfNames = retrieveJobPictureListOfNamesFromDTO(jobDTO);
        List<JobPicture> jobPictureListToBeDeleted = calculateJobPictureListToBeDeleted(jobPictureList, jobPictureListOfNames);
        deletePicturesFromPictureList(jobPictureListToBeDeleted);
    }

    private static List<JobPictureDTO> retrieveJobPictureDTOToBeSaved(JobDTO jobDTO) {
        return jobDTO.getJobPictureList()
                .stream()
                .filter(jobPictureDTO -> jobPictureDTO.getContent() != null)
                .collect(Collectors.toList());
    }

    private void deletePicturesFromPictureList(List<JobPicture> jobPictureListToBeDeleted) {
        if (jobPictureListToBeDeleted.size() > 0) {
            deleteFilesFromDisk(jobPictureListToBeDeleted);
            this.jobPictureRepository.deleteAll(jobPictureListToBeDeleted);
        }
    }

    private static List<JobPicture> calculateJobPictureListToBeDeleted(List<JobPicture> jobPictureList, List<String> jobPictureListOfNames) {
        return jobPictureList.stream()
                .filter(jobPicture -> !jobPictureListOfNames.contains(jobPicture.getPictureName()))
                .collect(Collectors.toList());
    }

    private static List<String> retrieveJobPictureListOfNamesFromDTO(JobDTO jobDTO) {
        return jobDTO.getJobPictureList()
                .stream()
                .filter(jobPictureDTO -> jobPictureDTO.getContent() == null)
                .map(jobPicture -> jobPicture.getPictureName())
                .collect(Collectors.toList());
    }

}
