package com.se.lsrs.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.user.domain.Role;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.domain.UserStatus;
import com.se.lsrs.user.domain.VerificationToken;
import com.se.lsrs.user.repository.UserRepository;
import com.se.lsrs.user.repository.VerificationTokenRepository;
import com.se.lsrs.user.security.JwtProvider;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public void signup(String loginId, String email, String rawPassword) {
        if (loginId == null || loginId.isBlank()
                || email == null || email.isBlank()
                || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(loginId, email, encodedPassword,
                Role.STUDENT, UserStatus.PENDING);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken(
                email, token, LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(verificationToken);

        System.out.println("이메일 인증 토큰: " + token);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT, "유효하지 않은 인증 토큰입니다."));

        if (verificationToken.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "인증 토큰이 만료되었습니다.");
        }

        User user = userRepository.findByEmail(verificationToken.getEmail())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT, "사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "인증할 수 없는 계정입니다.");
        }

        user.activate();
        verificationTokenRepository.deleteByEmail(verificationToken.getEmail());
    }

    @Transactional(readOnly = true)
    public String login(String loginId, String rawPassword) {
        if (loginId == null || rawPassword == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UNAUTHENTICATED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "로그인할 수 없는 계정입니다.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(
                    ErrorCode.UNAUTHENTICATED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtProvider.generateAccessToken(user);
    }
}
