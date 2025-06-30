package com.example.avancedrag.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    String upload(MultipartFile file) throws IOException;

}
