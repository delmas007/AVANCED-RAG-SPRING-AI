package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.Dto.UtilisateurDto;
import com.example.avancedrag.Service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
public class RagServiceImp implements RagService {
//    @Value("${spring.ai.openai.api-key}")
//    private String apiKey;


    @Override
    public String askLlm(String query) {
        return "";
    }

    @Override
    public void rag(MultipartFile[] files) throws IOException {
        Resource[] pdfResources = Arrays.stream(files)
                .map(MultipartFile::getResource)
                .toArray(Resource[]::new);
//
//        String fileExtension = StaticMethode.getFileExtension(pdfResources[0]);
//        List<Document> documentList;
//        switch (fileExtension) {
//            case "pdf" -> documentList = StaticMethode.pdfExtract(pdfResources);
//            case "docx" -> documentList = StaticMethode.docExtract(pdfResources);
//            case "pptx" -> documentList = StaticMethode.powerPointExtract(pdfResources);
//            default -> throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
//        }
//
//        StaticMethode.vectorizeDocuments(documentList);

        StaticMethode.loadDataIntoVectorStore(pdfResources);


    }


}

