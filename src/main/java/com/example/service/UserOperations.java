package com.example.service;

import com.example.model.Role;
import com.example.model.User;

import java.util.List;

public interface UserOperations {
    List<User> getAllUsers();
    User getUserById(Long id);
    void createUser(User user);
    void updateUser(User user, boolean updatePassword);
    void deleteUser(Long id);
    List<Role> getAllRoles();
}