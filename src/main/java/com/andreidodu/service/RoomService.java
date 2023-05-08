package com.andreidodu.service;

import com.andreidodu.dto.MessageDTO;
import com.andreidodu.dto.RoomDTO;
import com.andreidodu.dto.RoomExtendedDTO;

import java.util.List;

public interface RoomService {
    MessageDTO createMessage(String usernameFrom, MessageDTO messageDTO);

    RoomDTO getRoom(String username, Long jobId);

    List<MessageDTO> getMessages(String username, Long roomId);

    List<RoomExtendedDTO> getRooms(String username);
}
