package com.example.avancedrag.Repository;

import com.example.avancedrag.Model.Utilisateur;
import com.example.avancedrag.Model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Integer> {

    @Query("SELECT v FROM Validation v WHERE v.code = :code")
    Optional<Validation> findByCode(@Param("code") String code);

    Optional<Validation> findByUtilisateur(Utilisateur utilisateur);

    void deleteAllByExpirationBefore(Instant now);

    void deleteById(int id);
}
