package com.example.fitness.services;

import com.example.fitness.entitties.ChatRoom;
import com.example.fitness.repositories.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatroomService {


    private final ChatRoomRepository chatroomRepository;

    public ChatroomService(ChatRoomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }
    public ChatRoom createChatroom(Integer coachId) {
        ChatRoom chatroom = new ChatRoom();
        chatroom.setCoach_id(coachId);
        chatroom.setUser_id(null); // free by default
        return chatroomRepository.save(chatroom);
    }
    public List<ChatRoom> getAllChatrooms() {
        List<ChatRoom> chatrooms = new ArrayList<>();
        chatroomRepository.getAllChatrooms().forEach(chatrooms::add);
        return chatrooms;
    }

    public List<ChatRoom> fetchAll() {
        return chatroomRepository.getAllChatrooms();
    }

    public ChatRoom getChatroomById(Integer id) {
        return chatroomRepository.findChatroomById(id);
    }

    public List<ChatRoom> getChatroomsByCoachId(Integer coachId) {
        return chatroomRepository.findChatroomsByCoachId(coachId);
    }

    public List<ChatRoom> getChatroomsByUserId(Integer userId) {
        return chatroomRepository.findChatroomsByUserId(userId);
    }

    public void addChatroom(
            Integer coachId,
            Integer userId
    ) {
        System.out.println("Chatroom created");
        chatroomRepository.addChatroom(
                coachId,
                userId
        );
    }

    public void deleteChatroomById(Integer chatroomId) {
        chatroomRepository.deleteChatroomById(chatroomId);
    }

    public void updateChatroom(
            Integer chatroomId,
            Integer coachId,
            Integer userId
    ) {
        chatroomRepository.updateChatroom(
                chatroomId,
                coachId,
                userId
        );
    }

    public Integer countChatroomsByCoachId(Integer coachId) {
        return chatroomRepository.countChatroomsByCoachId(coachId);
    }


}
