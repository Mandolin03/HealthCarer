package com.evaruiz.healthcarer.integration;

import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDB savedUser;
    private RegisterUserDTO registerUserDTO;
    private RegisterUserDTO updatedUserDTO;

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();

        UserDB userToSave = new UserDB();
        userToSave.setName("Initial User");
        userToSave.setEmail("initial@example.com");
        userToSave.setEncodedPassword(passwordEncoder.encode("initialPass"));
        userToSave.setRole("USER");
        savedUser = userRepository.save(userToSave);

        registerUserDTO = new RegisterUserDTO(
                "New Registered User",
                "new.register@example.com",
                "registerPass123"
        );

        updatedUserDTO = new RegisterUserDTO(
                "Updated Name",
                "updated.email@example.com",
                "updatedPass"
        );
    }

    @Test
    @DisplayName("POST register a new user and persist it to DB")
    void registerNewUserAndPersist() {
        userService.registerUser(registerUserDTO);

        Optional<UserDB> foundUserOptional = userRepository.findByEmail(registerUserDTO.email().trim());
        assertTrue(foundUserOptional.isPresent(), "Registered user should be found in DB");
        UserDB foundUser = foundUserOptional.get();

        assertEquals(registerUserDTO.name(), foundUser.getName());
        assertEquals(registerUserDTO.email().trim(), foundUser.getEmail());
        assertTrue(passwordEncoder.matches(registerUserDTO.password(), foundUser.getEncodedPassword()), "Password should be encoded correctly");
        assertEquals("ROLE_USER", foundUser.getRole());
    }

    @Test
    @DisplayName("POST registering with existing email")
    void registeringExistingEmail() {
        RegisterUserDTO duplicateEmailDTO = new RegisterUserDTO(
                "Duplicate User",
                savedUser.getEmail(),
                "somePass"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(duplicateEmailDTO)
        );

        assertEquals("Email already in use: " + savedUser.getEmail(), exception.getMessage());
        assertEquals(1, userRepository.count(), "Only one user (the initial one) should be in DB");
    }

    @Test
    @DisplayName("GET user by email from DB")
    void userByEmailFromDB() {
        UserDetails userDetails = userService.loadUserByUsername(savedUser.getEmail());

        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(savedUser.getEmail(), userDetails.getUsername());
        assertTrue(passwordEncoder.matches("initialPass", userDetails.getPassword()), "Loaded password should match original");
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(savedUser.getRole())), "Role should match");
    }

    @Test
    @DisplayName("GET non-existent user")
    void nonExistentUser() {
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("nonexistent@example.com")
        );
    }

    @Test
    @DisplayName("UPDATE user profile in DB including password")
    void updateUserProfileIncludingPassword() {
        Long userIdToUpdate = savedUser.getId();
        UserDB userFromDb = userService.findById(userIdToUpdate);

        userService.updateUserProfile(userFromDb, updatedUserDTO);

        Optional<UserDB> updatedUserOptional = userRepository.findById(userIdToUpdate);
        assertTrue(updatedUserOptional.isPresent(), "Updated user should still exist");
        UserDB actualUpdatedUser = updatedUserOptional.get();

        assertEquals(updatedUserDTO.name(), actualUpdatedUser.getName());
        assertEquals(updatedUserDTO.email().trim(), actualUpdatedUser.getEmail());
        assertTrue(passwordEncoder.matches(updatedUserDTO.password(), actualUpdatedUser.getEncodedPassword()), "New password should be encoded and match");
        assertEquals("USER", actualUpdatedUser.getRole());
    }

    @Test
    @DisplayName("UPDATE user profile in DB excluding password")
    void updateUserProfileExcludingPassword() {
        Long userIdToUpdate = savedUser.getId();
        UserDB userFromDb = userService.findById(userIdToUpdate);
        String originalEncodedPassword = userFromDb.getEncodedPassword();

        RegisterUserDTO updatedUserDTOMinimal = new RegisterUserDTO(
                "Updated Name Only",
                "updated.minimal@example.com",
                ""
        );

        userService.updateUserProfile(userFromDb, updatedUserDTOMinimal);

        Optional<UserDB> updatedUserOptional = userRepository.findById(userIdToUpdate);
        assertTrue(updatedUserOptional.isPresent());
        UserDB actualUpdatedUser = updatedUserOptional.get();

        assertEquals(updatedUserDTOMinimal.name(), actualUpdatedUser.getName());
        assertEquals(updatedUserDTOMinimal.email().trim(), actualUpdatedUser.getEmail());
        assertEquals(originalEncodedPassword, actualUpdatedUser.getEncodedPassword(), "Password should NOT have changed");
    }

    @Test
    @DisplayName("GET user by ID from DB")
    void userById() {
        UserDB foundUser = userService.findById(savedUser.getId());

        assertNotNull(foundUser, "Found user should not be null");
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    @DisplayName("GET non-existent user by ID")
    void nonExistentUserById() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.findById(999L)
        );
    }
}
