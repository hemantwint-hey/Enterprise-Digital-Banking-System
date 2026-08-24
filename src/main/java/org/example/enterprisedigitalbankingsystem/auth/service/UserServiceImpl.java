package org.example.enterprisedigitalbankingsystem.auth.service;


import org.example.enterprisedigitalbankingsystem.auth.dto.request.LoginRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.RegisterRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.LoginResponse;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.RegisterResponse;
import org.example.enterprisedigitalbankingsystem.auth.entity.Role;
import org.example.enterprisedigitalbankingsystem.auth.entity.User;
import org.example.enterprisedigitalbankingsystem.auth.entity.UserStatus;
import org.example.enterprisedigitalbankingsystem.auth.repository.UserRepository;
import org.example.enterprisedigitalbankingsystem.exception.InvalidCredentialsException;
import org.example.enterprisedigitalbankingsystem.exception.UserAlreadyExistsException;
import org.example.enterprisedigitalbankingsystem.exception.UserNotFoundException;
import org.example.enterprisedigitalbankingsystem.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            throw new InvalidCredentialsException("Password do not match");
        }
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.CUSTOMER)
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

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new UserNotFoundException("Invalid email id"));
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Password didnt match");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .token(token)
                .build();
    }
}
