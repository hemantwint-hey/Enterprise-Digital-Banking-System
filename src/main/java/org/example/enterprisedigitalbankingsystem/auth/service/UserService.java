package org.example.enterprisedigitalbankingsystem.auth.service;

import org.example.enterprisedigitalbankingsystem.auth.dto.request.LoginRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.RegisterRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.AuthResponse;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.LoginResponse;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.RegisterResponse;

public interface UserService {
    RegisterResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
}
