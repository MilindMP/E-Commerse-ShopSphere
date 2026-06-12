package com.shopsphere.user.controller;

import com.shopsphere.common.dto.UserDTO;
import com.shopsphere.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
        log.info("Register request for email: {}", userDTO.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String email,
            @RequestParam String password) {
        log.info("Login request for email: {}", email);
        String token = userService.authenticate(email, password);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        log.info("Get user request for userId: {}", userId);
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable String userId,
            @RequestBody UserDTO userDTO) {
        log.info("Update user request for userId: {}", userId);
        userService.updateUser(userId, userDTO);
        return ResponseEntity.ok().build();
    }
}
