package com.crm.poc.user.controller;

import com.crm.poc.user.model.User;
import com.crm.poc.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/subordinates")
    public ResponseEntity<List<User>> getSubordinates(
            @RequestParam(required = false) String managerId) {
        List<User> subordinates = userService.findSubordinates(managerId);
        return ResponseEntity.ok(subordinates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
}
