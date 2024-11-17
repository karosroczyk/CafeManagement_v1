package com.cafe.usermanagement.service;

import com.cafe.usermanagement.dao.UserDAOJPA;
import com.cafe.usermanagement.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private UserDAOJPA userDAOJPA;
    private PasswordEncoder passwordEncoder;
    private JWTService jwtService;

    public AuthService(UserDAOJPA userDAOJPA, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.userDAOJPA = userDAOJPA;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String saveUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDAOJPA.save(user);
        return "User added";
    }

    public String generateToken(String username){
        return jwtService.generateToken(username);
    }

    public void validateToken(String token){
        jwtService.validateToken(token);
    }
}
