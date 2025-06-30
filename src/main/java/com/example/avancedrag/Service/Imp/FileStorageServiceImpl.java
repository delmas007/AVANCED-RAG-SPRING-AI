package com.example.avancedrag.Service.Imp;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.avancedrag.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file) throws IOException {

        Map params = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        return (String) uploadResult.get("url");
    }
}
