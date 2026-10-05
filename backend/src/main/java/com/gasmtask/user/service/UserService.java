package com.gasmtask.user.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.user.domain.ReminderSettings;
import com.gasmtask.user.domain.User;
import com.gasmtask.user.domain.UserRegisteredEvent;
import com.gasmtask.user.dto.ReminderSettingsRequest;
import com.gasmtask.user.dto.ReminderSettingsResponse;
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

    @Transactional(readOnly = true)
    public ReminderSettingsResponse reminderSettings(UUID userId) {
        return ReminderSettingsResponse.of(load(userId).getReminderSettings());
    }

    @Transactional
    public ReminderSettingsResponse updateReminderSettings(UUID userId, ReminderSettingsRequest request) {
        User user = load(userId);
        user.changeReminderSettings(ReminderSettings.of(request.tasksEnabled(), request.leadMinutes(),
                request.bedtime(), request.wakeTime()), clock.instant());
        return ReminderSettingsResponse.of(user.getReminderSettings());
    }

    /** Troca o título exibido; quem confere se o título foi ganho é o módulo do personagem. */
    @Transactional
    public UserResponse changeActiveTitle(UUID userId, String titleCode) {
        User user = load(userId);
        user.changeActiveTitle(titleCode, clock.instant());
        return UserMapper.toResponse(user);
    }

    /** Fuso e data de cadastro: base de "hoje" e da exceção do primeiro dia (RN03). */
    @Transactional(readOnly = true)
    public UserTimeInfo timeInfo(UUID userId) {
        User user = load(userId);
        ZoneId zone = user.zoneId();
        return new UserTimeInfo(zone, LocalDate.ofInstant(user.getCreatedAt(), zone));
    }

    /** Ids de todos os usuários, para os jobs que percorrem a base (fechamento do dia). */
    @Transactional(readOnly = true)
    public List<UUID> allIds() {
        return repository.findAllIds();
    }

    private User load(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
