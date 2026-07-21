package org.example.enterprisedigitalbankingsystem.auth.controller;

import jakarta.validation.Valid;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.LoginRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.request.RegisterRequest;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.LoginResponse;
import org.example.enterprisedigitalbankingsystem.auth.dto.response.RegisterResponse;
import org.example.enterprisedigitalbankingsystem.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(userService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }
}
