package com.grievance.grievance_tracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grievance.grievance_tracker.model.Role;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register a new citizen
    public User registerCitizen(User user){

        // Check if email already exists
        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already registered");
        }

         // Encrypt password before saving — NEVER save plain text password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign CITIZEN role by default
        user.setRole(Role.CITIZEN);
        return userRepository.save(user);
    }

    // Find user by email (used during login)
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }


    // Find user by ID
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }


    // Get all users (for admin)
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }


    // Create an admin user (called manually or at startup)
    public User createAdmin(User user){


        // Check if email already exists
        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already registered");
        }


        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        
        // Assign ADMIN role
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

}
