package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.enums.UserRole;
import com.group.collectionofrecipes.mappers.UserMapper;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
            return new UsernameNotFoundException(
                    String.format("Пользователь '%s' не найден", username)
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

    public UserDTO saveUser(RegistrationUserDTO registrationUserDTO) {
        log.info("Создание нового пользователя: {}", registrationUserDTO.getUsername());
        User user = userMapper.toUserEntity(registrationUserDTO);
        user.setPassword(passwordEncoder.encode(registrationUserDTO.getPassword()));
        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setPhoto(MAIN_USER_AVATAR_NAME);

        String verificationUrl = "http://localhost:8080/api/v1/verify?token=" + token;
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("verificationUrl", verificationUrl);

        String templateName = "verification-email";
        String subject = "Подтверждение регистрации";

        log.info("Отправка verification email для пользователя: {}", user.getUsername());
        mailSenderService.sendHtmlEmail(user.getEmail(), subject, templateName, context);

        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно созранен с ID: {}", savedUser.getId());
        return userMapper.toUserDto(savedUser);
    }

    @Transactional
    public void createUserAvatar(MultipartFile image, Principal principal) {
        if (principal == null) {
            log.error("Пользователь не зарегистрирован.");
            throw new IllegalArgumentException("Пользователь не зарегистрирован.");
        }

        String userAvatarName = storageService.storeAvatarFile(image);
        String username = principal.getName();

        userRepository.updateAvatarByUsername(username, userAvatarName);
    }

    @Transactional
    public void deleteUserAvatar(Principal principal) {
        if (principal == null) {
            log.error("Пользователь не зарегистрирован.");
            throw new IllegalArgumentException("Пользователь не зарегистрирован.");
        }
        String filename = userRepository.findPhotoByUsername(principal.getName()).orElseThrow(() -> {
            log.warn("Пользователь {} не найден при попытке удалить аватарку.", principal.getName());
            return new EntityNotFoundException(ERROR_USER_NOT_FOUND + principal.getName());
        });
        storageService.deleteAvatarFile(filename);
        String username = principal.getName();
        userRepository.updateAvatarByUsername(username, MAIN_USER_AVATAR_NAME);
    }

}