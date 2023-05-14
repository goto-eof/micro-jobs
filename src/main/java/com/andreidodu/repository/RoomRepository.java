package com.andreidodu.repository;

import com.andreidodu.model.message.Message;
import com.andreidodu.model.message.Room;
import com.andreidodu.model.message.RoomExtended;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    Optional<Room> findByJobIdAndParticipants(Long jobId, String username);

    boolean userBelongsToRoom(String username, Long roomId);

    boolean userBelongsToRoomAndIsJobAuthor(String username, Long roomId);

    List<Message> findMessagesByUsernameAndRoomId(String username, Long roomId, long offset, long count, long limit);

    Long countMessages(Long roomId);

    List<RoomExtended> findRoomsByUsername(String username);
}