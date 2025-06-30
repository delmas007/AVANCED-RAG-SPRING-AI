package com.example.avancedrag.Service.memory;

import com.example.avancedrag.Model.ChatMessages;
import com.example.avancedrag.Model.Role_Chat;
import com.example.avancedrag.Repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Primary
@Transactional
public class PostgresChatMemoryStore implements ChatMemoryRepository {

    private final ChatMessageRepository repository;

    @Override
    public List<String> findConversationIds() {
        return repository.findDistinctUserIds().stream()
                .toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        String userIdStr = conversationId.toString();
        return repository.findByUserIdOrderByTimestamp(userIdStr).stream()
                .map(e -> {
                    Role_Chat role = Role_Chat.valueOf(e.getRole());
                    String content = e.getContent();
                    if (content == null || content.trim().isEmpty()) {
                        return null;
                    }
                    AbstractMessage message = switch (role) {
                        case USER -> new UserMessage(content);
                        case ASSISTANT -> new AssistantMessage(content);
                        case SYSTEM -> new SystemMessage(content);
                    };
                    return (Message) message;
                })
                .filter(Objects::nonNull)
                .toList();
    }


    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String userIdStr = conversationId.toString();
        deleteByConversationId(userIdStr);
        for (Message message : messages) {
            ChatMessages entity = new ChatMessages();
            entity.setUserId(userIdStr);
            entity.setRole(message.getMessageType().name());
            String content = null;
            if (message instanceof UserMessage userMessage) {
                content = userMessage.getText();
            } else if (message instanceof AssistantMessage assistantMessage) {
                content = assistantMessage.getText();
            }
            else if (message instanceof SystemMessage systemMessage) {
                content = systemMessage.getText();
            }
            else if (message instanceof ToolResponseMessage toolExecutionResultMessage) {
                content = toolExecutionResultMessage.getText();
            }
            entity.setContent(content);
            entity.setTimestamp(Instant.now());
            repository.save(entity);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        repository.deleteAllByUserId(conversationId);
    }
}
