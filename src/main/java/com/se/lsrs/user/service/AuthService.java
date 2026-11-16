package com.se.lsrs.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        validatePassword(rawPassword);

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

    /** 비밀번호 규칙 (US-01 AC3): 8자 이상, 영문·숫자·특수문자 모두 포함. 부족한 항목을 안내한다. */
    static void validatePassword(String rawPassword) {
        List<String> lacking = new ArrayList<>();
        if (rawPassword.length() < 8) {
            lacking.add("8자 이상");
        }
        if (rawPassword.chars().noneMatch(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
            lacking.add("영문");
        }
        if (rawPassword.chars().noneMatch(Character::isDigit)) {
            lacking.add("숫자");
        }
        if (rawPassword.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) {
            lacking.add("특수문자");
        }
        if (!lacking.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "비밀번호는 8자 이상이며 영문·숫자·특수문자를 모두 포함해야 합니다. 보완할 항목: "
                            + String.join(", ", lacking));
        }
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

        // 비밀번호를 먼저 확인한다: 틀린 비밀번호로는 계정 상태(PENDING 등)를 알 수 없게 한다 (US-02 AC2)
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BusinessException(
                    ErrorCode.UNAUTHENTICATED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED,
                    "이메일 인증이 필요합니다. 인증을 완료한 뒤 로그인해 주세요.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.UNAUTHENTICATED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtProvider.generateAccessToken(user);
    }
}
