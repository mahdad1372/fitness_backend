package com.example.fitness.controllers;
import com.example.fitness.entitties.WaitingRoom;
import com.example.fitness.services.WaitingRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/waitingroom")
@RestController
public class WaitingRoomController {

    private final WaitingRoomService waitingRoomService;

    public WaitingRoomController(WaitingRoomService waitingRoomService) {
        this.waitingRoomService = waitingRoomService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<WaitingRoom>> allWaitingRooms() {
        List<WaitingRoom> waitingRooms = waitingRoomService.fetchAll();
        return ResponseEntity.ok(waitingRooms);
    }

    @PostMapping("/addwaitingroom")
    public void addWaitingRoom(@RequestBody WaitingRoom waitingRoom) {
        waitingRoomService.addWaitingRoom(
                waitingRoom.getChatroom_id(),
                waitingRoom.getUser_id()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaitingRoom> getWaitingRoomById(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                waitingRoomService.getWaitingRoomById(id)
        );
    }

    @GetMapping("/chatroom/{id}")
    public ResponseEntity<List<WaitingRoom>> getWaitingRoomsByChatroomId(
            @PathVariable("id") Integer id
    ) {
        System.out.println("hello boy");
        return ResponseEntity.ok(
                waitingRoomService.getWaitingRoomsByChatroomId(id)
        );
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<WaitingRoom>> getWaitingRoomsByUserId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                waitingRoomService.getWaitingRoomsByUserId(id)
        );
    }

    @DeleteMapping("/deletewaitingroom/{id}")
    public void deleteWaitingRoomById(
            @PathVariable("id") Integer id
    ) {
        waitingRoomService.deleteWaitingRoomById(id);
    }

    @PutMapping("/updatewaitingroom/{id}")
    public ResponseEntity<String> updateWaitingRoom(
            @PathVariable("id") Integer id,
            @RequestBody WaitingRoom waitingRoom
    ) {
        waitingRoomService.updateWaitingRoom(
                id,
                waitingRoom.getChatroom_id(),
                waitingRoom.getUser_id()
        );

        return ResponseEntity.ok(
                "Waiting room updated successfully"
        );
    }

    @GetMapping("/count/chatroom/{id}")
    public ResponseEntity<Integer> countWaitingRoomsByChatroomId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(
                waitingRoomService.countWaitingRoomsByChatroomId(id)
        );
    }

}

