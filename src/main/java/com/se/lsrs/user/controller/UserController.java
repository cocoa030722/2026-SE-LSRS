package com.se.lsrs.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> withdraw(
            @AuthenticationPrincipal String loginId) {

        userService.withdraw(loginId);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
