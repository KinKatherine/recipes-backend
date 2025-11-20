package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Modifying
    @Query("UPDATE User u SET u.photo = :avatarName WHERE u.username = :username")
    void updateAvatarByUsername(String username, String avatarName);

    @Query("SELECT u.photo FROM User u WHERE u.username = :username")
    Optional<String> findPhotoByUsername(String username);
}
