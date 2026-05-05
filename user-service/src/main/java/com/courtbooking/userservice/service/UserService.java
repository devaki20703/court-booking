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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            Role.USER,
            true
        );

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getId(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BadRequestException("Invalid username/email or password")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username/email or password");
        }

        if (!user.getEnabled()) {
            throw new BadRequestException("User account is disabled");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getId(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean validateUser(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEnabled)
                .orElse(false);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.getEnabled());
        return dto;
    }
}
