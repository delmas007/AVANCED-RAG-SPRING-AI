package com.example.avancedrag.web.rest;

import com.example.avancedrag.Service.Dto.QueryDTO;
import com.example.avancedrag.Service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rag")
public class RagResource {

    private final RagService ragService;

    @PostMapping("/file")
    public ResponseEntity<Void> rag(@RequestParam("files") MultipartFile[] files) throws Exception {
        ragService.rag(files);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askLlm(@RequestBody QueryDTO queryDTO) {
        String response = ragService.askLlm(queryDTO.getQuery());
        return ResponseEntity.ok(response);

    }

}