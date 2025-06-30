package com.example.avancedrag.Service.Imp;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageConverter {

    public static MultipartFile convertToMultipart(PDImageXObject image, int index) throws IOException {
        BufferedImage bufferedImage = image.getImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] bytes = baos.toByteArray();

        return new InMemoryMultipartFile(
                "image" + index,
                "image" + index + ".png",
                "image/png",
                bytes
        );
    }

    public static MultipartFile convertToMultipart(byte[] bytes, String extension, int index) {
        String contentType = switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream"; // fallback
        };

        return new InMemoryMultipartFile(
                "image" + index,
                "image" + index + "." + extension,
                contentType,
                bytes
        );
    }

}

