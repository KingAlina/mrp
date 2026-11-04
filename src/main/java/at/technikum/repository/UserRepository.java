package at.technikum.repository;

import at.technikum.model.User;

import java.util.List;

public interface UserRepository {
    void save(User user);
    User findById(int id);
    User findByUsername(String username);
    void delete(int id);
    List<User> findAll();
}
