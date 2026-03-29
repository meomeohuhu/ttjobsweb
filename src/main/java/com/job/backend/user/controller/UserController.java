package com.job.backend.user.controller;

import com.job.backend.user.dto.UpdateUserRequest;
import com.job.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        return ResponseEntity.ok(userService.getMe());
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }
}