package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.enums.UserRole;
import com.group.collectionofrecipes.mappers.UserMapper;
import com.group.collectionofrecipes.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.context.Context;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MailSenderService mailSenderService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "rawPassword";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
    }

    @Test
    void findByUsername_UserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByUsername(TEST_USERNAME);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(TEST_USERNAME, foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
    }

    @Test
    void findByUsername_UserNotFound_ShouldReturnEmptyOptional() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.findByUsername("nonExistent");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    void findByUserEmail_EmailExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> foundUser = userService.findByUserEmail(TEST_EMAIL);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(TEST_EMAIL, foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_UserExistsAndEnabled_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(TEST_USERNAME);

        // Assert
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals(ENCODED_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.USER.name())));
    }

    @Test
    void loadUserByUsername_UserNotFound_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert (Негативный случай)
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown"),
                "Должно быть выброшено UsernameNotFoundException, если пользователь не найден.");
    }

    @Test
    void loadUserByUsername_UserExistsButDisabled_ShouldThrowUsernameNotFoundException() {
        // Arrange (Пограничный случай)
        testUser.setEnabled(false);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // Act & Assert (Негативный случай)
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(TEST_USERNAME));

        assertEquals("Email не подтвержден. Проверьте вашу почту.", exception.getMessage());
    }

    @Test
    void verifyEmail_ValidToken_ShouldEnableUser() {
        // Arrange
        String validToken = "valid-uuid-token";
        testUser.setEnabled(false);
        testUser.setVerificationToken(validToken);

        when(userRepository.findByVerificationToken(validToken)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.verifyEmail(validToken);

        // Assert
        assertTrue(testUser.isEnabled());
        assertNull(testUser.getVerificationToken());
        verify(userRepository, times(1)).findByVerificationToken(validToken);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void verifyEmail_InvalidToken_ShouldThrowRuntimeException() {
        // Arrange (Негативный случай)
        String invalidToken = "invalid-token";
        when(userRepository.findByVerificationToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.verifyEmail(invalidToken),
                "Должно быть выброшено RuntimeException при неверном токене.");

        assertEquals("Неверная ссылка подтверждения", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveUser_ValidData_ShouldSaveUserAndSendVerificationEmail() {
        // Arrange
        RegistrationUserDTO registrationDTO = new RegistrationUserDTO(
                TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD, TEST_EMAIL
        );

        User newUser = new User();
        newUser.setUsername(TEST_USERNAME);
        newUser.setEmail(TEST_EMAIL);

        UserDTO expectedUserDTO = new UserDTO();
        expectedUserDTO.setUsername(TEST_USERNAME);

        when(userMapper.toUserEntity(registrationDTO)).thenReturn(newUser);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedUserDTO);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L);
            return userToSave;
        });

        // Act
        UserDTO resultDTO = userService.saveUser(registrationDTO, "ru");

        // Assert
        assertNotNull(resultDTO);
        assertEquals(TEST_USERNAME, resultDTO.getUsername());

        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);

        verify(userRepository, times(1)).save(argThat(user ->
                ENCODED_PASSWORD.equals(user.getPassword()) &&
                        !user.isEnabled() &&
                        user.getRole().equals(UserRole.USER) &&
                        user.getVerificationToken() != null &&
                        user.getCreatedAt() != null
        ));

        verify(mailSenderService, times(1)).sendHtmlEmail(
                eq(TEST_EMAIL), // To: email
                eq("Подтверждение регистрации"),
                eq("verification-email"),
                any(Context.class)
        );
    }
}