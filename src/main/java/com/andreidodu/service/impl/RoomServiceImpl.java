package com.andreidodu.service.impl;

import com.andreidodu.constants.MessageConst;
import com.andreidodu.constants.RoomConst;
import com.andreidodu.dto.*;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Override
    public MessageDTO createMessage(String username, MessageDTO messageDTO) throws ValidationException {
        Long roomId = messageDTO.getRoomId();

        validateUserToRoomBelonging(username, roomId);

        Optional<Room> roomOptional = roomCrudRepository.findById(messageDTO.getRoomId());
        validateRoomExistence(roomOptional);
        Room room = roomOptional.get();

        Optional<User> userOptional = userRepository.findByUsername(username);
        validateUserExistence(userOptional);
        User user = userOptional.get();

        Message message = createMessage(messageDTO, user, room);
        messageRepository.save(message);

        return this.messageMapper.toDTO(message);
    }

    private static void validateRoomExistence(Optional<Room> roomOptional) throws ValidationException {
        if (roomOptional.isEmpty()) {
            throw new ValidationException("room does not exists");
        }
    }

    private static void validateUserExistence(Optional<User> userOptional) throws ValidationException {
        if (userOptional.isEmpty()) {
            throw new ValidationException("user does not exists");
        }
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
    public RoomDTO getRoom(String username, Long jobId) throws ValidationException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        validateUserExistence(userOptional);
        User user = userOptional.get();

        Optional<Room> roomOptional = roomRepository.findByJobIdAndParticipants(jobId, username);
        Room room = null;
        if (roomOptional.isEmpty()) {
            Optional<Job> jobOptional = jobRepository.findById(jobId);
            validateJobExistence(jobOptional);
            Job job = jobOptional.get();

            room = createRoom(job);
            room = roomCrudRepository.save(room);

            Participant participant = createParticipant(user, room, job);
            participantRepository.save(participant);
            participant = createHostParticipant(room, job);
            participantRepository.save(participant);
        } else {
            room = roomOptional.get();
        }

        return this.roomMapper.toDTO(room);
    }

    private static void validateJobExistence(Optional<Job> jobOptional) throws ValidationException {
        if (jobOptional.isEmpty()){
            throw new ValidationException("job does not exists");
        }
    }


    @Override
    public Optional<Long> retrieveWorkerId(String username, Long roomId) {
        Optional<Room> roomOpt = roomCrudRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }
        Room room = roomOpt.get();
        Long publishedId = room.getJob().getPublisher().getId();
        return room.getParticipants().stream()
                .filter(participant -> !participant.getUser().getId().equals(publishedId))
                .map(participant -> participant.getUser().getId()).findFirst();
    }

    private static Room createRoom(Job job) {
        Room room;
        room = new Room();
        room.setDescription(job.getDescription());
        room.setStatus(RoomConst.STATUS_CREATED);
        room.setTitle(job.getTitle());
        room.setJob(job);
        room.setPictureName(extractMainPictureName(job));
        return room;
    }

    private static String extractMainPictureName(Job job) {
        if (job.getJobPictureList() != null && job.getJobPictureList().size() > 0) {
            return job.getJobPictureList().get(0).getPictureName();
        }
        return "";
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
        MessageResponseDTO response = new MessageResponseDTO();
        List<Message> messagesByUsernameAndRoomIdList = roomRepository.findMessagesByUsernameAndRoomId(username, roomId, currentOffsetRequest, count, limit);
        List<MessageDTO> messageDTOList = this.messageMapper.toListDTO(messagesByUsernameAndRoomIdList);
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
