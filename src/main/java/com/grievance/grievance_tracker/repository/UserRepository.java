package com.grievance.grievance_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grievance.grievance_tracker.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    //SELECT * FROM users WHERE email = ? (returns true/false)
    boolean existsByEmail(String email);
}
