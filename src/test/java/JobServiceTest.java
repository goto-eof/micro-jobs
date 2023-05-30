import com.andreidodu.constants.JobConst;
import com.andreidodu.dto.JobDTO;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.mapper.JobMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.PaymentType;
import com.andreidodu.model.User;
import com.andreidodu.model.UserPicture;
import com.andreidodu.repository.JobPageableRepository;
import com.andreidodu.repository.JobPictureRepository;
import com.andreidodu.repository.JobRepository;
import com.andreidodu.repository.UserRepository;
import com.andreidodu.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

import org.mockito.*;

import java.util.Optional;

public class JobServiceTest {
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobMapper jobMapper;
    private JobServiceImpl jobServiceImpl;

    @Mock
    UserRepository userRepository;

    @Mock
    JobPageableRepository jobPageableRepository;

    @Mock
    JobPictureRepository jobPictureRepository;

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
    @DisplayName("Assert throws exception when jobId is null")
    public void testValidateJobId_whenJobIdIsNull_throwException() {
        // Arrange
        Long emptyJobId = null;

        // Act & Assert
        assertThrows(ApplicationException.class, () -> jobServiceImpl.validateJobId(emptyJobId));
    }

    @Test
    @DisplayName("Assert throws exception when username is null")
    public void testValidateUsername_whenUsernameIsNull_throwException() {
        // Arrange
        String emptyUsername = null;

        // Act & Assert
        assertThrows(ApplicationException.class, () -> jobServiceImpl.validateUsername(emptyUsername));
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
}
