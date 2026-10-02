package com.gasmtask.room.service;

import com.gasmtask.room.domain.Room;
import com.gasmtask.room.repository.RoomRepository;
import com.gasmtask.user.domain.UserRegisteredEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class RoomRegistrationListener {

    private final RoomRepository rooms;

    RoomRegistrationListener(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @EventListener
    void onUserRegistered(UserRegisteredEvent event) {
        rooms.save(Room.create(event.userId(), event.registeredAt()));
    }
}
