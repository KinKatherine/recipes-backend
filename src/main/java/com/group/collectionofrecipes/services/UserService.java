package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.enums.UserRole;
import com.group.collectionofrecipes.mappers.UserMapper;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUserEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("Пользователь '%s' не найден", username)
        ));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }


    public UserDTO saveUser(RegistrationUserDTO registrationUserDTO) {
        User user = userMapper.toUserEntity(registrationUserDTO);
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userMapper.toUserDto(userRepository.save(user));
    }
}
