package com.gasmtask.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.gasmtask.auth.dto.LoginRequest;
import com.gasmtask.auth.dto.RegisterRequest;
import com.gasmtask.auth.service.RefreshTokenService.Rotation;
import com.gasmtask.shared.exception.BusinessException;
import com.gasmtask.shared.exception.ErrorCode;
import com.gasmtask.shared.security.AccessTokenService;
import com.gasmtask.user.dto.UserResponse;
import com.gasmtask.user.service.NewUser;
import com.gasmtask.user.service.UserCredentials;
import com.gasmtask.user.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Limite do BCrypt: bytes além disso seriam ignorados. */
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final String timingEqualizerHash;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       AccessTokenService accessTokenService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        // Usado quando o e-mail não existe, para o login levar o mesmo tempo nos dois casos
        // e não revelar quais e-mails têm conta.
        this.timingEqualizerHash = passwordEncoder.encode("gasmtask-timing-equalizer");
    }

    @Transactional
    public AuthSession register(RegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());
        UserResponse user = userService.register(
                new NewUser(request.email(), passwordHash, request.displayName(), request.timeZone()));
        return openSession(user);
    }

    public AuthSession login(LoginRequest request) {
        Optional<UserCredentials> credentials = userService.findCredentials(request.email());
        String hashToCheck = credentials.map(UserCredentials::passwordHash).orElse(timingEqualizerHash);

        boolean passwordMatches = fitsBcrypt(request.password())
                && passwordEncoder.matches(request.password(), hashToCheck);
        if (credentials.isEmpty() || !passwordMatches) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return openSession(userService.getProfile(credentials.get().userId()));
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthSession refresh(String rawRefreshToken) {
        Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
        UserResponse user = userService.getProfile(rotation.userId());
        return new AuthSession(accessTokenService.issue(user.id()), rotation.refreshToken(), user);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.endSession(rawRefreshToken);
    }

    private AuthSession openSession(UserResponse user) {
        return new AuthSession(
                accessTokenService.issue(user.id()),
                refreshTokenService.startSession(user.id()),
                user);
    }

    private static boolean fitsBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES;
    }
}
