package com.example.dao;

import com.example.model.Role;

public interface RoleDao extends Dao<Role> {
    void create(Role role);
    void update(Role role);
    void remove(Role role);
    Role findByName(String name);
}
