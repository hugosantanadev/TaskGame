package com.gasmtask.user.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.user.domain.User;
import com.gasmtask.user.domain.UserRegisteredEvent;
import com.gasmtask.user.dto.UpdateProfileRequest;
import com.gasmtask.user.dto.UserResponse;
import com.gasmtask.user.mapper.UserMapper;
import com.gasmtask.user.repository.UserRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public UserService(UserRepository repository, ApplicationEventPublisher events, Clock clock) {
        this.repository = repository;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public UserResponse register(NewUser newUser) {
        String email = User.normalizeEmail(newUser.email());
        if (repository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        Instant now = clock.instant();
        User user = User.register(email, newUser.passwordHash(), newUser.displayName(),
                ZoneId.of(newUser.timeZone()), now);
        try {
            repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Dois cadastros simultâneos com o mesmo e-mail: a constraint única decide.
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        events.publishEvent(new UserRegisteredEvent(user.getId(), user.zoneId(), now));
        return UserMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserCredentials> findCredentials(String email) {
        return repository.findByEmail(User.normalizeEmail(email))
                .map(user -> new UserCredentials(user.getId(), user.getPasswordHash()));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return UserMapper.toResponse(load(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = load(userId);
        Instant now = clock.instant();

        if (request.displayName() != null) {
            user.rename(request.displayName(), now);
        }
        if (request.timeZone() != null) {
            user.changeTimeZone(ZoneId.of(request.timeZone()), now);
        }
        if (request.rankingVisible() != null) {
            user.changeRankingVisibility(request.rankingVisible(), now);
        }
        return UserMapper.toResponse(user);
    }

    private User load(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
