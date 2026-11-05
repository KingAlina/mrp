package at.technikum.repository;

import at.technikum.model.User;

import java.util.List;

public interface UserRepository extends Repository<User, Integer> {
    
    User findByUsername(String username);
}
