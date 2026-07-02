package com.example.fitness.controllers;

import com.example.fitness.dto.UserJoinRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {
    private final List<int[]> waiting_list =
            Collections.synchronizedList(new ArrayList<>());
    private final Set<Long> activeUsers =
            ConcurrentHashMap.newKeySet();
    private Integer turn = 1;
    public String status;

    private final SimpMessagingTemplate messagingTemplate;



    public ChatController(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    // ==========================
    // REST API
    // ==========================
    @PostMapping("/addwaitinglist")
    @ResponseBody
    public synchronized ResponseEntity<String> addUserTurn(
            @RequestBody Map<String, Integer> request
    ) {
        int userId = request.get("userId");
//        int turn = request.get("turn");

        waiting_list.add(new int[]{userId, turn++});

        return ResponseEntity.ok("Added successfully");
    }
    @GetMapping("/getstatsroom")
    @ResponseBody
    public Map<String, Object> getStatsRoom() {

        Map<String, Object> response = new HashMap<>();

        response.put("status", this.status);

        return response;
    }
    @GetMapping("/getwaitinglist")
    @ResponseBody
    public List<int[]> getwaitinglist() {
        return this.waiting_list;
    }
    @GetMapping("/numberchatpeople")
    @ResponseBody
    public Integer getNumberChatPeople() {
        return activeUsers.size();
    }
    @PostMapping("/removewaitinglist")
    @ResponseBody
    public synchronized ResponseEntity<String> removeWaitingList(
            @RequestBody Map<String, Integer> request
    ) {
        int userId = request.get("userId");

        boolean removed = waiting_list.removeIf(
                item -> item[0] == userId
        );

        if (removed) {
            return ResponseEntity.ok(
                    "User removed from waiting list"
            );
        }

        return ResponseEntity
                .badRequest()
                .body("User not found");
    }
    @PostMapping("/checkuser")
    @ResponseBody
    public boolean checkUser(
            @RequestBody UserJoinRequest request
    ) {
        return activeUsers.contains(
                request.getUserId()
        );
    }
    @PostMapping("/addpeople")
    @ResponseBody
    public synchronized ResponseEntity<String> addPeople(
            @RequestBody UserJoinRequest request
    ) {

        Long userId = request.getUserId();

        if (activeUsers.contains(userId)) {
            return ResponseEntity.ok(
                    "User already in room"
            );
        }

        if (activeUsers.size() >= 2) {
            this.status = "FULL";
            return ResponseEntity
                    .badRequest()
                    .body("Room is full");
        }else {
            activeUsers.add(userId);

            System.out.println(
                    "User added: " + userId
            );

            System.out.println(
                    "Current users: " + activeUsers
            );

            return ResponseEntity.ok(
                    "Joined successfully"
            );
        }


    }

    @PostMapping("/removepeople")
    @ResponseBody
    public synchronized ResponseEntity<String> removePeople(
            @RequestBody UserJoinRequest request
    ) {

        activeUsers.remove(request.getUserId());
        System.out.println("User removed: " + request.getUserId());
        System.out.println("Current users: " + activeUsers);
        if(activeUsers.size() < 2){
            messagingTemplate.convertAndSend(
                    "/topic/notifications",
                    "🔔 Chat room is free now"
            );
        }
        return ResponseEntity.ok(
                "Removed successfully"
        );
    }

    // ==========================
    // WEBSOCKET CHAT
    // ==========================

    @MessageMapping("chat.sendMessage/{chatroomId}")
    @SendTo("/topic/chat/{chatroomId}")
    public Message sendMsg(
            @DestinationVariable Integer chatroomId,
            @Payload Message msg
    ) {
        return msg;
    }

    @MessageMapping("chat.clear/{chatroomId}")
    @SendTo("/topic/chat/{chatroomId}")
    public Message clearChat(
            @DestinationVariable Integer chatroomId,
            @Payload Message message
    ) {

        Message leaveMessage =
                new Message();

        leaveMessage.setSender(
                message.getSender()
        );

        leaveMessage.setType(
                MsgType.LEAVE
        );

        return leaveMessage;
    }

    @MessageMapping("chat.addUser/{chatroomId}")
    public void addUser(
            @DestinationVariable Integer chatroomId,
            @Payload Message msg,
            SimpMessageHeaderAccessor headerAccessor
    ) {

        headerAccessor
                .getSessionAttributes()
                .put(
                        "username",
                        msg.getSender()
                );

        Message joinMessage =
                new Message();

        joinMessage.setSender(
                msg.getSender()
        );

        joinMessage.setType(
                MsgType.JOIN
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/{chatroomId}",
                joinMessage
        );

        System.out.println(
                "Chat joined: "
                        + msg.getSender()
        );
    }
}