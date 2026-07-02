package com.example.fitness.services;

import com.example.fitness.entitties.WaitingRoom;
import com.example.fitness.repositories.WaitingRoomRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WaitingRoomService {

    private final WaitingRoomRepository waitingRoomRepository;

    public WaitingRoomService(WaitingRoomRepository waitingRoomRepository) {
        this.waitingRoomRepository = waitingRoomRepository;
    }

    public List<WaitingRoom> getAllWaitingRooms() {
        List<WaitingRoom> waitingRooms = new ArrayList<>();
        waitingRoomRepository.getAllWaitingRooms().forEach(waitingRooms::add);
        return waitingRooms;
    }

    public List<WaitingRoom> fetchAll() {
        return waitingRoomRepository.getAllWaitingRooms();
    }

    public WaitingRoom getWaitingRoomById(Integer id) {
        return waitingRoomRepository.findWaitingRoomById(id);
    }

    public List<WaitingRoom> getWaitingRoomsByChatroomId(Integer chatroomId) {
        return waitingRoomRepository.findWaitingRoomsByChatroomId(chatroomId);
    }

    public List<WaitingRoom> getWaitingRoomsByUserId(Integer userId) {
        return waitingRoomRepository.findWaitingRoomsByUserId(userId);
    }

    public void addWaitingRoom(
            Integer chatroomId,
            Integer userId
    ) {
        System.out.println("Waiting room entry created");
        waitingRoomRepository.addWaitingRoom(
                chatroomId,
                userId
        );
    }

    public void deleteWaitingRoomById(Integer id) {
        waitingRoomRepository.deleteWaitingRoomById(id);
    }

    public void updateWaitingRoom(
            Integer id,
            Integer chatroomId,
            Integer userId
    ) {
        waitingRoomRepository.updateWaitingRoom(
                id,
                chatroomId,
                userId
        );
    }

    public Integer countWaitingRoomsByChatroomId(Integer chatroomId) {
        return waitingRoomRepository.countWaitingRoomsByChatroomId(chatroomId);
    }

}
