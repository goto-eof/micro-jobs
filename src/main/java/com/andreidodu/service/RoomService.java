package com.andreidodu.service;

import com.andreidodu.dto.*;
import com.andreidodu.exception.ValidationException;

import java.util.List;

public interface RoomService {
    MessageDTO createMessage(String usernameFrom, MessageDTO messageDTO) throws ValidationException;

    RoomDTO getRoom(String username, Long jobId);

    MessageResponseDTO getMessages(String username, Long roomId, MessageRequestDTO messageRequest) throws ValidationException;

    List<RoomExtendedDTO> getRooms(String username);

    JobDTO getJobByRoomId(String extractUsernameFromAuthorizzation, Long roomId);
}
