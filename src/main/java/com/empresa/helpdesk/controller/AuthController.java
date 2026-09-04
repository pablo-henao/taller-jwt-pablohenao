package com.empresa.helpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.helpdesk.dto.AuthRequest;
import com.empresa.helpdesk.dto.AuthResponse;
import com.empresa.helpdesk.model.Role;
import com.empresa.helpdesk.model.Usuario;
import com.empresa.helpdesk.repository.UsuarioRepository;
import com.empresa.helpdesk.security.JwtService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(registerUser(request, Role.CLIENTE));
    }

    @PostMapping("/register-soporte")
    public ResponseEntity<AuthResponse> registerSoporte(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(registerUser(request, Role.SOPORTE));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(registerUser(request, Role.ADMIN));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = usuarioRepository.findByUsername(request.username()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(jwtService.generateToken(user)));
    }

    private AuthResponse registerUser(AuthRequest request, Role role) {
        Usuario user = Usuario.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .role(role)
            .build();
        usuarioRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user));
    }
}
