package com.se.lsrs.common.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.domain.UserStatus;
import com.se.lsrs.user.repository.UserRepository;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public JwtCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof String)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        String loginId = (String) authentication.getPrincipal();

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        return user.getUserId();
    }
}
