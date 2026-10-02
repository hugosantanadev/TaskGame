package com.gasmtask.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.user.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u.id from User u order by u.createdAt")
    List<UUID> findAllIds();
}
