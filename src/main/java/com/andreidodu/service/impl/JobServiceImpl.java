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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class JobServiceImpl implements JobService {
    private final static int NUMBER_OF_ITEMS_PER_PAGE = 10;
    public static final int MAX_IMAGE_SIZE = 1000;

    private static Integer MAX_NUMBER_ATTACHED_PICTURES = 5;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobPageableRepository jobPageableRepository;
    private final JobPictureRepository jobPictureRepository;
    private final JobMapper jobMapper;

    private static Supplier<ApplicationException> supplyJobNotFoundException = () -> new ApplicationException("Job not found");
    private static Supplier<ApplicationException> supplyUserNotFoundException = () -> new ApplicationException("User not found");

    @Override
    public JobDTO getPrivate(final Long jobId, final String username) throws ApplicationException {
        validateJobId(jobId);
        validateUsername(username);

        Job job = this.jobRepository.findById(jobId)
                .orElseThrow(supplyJobNotFoundException);

        User publisher = job.getPublisher();

        validateSameUsername(username, publisher.getUsername());

        return this.jobMapper.toDTO(job);
    }

    private void validateJobId(Long jobId) throws ValidationException {
        if (jobId == null) {
            throw new ValidationException("jobId is null");
        }
    }

    private void validateUsername(String username) throws ValidationException {
        if (username == null) {
            throw new ValidationException("username is null");
        }
    }

    public Optional<Job> retrieveJob(Long id) {
        return this.jobRepository.findById(id);
    }

    private static void validateSameUsername(String username, String publisherUsername) throws ApplicationException {
        if (!isSameUsername(username, publisherUsername)) {
            throw new ApplicationException("User not match");
        }
    }

    private static boolean isSameUsername(String username, String publisherUsername) {
        return publisherUsername.equals(username);
    }

    @Override
    public JobDTO getPublic(Long jobId) throws ApplicationException {
        validateJobId(jobId);

        Job job = this.jobRepository.findByIdAndStatus(jobId, JobConst.STATUS_PUBLISHED)
                .orElseThrow(supplyJobNotFoundException);

        return this.jobMapper.toDTO(job);
    }

    @Override
    public JobDTO getPrivateByAdmin(Long jobId, String username) throws ApplicationException {
        validateJobId(jobId);
        validateUsername(username);

        User administrator = this.userRepository.findByUsername(username)
                .orElseThrow(supplyUserNotFoundException);

        validateMakeSureIsAdmin(administrator);

        Job job = retrieveJob(jobId)
                .orElseThrow(supplyJobNotFoundException);

        return this.jobMapper.toDTO(job);
    }

    private static void validateMakeSureIsAdmin(User administrator) throws ValidationException {
        if (!isAdminSameRole(administrator)) {
            throw new ValidationException("User is not admin");
        }
    }

    private static boolean isAdminSameRole(User administrator) {
        return Role.ADMIN.equals(administrator.getRole());
    }

    @Override
    public List<JobDTO> getAllPublic(int type, int page) throws ApplicationException {
        validateJobType(type);

        Pageable pageRequest = PageRequest.of(page, NUMBER_OF_ITEMS_PER_PAGE);
        List<Integer> allowedStatuses = Arrays.asList(JobConst.STATUS_PUBLISHED);
        List<Job> models = retrieveJobs(type, pageRequest, allowedStatuses);

        return this.jobMapper.toListDTO(models);
    }

    private List<Job> retrieveJobs(int type, Pageable pageRequest, List<Integer> allowedStatuses) {
        return this.jobPageableRepository.findByTypeAndStatusIn(type, allowedStatuses, pageRequest);
    }

    @Override
    public long countAllPublicByType(int type) {
        return this.jobRepository.countByTypeAndStatusIn(type, Arrays.asList(JobConst.STATUS_PUBLISHED));
    }

    @Override
    public List<JobDTO> getAllPrivate(String username, int type, int page) throws ApplicationException {
        validateJobType(type);
        validateUsername(username);

        Pageable pageRequest = PageRequest.of(page, NUMBER_OF_ITEMS_PER_PAGE);
        List<Job> models = retrieveJobsByUsername(username, type, pageRequest);

        return this.jobMapper.toListDTO(models);
    }

    private List<Job> retrieveJobsByUsername(String username, int type, Pageable pageRequest) {
        return this.jobPageableRepository.findByTypeAndPublisher_username(type, username, pageRequest);
    }

    @Override
    public long countAllPrivateByTypeAndUsername(String username, int type) {
        return this.jobRepository.countByTypeAndPublisher_username(type, username);
    }

    @Override
    public List<JobDTO> getAllPrivateByTypeAndStatus(int jobType, List<Integer> statuses, String username, int page) throws ApplicationException {
        validateUsername(username);
        validateJobType(jobType);

        User user = this.userRepository.findByUsername(username)
                .orElseThrow(supplyUserNotFoundException);

        validateMakeSureIsAdmin(user);

        Pageable pageRequest = PageRequest.of(page, NUMBER_OF_ITEMS_PER_PAGE);
        List<Job> models = retrieveJobs(jobType, pageRequest, statuses);

        return this.jobMapper.toListDTO(models);
    }

    private static void validateJobType(Integer jobType) throws ValidationException {
        if (!Arrays.asList(JobConst.TYPE_REQUEST, JobConst.TYPE_OFFER).contains(jobType)) {
            throw new ValidationException("Invalid job type");
        }
    }

    @Override
    public long countAllPrivateByTypeAndStatus(int type, List<Integer> statuses, String username) throws ApplicationException {
        validateUsername(username);

        User user = this.userRepository.findByUsername(username)
                .orElseThrow(supplyUserNotFoundException);

        validateMakeSureIsAdminByRole(user.getRole());

        return this.jobRepository.countByTypeAndStatusIn(type, statuses);
    }

    private static void validateMakeSureIsAdminByRole(Role role) throws ApplicationException {
        if (!Role.ADMIN.equals(role)) {
            throw new ApplicationException("User is not admin");
        }
    }

    @Override
    public void delete(Long jobId, String username) throws ApplicationException {
        validateJobId(jobId);
        validateUsername(username);

        User user = this.userRepository.findByUsername(username)
                .orElseThrow(supplyUserNotFoundException);

        Job job = retrieveJob(jobId)
                .orElseThrow(supplyJobNotFoundException);

        User publisher = job.getPublisher();
        validateMakeSureIsJobAuthor(user, publisher);

        deleteFilesFromDisk(job.getJobPictureList());

        deleteJob(jobId, username);
    }

    private static void validateMakeSureIsJobAuthor(User user, User publisher) throws ValidationException {
        if (!isSameUsername(publisher.getUsername(), user.getUsername())) {
            throw new ValidationException("You are nto allowed to do this operation");
        }
    }

    private void deleteJob(Long jobId, String username) {
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
        validateUsername(username);
        validateMaNumberOfAttachments(jobDTO);

        User author = this.userRepository.findByUsername(username)
                .orElseThrow(supplyUserNotFoundException);

        jobDTO.setStatus(JobConst.STATUS_CREATED);

        Job model = this.jobMapper.toModel(jobDTO);
        model.setPublisher(author);
        Job job = this.jobRepository.save(model);

        Iterable<JobPicture> savedJobPictureList = saveJobPictureModelList(jobDTO.getJobPictureList(), job);

        job = saveJobMainPicture(job, savedJobPictureList);

        return this.jobMapper.toDTO(job);
    }

    private Job saveJobMainPicture(Job job, Iterable<JobPicture> savedJobPictureList) {
        final Optional<String> mainPictureName = extractMainJobPicture(savedJobPictureList);
        if (mainPictureName.isPresent()) {
            job.setPicture(mainPictureName.get());
            job = this.jobRepository.save(job);
        }
        return job;
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

    private JobPicture base64ImageToJobPictureModel(Job job, String base64ImageFull) throws IOException, NoSuchAlgorithmException {
        byte[] imageBytesData = ImageUtil.convertBase64StringToBytes(base64ImageFull);
        final String fullFileName = ImageUtil.calculateFileName(job.getId().toString(), base64ImageFull, imageBytesData);
        ImmutablePair<Integer, Integer> imageSize = ImageUtil.retrieveImageSize(imageBytesData);
        if (isInvalidImageSize(imageSize)) {
            imageBytesData = getResizedImageBytes(imageBytesData);
        }
        ImageUtil.writeImageOnFile(fullFileName, imageBytesData);
        return createJobPictureModel(fullFileName, job);
    }

    private static byte[] getResizedImageBytes(byte[] imageBytesData) {
        return ImageUtil.resizeImage(imageBytesData, MAX_IMAGE_SIZE);
    }

    private static boolean isInvalidImageSize(ImmutablePair<Integer, Integer> imageSize) {
        return imageSize.getLeft() > MAX_IMAGE_SIZE || imageSize.getRight() > MAX_IMAGE_SIZE;
    }


    private JobPicture createJobPictureModel(final String fullFileName, final Job job) {
        JobPicture modelJobPicture = new JobPicture();
        modelJobPicture.setPictureName(fullFileName);
        modelJobPicture.setJob(job);
        return modelJobPicture;
    }

    @Override
    public JobDTO changeJobStatus(Long jobId, int jobStatus, String usernameAdministrator) throws ApplicationException {
        validateJobId(jobId);

        User administrator = this.userRepository.findByUsername(usernameAdministrator)
                .orElseThrow(supplyUserNotFoundException);

        validateRoleIsAdmin(administrator);

        Job job = retrieveJob(jobId)
                .orElseThrow(supplyJobNotFoundException);

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
    public JobDTO update(Long jobId, JobDTO jobDTO, String ownerUsername) throws ApplicationException {
        validateJobId(jobId);
        validateUsername(ownerUsername);
        validateJobIdMatch(jobId, jobDTO);
        validateMaNumberOfAttachments(jobDTO);

        Job job = retrieveJob(jobId)
                .orElseThrow(supplyJobNotFoundException);

        User publisher = job.getPublisher();

        validateMakeSureIsAuthor(ownerUsername, publisher);

        this.jobMapper.getModelMapper().map(jobDTO, job);
        job.setStatus(JobConst.STATUS_UPDATED);
        Job jobSaved = this.jobRepository.save(job);

        deleteJobPicturesNotInDTOList(jobDTO, jobSaved);

        Iterable<JobPicture> savedJobPictureList = saveJobPicturesInDTOList(jobDTO, jobSaved);

        saveJobMainPicture(job, savedJobPictureList);

        return this.jobMapper.toDTO(jobSaved);
    }

    private static void validateMakeSureIsAuthor(String owner, User publisher) throws ApplicationException {
        if (!isSameUsername(owner, publisher.getUsername())) {
            throw new ApplicationException("wrong user");
        }
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
