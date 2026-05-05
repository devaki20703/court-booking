package com.courtbooking.userservice.service;

import com.courtbooking.userservice.dto.AuthResponse;
import com.courtbooking.userservice.dto.LoginRequest;
import com.courtbooking.userservice.dto.RegisterRequest;
import com.courtbooking.userservice.dto.UserDTO;
import com.courtbooking.userservice.entity.Role;
import com.courtbooking.userservice.entity.User;
import com.courtbooking.userservice.exception.BadRequestException;
import com.courtbooking.userservice.exception.ResourceNotFoundException;
import com.courtbooking.userservice.repository.UserRepository;
import com.courtbooking.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("token123");

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = new User("testuser", "test@example.com", "encodedPassword", Role.USER, true);
        user.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("token123");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenInvalidCredentials() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = new User("testuser", "test@example.com", "encodedPassword", Role.USER, true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    void shouldThrowExceptionWhenUserDisabled() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = new User("testuser", "test@example.com", "encodedPassword", Role.USER, false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.login(request));
    }

    @Test
    void shouldReturnUserById() {
        User user = new User("testuser", "test@example.com", "encodedPassword", Role.USER, true);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO dto = userService.getUserById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void shouldValidateUserWhenExistsAndEnabled() {
        User user = new User("testuser", "test@example.com", "encodedPassword", Role.USER, true);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.validateUser(1L);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.validateUser(1L);

        assertFalse(result);
    }
}