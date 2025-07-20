package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterUserDTO newUser) {

        String normalizedEmail = newUser.email().trim();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email already in use: " + normalizedEmail);

        }

        UserDB user = new UserDB();
        user.setName(newUser.name());
        user.setEmail(normalizedEmail);
        user.setEncodedPassword(passwordEncoder.encode(newUser.password()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).map(LoggedUser::new).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public boolean findByEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail).isPresent();
    }

    public void updateUserProfile(UserDB user, RegisterUserDTO updatedUser) {
        user.setName(updatedUser.name());
        user.setEmail(updatedUser.email().trim());
        if (updatedUser.password() != null && !updatedUser.password().isEmpty()) {
            user.setEncodedPassword(passwordEncoder.encode(updatedUser.password()));
        }
        userRepository.save(user);
    }

    public UserDB findById(java.lang.Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public void removeTakeFromUser(Long currentUser, Long id) {
        UserDB user = userRepository.findById(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUser));
        user.getTakes().removeIf(take -> take.getId().equals(id));
        userRepository.save(user);
    }
}

