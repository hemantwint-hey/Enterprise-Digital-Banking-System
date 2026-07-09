package org.example.enterprisedigitalbankingsystem.auth.service;


import org.example.enterprisedigitalbankingsystem.auth.dto.request.LoginRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.RegisterRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.LoginResponse;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.RegisterResponse;
import org.example.enterprisedigitalbankingsystem.auth.entity.Role;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.auth.entity.UserStatus;
import org.example.enterprisedigitalbankingsystem.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if(!registerRequest.getPassword().equals(registerRequest.getConfirm_password())){
            throw new RuntimeException("Passowrd do not match");
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .role(Role.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .kycVerified(false)
                .emailVerified(false)
                .phoneVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return RegisterResponse.builder()
                .success(true)
                .message("User Registered Successfully")
                .build();
    }
//login code
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email id"));
        if(!loginRequest.getPassword().equals(user.getPassword())){
            throw new RuntimeException("Password didnt match");
        }

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .build();
    }
}
