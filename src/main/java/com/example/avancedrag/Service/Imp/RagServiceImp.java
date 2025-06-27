package angaman.cedrick.rag_openai.Service.Imp;

import angaman.cedrick.rag_openai.Dto.UtilisateurDto;
import angaman.cedrick.rag_openai.Model.Utilisateur;
import angaman.cedrick.rag_openai.Repository.VectorRepository;
import angaman.cedrick.rag_openai.Service.RagService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class RagServiceImp implements RagService {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public RagServiceImp(VectorStore vectorStore, JdbcTemplate jdbcTemplate,VectorRepository vectorRepository) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.vectorRepository = vectorRepository;
    }

    VectorStore vectorStore;
    JdbcTemplate jdbcTemplate;
    VectorRepository vectorRepository;


    @Override
    public String askLlm(String query,UtilisateurDto utilisateur) {

        List<Document> allResults = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(30));

        String utilisateurId = utilisateur.getId();

        allResults = allResults.stream()
                .filter(doc -> {
                    Optional<String> vectorStoreUserIdOpt = vectorRepository.findUserIdByVectorStoreId(doc.getId());
                    return utilisateurId.equals(vectorStoreUserIdOpt.get());
                })
                .toList();




        String systemMessageTemplate = """
                Vous devez répondre à la question suivante en vous basant uniquement sur le CONTEXTE fourni ci-dessous. Ne fournissez aucune information qui n'est pas contenue dans ce contexte.

                Votre tâche est de :
                - Utiliser uniquement le CONTEXTE pour élaborer votre réponse.
                - Ne pas faire de suppositions ou ajouter des informations qui ne sont pas dans le CONTEXTE.
                - Si vous ne trouvez pas la réponse dans le CONTEXTE, indiquez que vous ne disposez pas des informations nécessaires.

                CONTEXTE:
                    {CONTEXTE}

                Notez que votre réponse doit être bien organiser respecte les retour a la ligne, précise, concise, et axée sur la question.\s
                """;

        Message systemMessage = new SystemPromptTemplate(systemMessageTemplate)
                .createMessage(Map.of("CONTEXTE",allResults));
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(List.of(systemMessage,userMessage));
        OpenAiApi aiApi = new OpenAiApi(apiKey);
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .withModel("gpt-4o")
                .withTemperature(0.0)
                .withMaxTokens(3000)
                .build();
        OpenAiChatModel openAiChatClient = new OpenAiChatModel(aiApi, openAiChatOptions);
        ChatResponse response = openAiChatClient.call(prompt);
        String responseContent = response.getResult().getOutput().getContent();
       return responseContent;

    }

    @Override
    public void textEmbeddingPdf(Resource[] pdfResources, UtilisateurDto utilisateur) {
        vectorRepository.supprimerParUtilisateurId(utilisateur.getId());
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
//        String content = "";
        List<Document> documentList = List.of();
        for(Resource resource : pdfResources){
            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource,config);
            documentList = pagePdfDocumentReader.get();
//            content += documentList.stream().map(d -> d.getContent()).collect(Collectors.joining("\n"))+"\n";
        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
//        List<Document> chunksDocs = chunks.stream().map(chunk -> new Document(chunk)).collect(Collectors.toList());
        vectorStore.accept(chunks);

//        for (Document doc : chunksDocs) {
//            vectorRepository.updateUserForVector(utilisateur.getId(), doc.getId());
//        }
        for (Document doc : chunks) {
            vectorRepository.updateUserForVector(utilisateur.getId(), doc.getId());
        }


    }

    @Override
    public void textEmbeddingWord(Resource[] worldResources,UtilisateurDto utilisateur) {
       vectorRepository.supprimerParUtilisateurId(utilisateur.getId());
        String content = "";
        List<Document> documentList = List.of();
        for(Resource resource : worldResources){
            try (InputStream inputStream = resource.getInputStream()) {
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                documentList = List.of(new Document(extractor.getText()));
                content += extractor.getText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
//        List<Document> chunksDocs = chunks.stream().map(chunk -> new Document(chunk)).collect(Collectors.toList());
        vectorStore.accept(chunks);

        for (Document doc : chunks) {
            vectorRepository.updateUserForVector(utilisateur.getId(), doc.getId());
        }
    }



    @Override
    public void textEmbeddingExcel(Resource[] excelResources,UtilisateurDto utilisateur) {
        vectorRepository.supprimerParUtilisateurId(utilisateur.getId());
        String content = "";
        List<Document> documentList = List.of();
        for (Resource resource : excelResources) {
            try (InputStream inputStream = resource.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                int numberOfSheets = workbook.getNumberOfSheets();

                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    Iterator<Row> rowIterator = sheet.iterator();

                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        StringBuilder rowContent = new StringBuilder();

                        Iterator<Cell> cellIterator = row.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            rowContent.append(cell.toString()).append(" ");
                        }
                        documentList.add(new Document(rowContent.toString().trim()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        for(Resource resource : excelResources){
//            try (InputStream inputStream = resource.getInputStream()) {
//                Workbook workbook = WorkbookFactory.create(inputStream);
//                int numberOfSheets = workbook.getNumberOfSheets();
//                for (int i = 0; i < numberOfSheets; i++) {
//                    Sheet sheet = workbook.getSheetAt(i);
//                    Iterator<Row> rowIterator = sheet.iterator();
//                    while (((Iterator<?>) rowIterator).hasNext()) {
//                        Row row = rowIterator.next();
//                        Iterator<Cell> cellIterator = row.cellIterator();
//                        while (cellIterator.hasNext()) {
//                            Cell cell = cellIterator.next();
//                            content += cell.toString() + " ";
//                        }
//                        content += "\n";
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
//        List<Document> chunksDocs = chunks.stream().map(Document::new).collect(Collectors.toList());
        vectorStore.accept(chunks);
        for (Document doc : chunks) {
            vectorRepository.updateUserForVector(utilisateur.getId(), doc.getId());
        }
    }



    @Override
    public void textEmbeddingPowerpoint(Resource[] PowerpointResources,UtilisateurDto utilisateur) {
        vectorRepository.supprimerParUtilisateurId(utilisateur.getId());
        String content = "";
        List<Document> documentList = List.of();
        for (Resource resource : PowerpointResources) {
            try (InputStream inputStream = resource.getInputStream()) {
                XMLSlideShow ppt = new XMLSlideShow(inputStream);

                for (XSLFSlide slide : ppt.getSlides()) {
                    StringBuilder slideContent = new StringBuilder();

                    for (XSLFShape shape : slide.getShapes()) {
                        if (shape instanceof XSLFTextShape) {
                            XSLFTextShape textShape = (XSLFTextShape) shape;
                            for (XSLFTextParagraph paragraph : textShape) {
                                slideContent.append(paragraph.getText()).append("\n");
                            }
                        }
                    }
                    if (slideContent.length() > 0) {
                        documentList.add(new Document(slideContent.toString().trim()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        for (Resource resource : PowerpointResources) {
//            try (InputStream inputStream = resource.getInputStream()) {
//                XMLSlideShow ppt = new XMLSlideShow(inputStream);
//                for (XSLFSlide slide : ppt.getSlides()) {
//                    for (XSLFShape shape : slide.getShapes()) {
//                        if (shape instanceof XSLFTextShape) {
//                            XSLFTextShape textShape = (XSLFTextShape) shape;
//                            for (XSLFTextParagraph paragraph : textShape) {
//                                content += paragraph.getText() + "\n";
//                            }
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                // Gérer l'erreur d'une manière appropriée
//                e.printStackTrace();
//            }
//        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
//        List<Document> chunksDocs = chunks.stream().map(Document::new).collect(Collectors.toList());
        vectorStore.accept(chunks);
        for (Document doc : chunks) {
            vectorRepository.updateUserForVector(utilisateur.getId(), doc.getId());
        }
    }

    @Override
    public String Image(String prompt) {
        OpenAiImageApi api = new OpenAiImageApi(apiKey);
        OpenAiImageModel openaiImageClient = new OpenAiImageModel(api);

        ImageResponse response = openaiImageClient.call(
                new ImagePrompt(prompt,
                        OpenAiImageOptions.builder()
                                .withQuality("hd")
                                .withN(1)
                                .withHeight(1024)
                                .withWidth(1024).build())

        );
        return response.getResult().getOutput().getUrl();
    }

    public void textEmbeddingTxt(Resource[] txtResources) {
        jdbcTemplate.update("delete from vector_store");
        StringBuilder content = new StringBuilder();

        for (Resource resource : txtResources) {
            try (InputStream inputStream = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

