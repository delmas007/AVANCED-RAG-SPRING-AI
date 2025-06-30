package com.example.avancedrag.Repository;

import com.example.avancedrag.Model.ChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {

    List<ChatMessages> findByUserIdOrderByTimestamp(String userId);

    void deleteAllByUserId(String userId);

    @Query("SELECT DISTINCT c.userId FROM ChatMessages c")
    List<String> findDistinctUserIds();
}
