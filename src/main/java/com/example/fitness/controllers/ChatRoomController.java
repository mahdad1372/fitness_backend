package com.example.fitness.controllers;

import com.example.fitness.entitties.ChatRoom;
import com.example.fitness.services.ChatroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/chatroom")
@RestController
public class ChatRoomController {

    private final ChatroomService chatroomService;

    public ChatRoomController(ChatroomService chatroomService) {
        this.chatroomService = chatroomService;
    }
    // Create chatroom
    @PostMapping("/create/{id}")
    public ResponseEntity<ChatRoom> createChatroom(@PathVariable("id") Integer id) {
        ChatRoom chatroom = chatroomService.createChatroom(id);
        return ResponseEntity.ok(chatroom);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<ChatRoom>> allChatrooms() {
        List<ChatRoom> chatrooms = chatroomService.fetchAll();
        return ResponseEntity.ok(chatrooms);
    }

    @PostMapping("/addchatroom")
    public void addChatroom(@RequestBody ChatRoom chatroom) {
        chatroomService.addChatroom(
                chatroom.getCoach_id(),
                chatroom.getUser_id()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getChatroomById(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                chatroomService.getChatroomById(id)
        );
    }

    @GetMapping("/coach/{id}")
    public ResponseEntity<List<ChatRoom>> getChatroomsByCoachId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                chatroomService.getChatroomsByCoachId(id)
        );
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ChatRoom>> getChatroomsByUserId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                chatroomService.getChatroomsByUserId(id)
        );
    }

    @DeleteMapping("/deletechatroom/{id}")
    public void deleteChatroomById(
            @PathVariable("id") Integer id
    ) {
        chatroomService.deleteChatroomById(id);
    }

    @PutMapping("/updatechatroom/{id}")
    public ResponseEntity<String> updateChatroom(
            @PathVariable("id") Integer id,
            @RequestBody ChatRoom chatroom
    ) {
        chatroomService.updateChatroom(
                id,
                chatroom.getCoach_id(),
                chatroom.getUser_id()
        );

        return ResponseEntity.ok(
                "Chatroom updated successfully"
        );
    }

    @GetMapping("/count/coach/{id}")
    public ResponseEntity<Integer> countChatroomsByCoachId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                chatroomService.countChatroomsByCoachId(id)
        );
    }

}
