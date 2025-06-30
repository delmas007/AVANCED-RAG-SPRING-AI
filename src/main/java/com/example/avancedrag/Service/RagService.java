package com.example.avancedrag.Service;

import com.example.avancedrag.Service.Dto.UtilisateurDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface RagService {

    String askLlm(String query, String userId);

    void rag(MultipartFile[] files) throws Exception;

}
