package com.example.fitness.entitties;

import jakarta.persistence.*;

@Table(name = "waiting_room")
@Entity
public class WaitingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer waiting_room_id;

    @Column(nullable = false)
    private Integer chatroom_id;

    @Column(nullable = false)
    private Integer user_id;

    public Integer getId() {
        return waiting_room_id;
    }

    public void setId(Integer id) {
        this.waiting_room_id = id;
    }

    public Integer getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(Integer chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
}