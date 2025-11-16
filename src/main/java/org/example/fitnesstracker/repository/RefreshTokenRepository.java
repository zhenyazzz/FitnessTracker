package org.example.fitnesstracker.repository;

import java.util.Optional;

import org.example.fitnesstracker.model.RefreshToken;
import org.example.fitnesstracker.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    void deleteByUser(User user);

}
