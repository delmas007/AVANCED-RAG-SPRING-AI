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
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class RagServiceImp implements RagService {
    private final PostgresChatMemoryStore chatMemoryRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.deepseek.api-key}")
    private String deepAiApiKey;

    @Value("${spring.ai.deepseek.chat.options.model}")
    private String deepModel;

    @Value("${spring.ai.huggingface.chat.api-key}")
    private String huggingfaceApiKey;

    @Value("${spring.ai.huggingface.chat.url}")
    private String huggingfaceBaseUrl;

    @Value("${spring.ai.groq.api-key}")
    private String groqApiKey;

    @Value("${spring.ai.groq.model-name}")
    private String groqModel;

    @Value("${spring.ai.groq.base-url}")
    private String groqApiUrl;

    private final ManageDoc manageDoc;
    private final VectorStore vectorStore;

    @Value("classpath:prompts/systemMessageChat.st")
    private Resource systemMessage;

    @Override
    public String askLlm(String query) {

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(openAiApiKey)
                        .build())
                .build();

        OpenAiChatModel grocChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(groqApiUrl)
                        .apiKey(groqApiKey)
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(groqModel)
                        .temperature(0.0)
                        .build())
                .build();

        DeepSeekChatModel deepSeekChatModel = DeepSeekChatModel.builder()
                .deepSeekApi(DeepSeekApi.builder()
                        .apiKey(deepAiApiKey).build())
                .defaultOptions(DeepSeekChatOptions.builder()
                        .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER.getValue())
                        .build()).build();

        HuggingfaceChatModel huggingfaceChatModel = new HuggingfaceChatModel(huggingfaceApiKey,huggingfaceBaseUrl);

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

        String result = transformedQuery.text().replaceFirst("^Rewritten query:\\s*", "").trim();

        String filter = String.format("id == '%s'", SecurityUtils.getCurrentUsername());

        return chatClient.build().prompt()
                .system(systemMessage)
                .user(result)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, Objects.requireNonNull(SecurityUtils.getCurrentUsername()))
                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filter)
                )
                .call()
                .content();
    }

    @Override
    public void rag(MultipartFile[] files) throws Exception {
        Resource[] pdfResources = Arrays.stream(files)
                .map(MultipartFile::getResource)
                .toArray(Resource[]::new);
        manageDoc.loadDataIntoVectorStore(pdfResources);
    }


}

