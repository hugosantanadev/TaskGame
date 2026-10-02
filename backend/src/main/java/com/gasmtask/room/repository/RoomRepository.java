package com.gasmtask.room.repository;

import java.util.Optional;
import java.util.UUID;

import com.gasmtask.room.domain.Room;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findByUserId(UUID userId);
}
