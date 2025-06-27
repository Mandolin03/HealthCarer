package com.evaruiz.healthcarer.service;

import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.model.LoggedUser;
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
        if (newUser == null || !newUser.validate()) {
            throw new IllegalArgumentException("Invalid user data provided");
        }
        UserDB user = new UserDB();
        user.setName(newUser.name());
        user.setEmail(newUser.email());
        user.setEncodedPassword(passwordEncoder.encode(newUser.password()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LoggedUser user = userRepository.findByEmail(email).map(LoggedUser::new).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        System.out.println("User found: " + user.getUser().getName());
        System.out.println("User found: " + user.getUser().getEmail());
        return user;


    }
}

