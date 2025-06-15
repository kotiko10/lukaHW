package com.example.dao;

import java.util.List;

public interface Dao<E> {
    void create(E e);
    void update(E e);
    void remove(E e);
    List<E> findAll();
    E findById(Long id);
}
