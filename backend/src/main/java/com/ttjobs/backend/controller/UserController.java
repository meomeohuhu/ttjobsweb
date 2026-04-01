package com.ttjobs.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/admin/test")
    public String adminTest() {
        return "Admin access OK";
    }

    @GetMapping("/users/info")
    public String userInfo() {
        return "User Info OK";
    }
}
