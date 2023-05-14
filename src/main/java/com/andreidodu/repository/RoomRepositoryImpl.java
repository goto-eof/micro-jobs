package com.andreidodu.repository;

import com.andreidodu.constants.MessageConst;
import com.andreidodu.model.message.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepository {
    private JPAQueryFactory queryFactory;

    @Autowired
    public RoomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Room> findByJobIdAndParticipants(Long jobId, String username) {

        QRoom room = QRoom.room;
        QParticipant participant = QParticipant.participant;
        var expression = JPAExpressions
                .select(participant.room.id)
                .from(participant)
                .where(participant.job.id.eq(jobId)
                        .and(participant.user.username.eq(username)));
        var roomModel = this.queryFactory
                .select(room)
                .from(room)
                .where(room.id.in(expression))
                .fetch();
        if (!roomModel.isEmpty()) {
            return Optional.of(roomModel.get(0));
        }
        return Optional.empty();
    }

    @Override
    public boolean userBelongsToRoom(String username, Long roomId) {
        QParticipant participant = QParticipant.participant;
        return queryFactory
                .selectFrom(participant)
                .where(participant.room.id.eq(roomId).and(participant.user.username.eq(username))).fetch().size() > 0;
    }

    @Override
    public boolean userBelongsToRoomAndIsJobAuthor(String username, Long roomId) {
        QParticipant participant = QParticipant.participant;
        return queryFactory
                .selectFrom(participant)
                .where(participant.room.id.eq(roomId)
                        .and(participant.user.username.eq(username))
                        .and(participant.job.publisher.username.eq(username)))
                .fetch().size() > 0;
    }

    @Override
    public List<Message> findMessagesByUsernameAndRoomId(String username, Long roomId, long offset, long count, long limit) {

        QMessage message = QMessage.message1;
        QParticipant participant = QParticipant.participant;
        var userIds = JPAExpressions
                .select(participant.user.id)
                .from(participant)
                .where(participant.room.id.eq(roomId));
        return queryFactory
                .select(message)
                .from(message)
                .where(message.room.id.eq(roomId).and(message.user.id.in(userIds)))
                .orderBy(message.createdDate.asc())
                .offset(count - offset)
                .limit(limit)
                .fetch();
    }


    @Override
    public Long countMessages(Long roomId) {
        QMessage message = QMessage.message1;
        QParticipant participant = QParticipant.participant;
        var userIds = JPAExpressions
                .select(participant.user.id)
                .from(participant)
                .where(participant.room.id.eq(roomId));
        return queryFactory
                .select(message.id.count())
                .from(message)
                .where(message.room.id.eq(roomId).and(message.user.id.in(userIds)))
                .fetchOne();
    }

    @Override
    public List<RoomExtended> findRoomsByUsername(String username) {
        QParticipant participant = QParticipant.participant;

        QRoom room = QRoom.room;
        return this.queryFactory
                .select(room.id, room.title, room.description, participant.job.id, room.pictureName)
                .distinct()
                .from(room, participant)
                .where(room.id.in(JPAExpressions
                                .select(participant.room.id)
                                .from(participant)
                                .where(participant.user.username.eq(username)))
                        .and(participant.room.id.eq(room.id))
                )
                .fetch().stream().map(rm -> new RoomExtended(
                        rm.get(0, Long.class),
                        rm.get(1, String.class),
                        rm.get(2, String.class),
                        rm.get(3, Long.class),
                        rm.get(4, String.class),
                        queryFactory
                                .selectFrom(participant)
                                .where(participant.room.id.eq(rm.get(0, Long.class)))
                                .stream()
                                .map(par -> par.getUser().getUsername())
                                .collect(Collectors.toList())
                )).collect(Collectors.toList());
    }
}
