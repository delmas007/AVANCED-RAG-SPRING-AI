package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.RagService;
import com.example.avancedrag.Service.memory.PostgresChatMemoryStore;
import com.example.avancedrag.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class RagServiceImp implements RagService {
    private final PostgresChatMemoryStore chatMemoryRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private final VectorStore vectorStore;



    @Override
    public String askLlm(String query) {

        String systemMessage =
                """
                    Vous êtes un expert chargé de répondre à des questions en vous appuyant uniquement sur le CONTEXTE fourni.
                   \s
                    Consignes :
                    - Basez votre réponse uniquement sur les informations présentes dans le CONTEXTE.
                    - N’inventez aucune information. Si la réponse n’est pas dans le CONTEXTE, indiquez-le clairement.
                    - Structurez votre réponse de manière claire, précise et concise.
                    - Lorsque des images sont mentionnées avec des URLs, présentez-les sous forme de liste comme suit :
                       - URL_IMAGE(URL 1)=>
                       - URL_IMAGE(URL 2)=>
                   \s
                    Style attendu :
                    - Le ton doit être fluide, naturel, professionnel, sans formules comme “dans le contexte fourni”.
                    - La réponse doit être directement axée sur la question posée, sans ajout inutile.
                    - Respectez les sauts de ligne pour une bonne lisibilité.
                    - La réponse doit toujours être rédigée en français.
               \s""";


        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(openAiApiKey)
                        .build())
                .build();


        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.73)
                .topK(5)
                .filterExpression(new FilterExpressionBuilder()
                        .eq("genre", "fairytale")
                        .build())
                .build();
        List<Document> documents = retriever.retrieve(new Query("What is the main character of the story?"));

        ChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        var chatClient = ChatClient.builder(openAiChatModel);

        chatClient.defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(memory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().build())
                                .build()
                ).build();

        Query requete = new Query(query);

        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient)
                .build();

        Query transformedQuery = queryTransformer.transform(requete);


        String filter = String.format("id == '%s'", SecurityUtils.getCurrentUsername());

        return chatClient.build().prompt()
                .system(systemMessage)
                .user(transformedQuery.text())
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, Objects.requireNonNull(SecurityUtils.getCurrentUsername()))
                        .param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filter)
                )
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

