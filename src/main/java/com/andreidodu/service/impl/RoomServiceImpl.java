package com.andreidodu.service.impl;

import com.andreidodu.constants.MessageConst;
import com.andreidodu.constants.RoomConst;
import com.andreidodu.dto.*;
import com.andreidodu.exception.ApplicationException;
import com.andreidodu.exception.ValidationException;
import com.andreidodu.mapper.JobMapper;
import com.andreidodu.mapper.MessageMapper;
import com.andreidodu.mapper.RoomExtendedMapper;
import com.andreidodu.mapper.RoomMapper;
import com.andreidodu.model.Job;
import com.andreidodu.model.User;
import com.andreidodu.model.message.Message;
import com.andreidodu.model.message.Participant;
import com.andreidodu.model.message.Room;
import com.andreidodu.repository.*;
import com.andreidodu.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomCrudRepository roomCrudRepository;
    private final JobRepository jobRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final ParticipantRepository participantRepository;
    private final RoomMapper roomMapper;
    private final RoomExtendedMapper roomExtendedMapper;
    private final JobMapper jobMapper;

    private Supplier<ApplicationException> supplyRoomNotFoundException = () -> new ApplicationException("room not found");
    private Supplier<ApplicationException> supplyUserNotFoundException = () -> new ApplicationException("user not found");
    private Supplier<ApplicationException> supplyJobNotFoundException = () -> new ApplicationException("Job not found");

    @Override
    public MessageDTO createMessage(String username, MessageDTO messageDTO) throws ApplicationException {
        Long roomId = messageDTO.getRoomId();

        validateUserToRoomBelonging(username, roomId);

        Room room = retrieveRoom(messageDTO)
                .orElseThrow(supplyRoomNotFoundException);

        User user = retrieveUser(username)
                .orElseThrow(supplyUserNotFoundException);

        Message message = createMessage(messageDTO, user, room);
        Message savedMessage = getSavedMessage(message);

        return this.messageMapper.toDTO(savedMessage);
    }

    private Message getSavedMessage(Message message) {
        return messageRepository.save(message);
    }

    private Optional<User> retrieveUser(String username) {
        return userRepository.findByUsername(username);
    }

    private Optional<Room> retrieveRoom(MessageDTO messageDTO) {
        return retrieveRoom(messageDTO.getRoomId());
    }

    private void validateUserToRoomBelonging(String username, Long roomId) throws ValidationException {
        if (!isUserBelongsToRoom(username, roomId)) {
            throw new ValidationException("wrong room id");
        }
    }

    private boolean isUserBelongsToRoom(String username, Long roomId) {
        return roomRepository.userBelongsToRoom(username, roomId);
    }

    private static Message createMessage(MessageDTO messageDTO, User user, Room room) {
        Message message = new Message();
        message.setMessage(messageDTO.getMessage());
        message.setStatus(MessageConst.STATUS_CREATED);
        message.setUser(user);
        message.setRoom(room);
        return message;
    }

    @Override
    public RoomDTO getRoom(String username, Long jobId) throws ApplicationException {
        User user = retrieveUser(username)
                .orElseThrow(supplyUserNotFoundException);

        Optional<Room> roomOptional = retrieveRoom(username, jobId);

        if (roomOptional.isEmpty()) {
            Job job = retrieveJob(jobId)
                    .orElseThrow(supplyJobNotFoundException);

            Room room = createRoom(job);
            room = saveRoom(room);

            Participant participant = createParticipant(user, room, job);
            saveParticipant(participant);

            participant = createHostParticipant(room, job);
            saveParticipant(participant);

            return this.roomMapper.toDTO(room);
        }
        return this.roomMapper.toDTO(roomOptional.get());
    }

    private void saveParticipant(Participant participant) {
        participantRepository.save(participant);
    }

    private Room saveRoom(Room room) {
        return roomCrudRepository.save(room);
    }

    private Optional<Room> retrieveRoom(String username, Long jobId) {
        return roomRepository.findByJobIdAndParticipants(jobId, username);
    }

    private Optional<Job> retrieveJob(Long jobId) {
        return jobRepository.findById(jobId);
    }

    @Override
    public Optional<Long> retrieveWorkerId(String username, Long roomId) {
        Optional<Room> roomOptional = retrieveRoom(roomId);
        if (roomOptional.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOptional.get();
        Long publishedId = room.getJob().getPublisher().getId();

        return retrieveWorkerIdFromParticipants(room, publishedId);
    }

    private static Optional<Long> retrieveWorkerIdFromParticipants(Room room, Long publishedId) {
        return room.getParticipants().stream()
                .filter(participant -> !participant.getUser().getId().equals(publishedId))
                .map(participant -> participant.getUser().getId()).findFirst();
    }

    private Optional<Room> retrieveRoom(Long roomId) {
        return roomCrudRepository.findById(roomId);
    }

    private static Room createRoom(Job job) {
        Room room;
        room = new Room();
        room.setDescription(job.getDescription());
        room.setStatus(RoomConst.STATUS_CREATED);
        room.setTitle(job.getTitle());
        room.setJob(job);
        room.setPictureName(extractMainPictureName(job).orElseGet(() -> null));
        return room;
    }

    private static Optional<String> extractMainPictureName(Job job) {
        if (job.getJobPictureList() != null && job.getJobPictureList().size() > 0) {
            return Optional.of(job.getJobPictureList().get(0).getPictureName());
        }
        return Optional.empty();
    }

    private static Participant createHostParticipant(Room room, Job job) {
        Participant participant;
        participant = new Participant();
        participant.setRoom(room);
        participant.setUser(job.getPublisher());
        participant.setJob(job);
        return participant;
    }

    private static Participant createParticipant(User user, Room room, Job job) {
        Participant participant = new Participant();
        participant.setRoom(room);
        participant.setUser(user);
        participant.setJob(job);
        return participant;
    }


    @Override
    public MessageResponseDTO getMessages(String username, Long roomId, MessageRequestDTO messageRequestDTO) throws ValidationException {
        validateUserToRoomBelonging(username, roomId);
        long currentOffsetRequest = messageRequestDTO.getOffsetRequest();
        long lastOffset = messageRequestDTO.getLastOffset();
        long count = roomRepository.countMessages(roomId);
        long limit = MessageConst.NUM_OF_MESSAGES_LIMIT;
        if (currentOffsetRequest > lastOffset) {
            limit = (currentOffsetRequest - lastOffset);
        }
        if (currentOffsetRequest > count) {
            messageRequestDTO.setOffsetRequest(currentOffsetRequest);
            currentOffsetRequest = count;
        }
        List<Message> messagesByUsernameAndRoomIdList = roomRepository.findMessagesByUsernameAndRoomId(username, roomId, currentOffsetRequest, count, limit);
        List<MessageDTO> messageDTOList = this.messageMapper.toListDTO(messagesByUsernameAndRoomIdList);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessages(messageDTOList);
        long nextOffset = calculateNewOffset(messageRequestDTO, count);
        response.setNextOffset(nextOffset);

        return response;

    }

    private static long calculateNewOffset(MessageRequestDTO messageRequestDTO, long count) {
        long offset = messageRequestDTO.getOffsetRequest();
        if (offset == count || offset > count) {
            offset = -1;
        } else {
            offset += MessageConst.NUM_OF_MESSAGES_LIMIT;
            if (offset > count) {
                offset = count;
            }
        }
        return offset;
    }

    @Override
    public List<RoomExtendedDTO> getRooms(String username) {
        return roomExtendedMapper.toListDTO(roomRepository.findRoomsByUsername(username));
    }

    @Override
    public JobDTO getJobByRoomId(String extractUsernameFromAuthorizzation, Long roomId) {
        return this.jobMapper.toDTO(this.roomCrudRepository.findById(roomId).get().getJob());
    }
}
