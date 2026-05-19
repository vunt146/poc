package com.crm.poc.user.service;

import com.crm.poc.data.InMemoryStore;
import com.crm.poc.user.model.User;
import com.crm.poc.user.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final InMemoryStore<User> userStore;

    public UserService(InMemoryStore<User> userStore) {
        this.userStore = userStore;
    }

    public List<User> findSubordinates(String managerId) {
        // POC: return all CBBH users (since no auth)
        return userStore.findBy(user -> user.getRole() == UserRole.CBBH);
    }

    public User findById(String userId) {
        return userStore.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + userId));
    }

    public List<User> findAll() {
        return userStore.findAll();
    }
}
