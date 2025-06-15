package com.example.dao;

import com.example.model.User;
import java.util.List;

public interface UserDao extends Dao<User> {
    void create(User user);
    void update(User user);
    void remove(User user);
    List<User> findAll();
    User findByLogin(String login);
    User findByEmail(String email);
}
