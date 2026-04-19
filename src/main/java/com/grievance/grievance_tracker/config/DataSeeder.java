package com.grievance.grievance_tracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.grievance.grievance_tracker.model.Role;
import com.grievance.grievance_tracker.model.User;
import com.grievance.grievance_tracker.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only create admin if no admin exists yet
        if (!userRepository.existsByEmail("admin@grievance.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@grievance.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println(">>> Default admin created: admin@grievance.com / admin123");
        }
    }
}
