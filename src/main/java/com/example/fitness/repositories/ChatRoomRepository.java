package com.example.fitness.repositories;

import com.example.fitness.entitties.ChatRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends CrudRepository<ChatRoom, Integer> {

    @Query(
            value = "SELECT * FROM chatroom WHERE chatroom_id = ?1",
            nativeQuery = true
    )
    ChatRoom findChatroomById(Integer chatroomId);

    @Query(
            value = "SELECT * FROM chatroom WHERE coach_id = ?1",
            nativeQuery = true
    )
    List<ChatRoom> findChatroomsByCoachId(Integer coachId);

    @Query(
            value = "SELECT * FROM chatroom WHERE user_id = ?1",
            nativeQuery = true
    )
    List<ChatRoom> findChatroomsByUserId(Integer userId);

    @Query(
            value = "SELECT * FROM chatroom",
            nativeQuery = true
    )
    List<ChatRoom> getAllChatrooms();

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM chatroom WHERE chatroom_id = ?1",
            nativeQuery = true
    )
    void deleteChatroomById(Integer chatroomId);

    @Transactional
    @Modifying
    @Query(
            value =
                    "INSERT INTO chatroom (" +
                            "coach_id, user_id" +
                            ") VALUES (" +
                            "?1, ?2" +
                            ")",
            nativeQuery = true
    )
    void addChatroom(
            Integer coachId,
            Integer userId
    );

    @Transactional
    @Modifying
    @Query(
            value =
                    "UPDATE chatroom SET " +
                            "coach_id = ?2, " +
                            "user_id = ?3 " +
                            "WHERE chatroom_id = ?1",
            nativeQuery = true
    )
    void updateChatroom(
            Integer chatroomId,
            Integer coachId,
            Integer userId
    );

    @Query(
            value = "SELECT COUNT(*) FROM chatroom WHERE coach_id = ?1",
            nativeQuery = true
    )
    Integer countChatroomsByCoachId(Integer coachId);


}
