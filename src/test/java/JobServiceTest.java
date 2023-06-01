import com.andreidodu.constants.JobConst;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.JobMapper;
import com.andreidodu.model.*;
import com.andreidodu.repository.JobPageableRepository;
import com.andreidodu.repository.JobPictureRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;

import org.mockito.*;

import java.util.Arrays;
import java.util.Optional;

public class JobServiceTest {
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobMapper jobMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    JobPageableRepository jobPageableRepository;
    @Mock
    JobPictureRepository jobPictureRepository;
    private JobServiceImpl jobServiceImpl;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        jobServiceImpl = new JobServiceImpl(jobRepository, userRepository, jobPageableRepository, jobPictureRepository, jobMapper);
    }

    @Test
    @DisplayName("Assert it calls findById one time")
    public void testGetPrivateJob_whenJobIdAndUsernameProvided_returnJob() {
        // Arrange
        Job mockJob = prepareJobMock();
        Mockito.doReturn(Optional.of(mockJob)).when(jobRepository).findById(1L);

        // Act
        JobDTO jobDTO = jobServiceImpl.getPrivate(1L, "test");

        // Assert
        Mockito.verify(jobRepository, Mockito.times(1))
                .findById(anyLong());
    }

    @Test
    @DisplayName("Assert it throws exception when getPrivate is called with empty jobId")
    public void testGetPrivateJob_whenJobIdIsNull_throwException() {
        // Arrange
        Long emptyJobId = null;

        // Act
        assertThrows(ApplicationException.class, () -> jobServiceImpl.getPrivate(emptyJobId, "test"));

        // Assert
        Mockito.verify(jobRepository, Mockito.times(0))
                .findById(anyLong());
    }


    @Test
    @DisplayName("Assert it throws exception when getPrivate is called with empty username")
    public void testGetPrivateJob_whenUsernameIsNull_throwException() {
        // Arrange
        Long jobId = 1l;
        String emptyUsername = null;

        // Act
        assertThrows(ApplicationException.class, () -> jobServiceImpl.getPrivate(jobId, emptyUsername));

        // Assert
        Mockito.verify(jobRepository, Mockito.times(0))
                .findById(anyLong());
    }

    @Test
    @DisplayName("Assert it throws exception when getPrivate is called with not same username")
    public void testGetPrivateJob_whenUsernameIsNotSame_throwException() {
        // Arrange
        String username = "mario.rossi";
        Job mockData = prepareJobMock();

        // Act & Assert
        long jobId = 1L;
        Mockito.doReturn(Optional.of(mockData)).when(jobRepository).findById(jobId);
        assertThrows(ApplicationException.class, () -> jobServiceImpl.getPrivate(jobId, username));
        Mockito.verify(jobMapper, Mockito.times(0))
                .toDTO(mockData);
    }

    @Test
    @DisplayName("Assert it calls toDTO when getPrivate is called with same username")
    public void testGetPrivateJob_whenUsernameIsSame_callToDTO() {
        // Arrange
        String username = "test";
        Job mockData = prepareJobMock();
        long jobId = 1L;
        Mockito.doReturn(Optional.of(mockData)).when(jobRepository).findById(jobId);

        // Act
        JobDTO jobDTO = jobServiceImpl.getPrivate(jobId, username);

        // Assert
        Mockito.verify(jobMapper, Mockito.times(1))
                .toDTO(mockData);
    }

    private static Job prepareJobMock() {
        Job mockJob = new Job();
        mockJob.setPicture("picture-test.png");
        mockJob.setStatus(JobConst.STATUS_CREATED);
        User publisher = new User();
        publisher.setPaymentType(new PaymentType());
        publisher.setUserPicture(new UserPicture());

        publisher.setUsername("test");
        mockJob.setPublisher(publisher);
        mockJob.setDescription("description test");
        mockJob.setPrice(2.0);
        mockJob.setTitle("title test");
        mockJob.setType(JobConst.TYPE_REQUEST);
        mockJob.setId(1l);
        return mockJob;
    }

    @Test
    @DisplayName("Assert it calls findByIdAndStatus one time")
    public void testGetPublicJob_whenJobIdAndUsernameProvided_returnJob() {
        // Arrange
        Job mockJob = prepareJobMock();
        Mockito.doReturn(Optional.of(mockJob)).when(jobRepository).findByIdAndStatus(1L, JobConst.STATUS_PUBLISHED);

        // Act
        JobDTO jobDTO = jobServiceImpl.getPublic(1L);

        // Assert
        Mockito.verify(jobRepository, Mockito.times(1))
                .findByIdAndStatus(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Assert it throws exception when getPrivate is called with empty jobId")
    public void testGetPublicJob_whenJobIdIsNull_throwException() {
        // Arrange
        Long emptyJobId = null;

        // Act
        assertThrows(ApplicationException.class, () -> jobServiceImpl.getPublic(emptyJobId));

        // Assert
        Mockito.verify(jobRepository, Mockito.times(0))
                .findByIdAndStatus(anyLong(), anyInt());
    }


    @Test
    @DisplayName("Assert that getAllPrivateByTypeAndStatus throws exception if is not the admin")
    public void testGetAllPrivateByTypeAndStatus_whenNotAdmin_throwException() {
        // Arrance
        User simpleUser = createSimpleUser();
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(simpleUser));

        // Act & Assert
        assertThrows(ApplicationException.class, () -> jobServiceImpl.countAllPrivateByTypeAndStatus(1, Arrays.asList(1), "user"));
    }

    private User createSimpleUser() {
        User simpleUser = new User();
        simpleUser.setRole(Role.USER);
        return simpleUser;
    }


    @Test
    @DisplayName("Assert that is admin role for countAllPrivateByTypeAndStatus")
    public void testCountAllPrivateByTypeAndStatus_whenNotAdmin_throwException() {
        // Arrange
        User simpleUser = createSimpleUser();
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(simpleUser));

        // Act & Assert
        assertThrows(ApplicationException.class, () -> jobServiceImpl.countAllPrivateByTypeAndStatus(1, Arrays.asList(1), "user"));
    }


    @Test
    @DisplayName("Assert that is admin role for countAllPrivateByTypeAndStatus")
    public void testCountAllPrivateByTypeAndStatus_whenUsernameInvalid_throwException() {
        // Arrange
        String username = null;

        // Act & Assert
        assertThrows(ApplicationException.class, () -> jobServiceImpl.countAllPrivateByTypeAndStatus(1, Arrays.asList(1), username));
    }
}
