package com.example.avancedrag.Repository;

import com.example.avancedrag.Model.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VectorRepository extends JpaRepository<VectorStore, String> {

    @Transactional(readOnly = true)
    @Query("SELECT v.id FROM VectorStore v")
    List<String> findAllVectorIds();

    @Transactional
    @Query("SELECT v.id, v.embedding FROM VectorStore v ")
    Optional<Document> findVectorStoreById();

}
