package com.ttjobs.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.ttjobs.backend.service.JwtService;

@Service
public class UserService {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    /**
     * @param user
     * @return
     */
    public User register(User user) {
        // hash the password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        // find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // make encoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // check password
        if (!encoder.matches(password, user.getPassword())) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return jwtService.generateToken(user.getEmail());

    }

}