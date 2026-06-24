package com.journalapp.userservice.service;

import com.journalapp.userservice.dto.RegisterRequest;
import com.journalapp.userservice.entity.User;
import com.journalapp.userservice.event.UserEventProducer;
import com.journalapp.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventProducer = userEventProducer;
    }

    public boolean userExists(String userName) {
        return userRepository.existsByUserName(userName);
    }

    /** Creates a USER account, then publishes a USER_REGISTERED event. */
    public User register(RegisterRequest request) {
        User user = new User();
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(List.of("USER"));

        User saved = userRepository.save(user);
        log.info("Registered new user '{}'", saved.getUserName());

        userEventProducer.publishUserRegistered(saved);
        return saved;
    }

    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}
