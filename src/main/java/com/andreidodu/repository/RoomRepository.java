package com.andreidodu.repository;

import com.andreidodu.model.message.Message;
import com.andreidodu.model.message.Room;
import com.andreidodu.model.message.RoomExtended;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    Optional<Room> findByJobIdAndParticipants(Long jobId, String username);

    List<Message> findMessagesByUsernameAndRoomId(String username, Long jobId);

    List<RoomExtended> findRoomsByUsername(String username);
}