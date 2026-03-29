package com.ttjobs.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ttjobs.backend.dto.LoginRequest;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return userService.register(user);
    }
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){
        return userService.login(request.getEmail(), request.getPassword());
    }
    @GetMapping("/api/admin/test")
    public String adminTest() {
        return "Admin access OK";
    }
    

}