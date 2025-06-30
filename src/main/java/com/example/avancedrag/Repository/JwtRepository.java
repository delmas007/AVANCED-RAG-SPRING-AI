package com.example.avancedrag.Repository;

import com.example.avancedrag.Model.Jwt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtRepository extends JpaRepository<Jwt, Integer> {

    Optional<Jwt> findByValueAndDesactiveAndExpire(String value, boolean desactive, boolean expire);
}
