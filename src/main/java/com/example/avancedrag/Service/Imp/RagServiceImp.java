package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.RagService;
import com.example.avancedrag.Service.memory.PostgresChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class RagServiceImp implements RagService {
    private final PostgresChatMemoryStore chatMemoryRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private final VectorStore vectorStore;



    @Override
    public String askLlm(String query,String userId) {

        String systemMessage = """
                Vous devez répondre à la question suivante en vous basant uniquement sur le CONTEXTE. Ne fournissez aucune information qui n'est pas contenue dans ce contexte.

                Votre tâche est de :
                - Utiliser uniquement le CONTEXTE pour élaborer votre réponse.
                - Ne pas faire de suppositions ou ajouter des informations qui ne sont pas dans le CONTEXTE.
                - Si vous ne trouvez pas la réponse dans le CONTEXTE, indiquez que vous ne disposez pas des informations nécessaires.
                - Si votre réponse contient l' url d'accès à la source des images dans le contexte, utilisez ce format pour présenter les images dans une liste d'éléments, comme:
                   - URL_IMAGE(URL 1)=>
                   - URL_IMAGE(URL 2)=>
               \s
                Notez que votre réponse doit être bien organiser respecte les retour a la ligne, précise, concise, et axée sur la question.\s
               \s""";

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(openAiApiKey)
                        .build())
                .build();

        ChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        var chatClient = ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().build())
                                .build()
                ).build();

        return chatClient.prompt()
                .system(systemMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, userId))
                .user(query)
                .call()
                .content();
    }

    @Override
    public void rag(MultipartFile[] files) throws Exception {
        Resource[] pdfResources = Arrays.stream(files)
                .map(MultipartFile::getResource)
                .toArray(Resource[]::new);
        StaticMethode.loadDataIntoVectorStore(pdfResources);

    }


}

