package com.evaruiz.healthcarer.unitary;

import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDB existingUser;
    private RegisterUserDTO registerUserDTO;
    private RegisterUserDTO updatedUserDTO;

    @BeforeEach
    void setUp() {
        existingUser = new UserDB(1L, "Existing User", "existing@example.com",
                "encodedExistingPassword", "USER",
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        registerUserDTO = new RegisterUserDTO(
                "New User",
                "newuser@example.com",
                "rawPassword123"
        );

        updatedUserDTO = new RegisterUserDTO(
                "Updated Name",
                "updated@example.com",
                "newRawPassword"
        );
    }

    @Test
    @DisplayName("POST register a new user successfully")
    void registerNewUser() {
        String normalizedEmail = registerUserDTO.email().trim();

        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.password())).thenReturn("encodedRawPassword123");
        when(userRepository.save(any(UserDB.class))).thenReturn(existingUser);

        userService.registerUser(registerUserDTO);

        verify(userRepository, times(1)).findByEmail(normalizedEmail);
        verify(passwordEncoder, times(1)).encode(registerUserDTO.password());

        ArgumentCaptor<UserDB> userCaptor = ArgumentCaptor.forClass(UserDB.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        UserDB savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(registerUserDTO.name(), savedUser.getName());
        assertEquals(normalizedEmail, savedUser.getEmail());
        assertEquals("encodedRawPassword123", savedUser.getEncodedPassword());
        assertEquals("ROLE_USER", savedUser.getRole());

        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("POST registering a user with existing email")
    void registeringWithExistingEmail() {
        String normalizedEmail = registerUserDTO.email().trim(); // Use the new user DTO email
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser(registerUserDTO)
        );

        assertEquals("Email already in use: " + normalizedEmail, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(normalizedEmail);
        verify(userRepository, never()).save(any(UserDB.class));
        verify(passwordEncoder, never()).encode(anyString());
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("GET a user by email")
    void loadUserByEmail() {
        String email = existingUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertInstanceOf(LoggedUser.class, userDetails);
        assertEquals(existingUser.getEmail(), userDetails.getUsername());
        assertEquals(existingUser.getEncodedPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(existingUser.getRole())));

        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("GET UsernameNotFoundException when user not found by email")
    void userNotFoundByEmail() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername(nonExistentEmail)
        );

        assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("GET true if user found by email")
    void userFoundByEmail() {
        String email = existingUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        boolean found = userService.findByEmail(email);

        assertTrue(found);
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("GET false if user not found by email")
    void userNotFoundByEmail2() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        boolean found = userService.findByEmail(nonExistentEmail);

        assertFalse(found);
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("UPDATE user profile including password")
    void updateUserProfileWithPassword() {

        String newNormalizedEmail = updatedUserDTO.email().trim();

        when(passwordEncoder.encode(updatedUserDTO.password())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(UserDB.class))).thenReturn(existingUser);

        userService.updateUserProfile(existingUser, updatedUserDTO);

        verify(passwordEncoder, times(1)).encode(updatedUserDTO.password());
        verify(userRepository, times(1)).save(existingUser);

        assertEquals(updatedUserDTO.name(), existingUser.getName());
        assertEquals(newNormalizedEmail, existingUser.getEmail());
        assertEquals("encodedNewPassword", existingUser.getEncodedPassword());
        assertEquals("USER", existingUser.getRole());

        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("UPDATE user profile without changing password")
    void updateUserProfileWithoutPassword() {
        RegisterUserDTO updatedUserDTOMinimal = new RegisterUserDTO(
                "Updated Name Only",
                "updated.minimal@example.com",
                ""
        );
        String originalEncodedPassword = existingUser.getEncodedPassword();


        when(userRepository.save(any(UserDB.class))).thenReturn(existingUser);

        userService.updateUserProfile(existingUser, updatedUserDTOMinimal);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(existingUser);

        assertEquals(updatedUserDTOMinimal.name(), existingUser.getName());
        assertEquals(updatedUserDTOMinimal.email().trim(), existingUser.getEmail());
        assertEquals(originalEncodedPassword, existingUser.getEncodedPassword());

        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("GET user by ID")
    void userById() {
        Long userId = existingUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserDB foundUser = userService.findById(userId);

        assertNotNull(foundUser);
        assertEquals(existingUser, foundUser);
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("GET user not found by ID")
    void userNotFoundById() {
        Long nonExistentId = 99L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.findById(nonExistentId)
        );

        assertEquals("User not found with ID: " + nonExistentId, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }
}
