package com.andreidodu.service;

import com.andreidodu.dto.*;
import com.andreidodu.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    MessageDTO createMessage(String usernameFrom, MessageDTO messageDTO) throws ValidationException;

    RoomDTO getRoom(String username, Long jobId);

    Optional<Long> retrieveWorkerId(String username, Long roomId);

    MessageResponseDTO getMessages(String username, Long roomId, MessageRequestDTO messageRequest) throws ValidationException;

    List<RoomExtendedDTO> getRooms(String username);

    JobDTO getJobByRoomId(String extractUsernameFromAuthorizzation, Long roomId);
}
