package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.userdto.LanguageRequest;
import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.enums.Language;
import com.group.collectionofrecipes.enums.UserRole;
import com.group.collectionofrecipes.exceptions.InvalidUserInfoException;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.mappers.UserMapper;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_USER_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.MAIN_USER_AVATAR_NAME;

import static com.group.collectionofrecipes.utils.ApiConstants.EMAIL_REGEX;
import static com.group.collectionofrecipes.utils.ApiConstants.UNAUTHORIZED_USER;
import static com.group.collectionofrecipes.utils.ApiConstants.USERNAME_REGEX;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final LocalFileStorageService storageService;

    public Optional<User> findByUsername(String username) {
        log.debug("Поиск пользователя по username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUserEmail(String email) {
        log.debug("Поиск пользователя по email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Загрузка пользователя для аутентификации: {}", username);
        User user = findByUsername(username).orElseThrow(() -> {
            log.error("Пользователь не найден: {}", username);
            return new UsernameNotFoundException(String.format("Пользователь '%s' не найден", username)
            );
        });

        if (!user.isEnabled()) {
            log.warn("Попытка входа неподтвержденного пользователя: {}", username);
            throw new UsernameNotFoundException("Email не подтвержден. Проверьте вашу почту.");
        }

        log.info("Пользователь успешно загружен: {}", username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public void verifyEmail(String token) {
        log.info("Запрос верификации email с токеном: {}", token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    log.error("Токен верификации не найден: {}", token);
                    return new RuntimeException("Неверная ссылка подтверждения");
                });

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        log.info("Email успешно подтвержден для пользователя: {}", user.getUsername());
    }

    @Transactional
    public UserDTO saveUser(RegistrationUserDTO registrationUserDTO, String langFromCookie) {
        log.info("Создание нового пользователя: {}", registrationUserDTO.getUsername());
        User user = userMapper.toUserEntity(registrationUserDTO);
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setPhoto(MAIN_USER_AVATAR_NAME);

        String verificationUrl = "https://recipes-api.poma.dev/api/v1/verify?token=" + token;
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("verificationUrl", verificationUrl);

        String templateName = "verification-email";
        String subject = "Подтверждение регистрации";

        log.info("Отправка verification email для пользователя: {}", user.getUsername());
        mailSenderService.sendHtmlEmail(user.getEmail(), subject, templateName, context);

        Language userLanguage = mapLanguage(langFromCookie);
        user.setLanguage(userLanguage);
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан с ID: {}", savedUser.getId());
        return userMapper.toUserDto(savedUser);
    }

    @Transactional
    public void createUserAvatar(MultipartFile image, Principal principal) {
        checkPrincipal(principal);

        String username = principal.getName();

        String lastFilename = userRepository.findPhotoByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден", username);
                    return new EntityNotFoundException(ERROR_USER_NOT_FOUND + username);
                });

        String userAvatarName = storageService.storeAvatarFile(image);

        if (!lastFilename.equals(MAIN_USER_AVATAR_NAME)) {
            storageService.deleteAvatarFile(lastFilename);
        }
        userRepository.updateAvatarByUsername(username, userAvatarName);
    }

    @Transactional
    public void deleteUserAvatar(Principal principal) {
        checkPrincipal(principal);
        String filename = userRepository.findPhotoByUsername(principal.getName()).orElseThrow(() -> {
            log.warn("Пользователь {} не найден при попытке удалить аватарку.", principal.getName());
            return new EntityNotFoundException(ERROR_USER_NOT_FOUND + principal.getName());
        });
        if (!filename.equals(MAIN_USER_AVATAR_NAME)) {
            storageService.deleteAvatarFile(filename);
        }
        String username = principal.getName();
        userRepository.updateAvatarByUsername(username, MAIN_USER_AVATAR_NAME);
    }

    public boolean isUsernameAvailable(String username) {
        boolean isValid = username.matches(USERNAME_REGEX);
        if (!isValid) {
            log.info("Логин не валиден");
            throw new InvalidUserInfoException("Логин не валиден");
        }
        return !userRepository.existsByUsernameIgnoreCase(username);
    }

    public boolean isEmailAvailable(String email) {
        boolean isValid = email.matches(EMAIL_REGEX);
        if (!isValid) {
            log.info("Почта не валидна");
            throw new InvalidUserInfoException("Почта не валидна");
        }
        return !userRepository.existsByEmailIgnoreCase(email);
    }

    public String getUserAvatar(Principal principal) {
        checkPrincipal(principal);
        return userRepository.findPhotoByUsername(principal.getName()).orElseThrow(() -> {
            log.warn("Пользователь {} не найден при попытке получить аватарку.", principal.getName());
            return new EntityNotFoundException(ERROR_USER_NOT_FOUND + principal.getName());
        });
    }

    public String getUserLanguage(Principal principal) {
        checkPrincipal(principal);
        return userRepository.findLanguageByUsername(principal.getName()).orElseThrow(() -> {
            log.warn("Пользователь {} не найден при попытке получить аватарку.", principal.getName());
            return new EntityNotFoundException(ERROR_USER_NOT_FOUND + principal.getName());
        });
    }

    @Transactional
    public void updateUserLanguage(Principal principal, @Valid LanguageRequest language) {
        checkPrincipal(principal);
        userRepository.updateLanguageByUsername(principal.getName(), language.getLanguage());
    }

    private void checkPrincipal(Principal principal) {
        if (principal == null) {
            log.error(UNAUTHORIZED_USER);
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }
    }

    private Language mapLanguage(String code) {
        return switch (code.toLowerCase()) {
            case "en" -> Language.ENGLISH;
            default -> Language.RUSSIAN;
        };
    }

    public void validateUserInfo(@Valid RegistrationUserDTO registrationUserDTO) {
        if (!registrationUserDTO.getPassword().equals(registrationUserDTO.getConfirmPassword())) {
            log.error("Пароли не совпадают для пользователя: {}", registrationUserDTO.getUsername());
            throw new InvalidUserInfoException("Пароли не совпадают");
        }

        if (!isUsernameAvailable(registrationUserDTO.getUsername())) {
            log.error("Пользователь с таким именем уже существует: {}", registrationUserDTO.getUsername());
            throw new InvalidUserInfoException("Пользователь с таким именем уже существует");
        }

        if (!isEmailAvailable(registrationUserDTO.getEmail())) {
            log.error("Пользователь с такой почтой уже существует: {}", registrationUserDTO.getEmail());
            throw new InvalidUserInfoException("Пользователь с такой почтой уже существует");
        }
    }
}