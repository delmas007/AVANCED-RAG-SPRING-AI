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
        ImageIO.write(bufferedImage, "png", baos); // ou "jpg"
        byte[] bytes = baos.toByteArray();

        return new InMemoryMultipartFile(
                "image" + index,
                "image" + index + ".png",
                "image/png",
                bytes
        );
    }
}

