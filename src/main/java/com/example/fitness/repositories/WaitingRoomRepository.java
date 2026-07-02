package com.example.fitness.repositories;


import com.example.fitness.entitties.WaitingRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitingRoomRepository extends CrudRepository<WaitingRoom, Integer> {


    @Query(
            value = "SELECT * FROM waiting_room WHERE id = ?1",
            nativeQuery = true
    )
    WaitingRoom findWaitingRoomById(Integer id);

    @Query(
            value = "SELECT * FROM waiting_room WHERE chatroom_id = ?1 ORDER BY waiting_room_id ASC",
            nativeQuery = true
    )
    List<WaitingRoom> findWaitingRoomsByChatroomId(Integer chatroomId);

    @Query(
            value = "SELECT * FROM waiting_room WHERE user_id = ?1",
            nativeQuery = true
    )
    List<WaitingRoom> findWaitingRoomsByUserId(Integer userId);

    @Query(
            value = "SELECT * FROM waiting_room",
            nativeQuery = true
    )
    List<WaitingRoom> getAllWaitingRooms();

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM waiting_room WHERE user_id = ?1",
            nativeQuery = true
    )
    void deleteWaitingRoomById(Integer id);

    @Transactional
    @Modifying
    @Query(
            value =
                    "INSERT INTO waiting_room (" +
                            "chatroom_id, user_id" +
                            ") VALUES (" +
                            "?1, ?2" +
                            ")",
            nativeQuery = true
    )
    void addWaitingRoom(
            Integer chatroomId,
            Integer userId
    );

    @Transactional
    @Modifying
    @Query(
            value =
                    "UPDATE waiting_room SET " +
                            "chatroom_id = ?2, " +
                            "user_id = ?3 " +
                            "WHERE id = ?1",
            nativeQuery = true
    )
    void updateWaitingRoom(
            Integer id,
            Integer chatroomId,
            Integer userId
    );

    @Query(
            value = "SELECT COUNT(*) FROM waiting_room WHERE chatroom_id = ?1",
            nativeQuery = true
    )
    Integer countWaitingRoomsByChatroomId(Integer chatroomId);


}

