package com.example.avancedrag.web.rest;

import com.example.avancedrag.Service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    @PostMapping("/file")
    public ResponseEntity<Void> rag(@RequestParam("files") MultipartFile[] files) throws IOException {
        ragService.rag(files);
        return ResponseEntity.ok().build();
    }

}