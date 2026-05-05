package com.courtbooking.userservice.controller;

import com.courtbooking.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "User Validation", description = "Public endpoint for service-to-service validation")
public class UserValidationController {

    private final UserService userService;

    public UserValidationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/validate/{id}")
    @Operation(summary = "Validate user exists", description = "Validates if a user exists and is enabled")
    public ResponseEntity<Boolean> validateUser(@PathVariable Long id) {
        boolean isValid = userService.validateUser(id);
        return ResponseEntity.ok(isValid);
    }
}