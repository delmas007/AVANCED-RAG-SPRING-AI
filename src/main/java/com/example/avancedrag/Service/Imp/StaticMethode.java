package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Service
public class StaticMethode {

    public StaticMethode(VectorStore vectorStore, FileStorageService fileStorageService,
                         ChatClient.Builder chatClient) {
        StaticMethode.vectorStore = vectorStore;
        StaticMethode.fileStorageService = fileStorageService;
        StaticMethode.chatClient = chatClient.build();
    }

    private static VectorStore vectorStore;
    private static  FileStorageService fileStorageService;
    private static ChatClient chatClient;

    public static String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "";
    }

    public static String getFileExtension(Resource resource) {
        String filename = resource.getFilename();
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "";
    }

    public static List<Document> powerPointExtract(Resource[] PowerpointResources) {
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
        return documentList;

    }

    public static List<Document> pdfExtract(Resource[] pdfResources) {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
        List<Document> documentList = List.of();
        for(Resource resource : pdfResources){
            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource,config);
            documentList = pagePdfDocumentReader.get();
        }
        return documentList;
    }

    public static List<Document> excelExtract(Resource[] excelResources) {
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
        return documentList;
    }
    public static List<Document> docExtract(Resource[] worldResources) {
        List<Document> documentList = List.of();
        for(Resource resource : worldResources){
            try (InputStream inputStream = resource.getInputStream()) {
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                documentList = List.of(new Document(extractor.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documentList;
    }

    public static void vectorizeDocuments(List<Document> documentList) {
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
        vectorStore.accept(chunks);
    }

    public static void vectorizeDocument(Document documentList) {
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
        vectorStore.accept(chunks);
    }

    public int getPdfPageCount(Resource resource) throws IOException {
        try (PDDocument document = PDDocument.load(resource.getInputStream())) {
            return document.getNumberOfPages();
        }
    }

    public int estimateDocxPageCount(Resource resource) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(resource.getInputStream())) {
            int wordCount = doc.getParagraphs().stream()
                    .mapToInt(p -> p.getText().split("\\s+").length)
                    .sum();
            return Math.max(1, wordCount / 300);
        }
    }

    public int getPptxSlideCount(Resource resource) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(resource.getInputStream())) {
            return ppt.getSlides().size();
        }
    }

    public static void loadDataIntoVectorStore(Resource[] Resources) throws IOException, IOException {

        for (Resource resource : Resources) {
            PDDocument document = PDDocument.load(resource.getInputStream());
            PDPageTree pdPages = document.getPages();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            int index = 0;
            int page = 0;
            List<Document> imagesDocuments = new ArrayList<>();
            for (PDPage pdPage : pdPages) {
                ++page;
                pdfTextStripper.setStartPage(page);
                pdfTextStripper.setEndPage(page);
                PDResources resources = pdPage.getResources();
                List<String> media = new ArrayList<>();
                String textContent = pdfTextStripper.getText(document);
                List<Document> documentList = new ArrayList<>();
                for (var c : resources.getXObjectNames()) {
                    PDXObject pdxObject = resources.getXObject(c);
                    if (pdxObject instanceof PDImageXObject image) {
                        ++index;
                        MultipartFile file = ImageConverter.convertToMultipart(image, index);
                        String urlImage = fileStorageService.upload(file);
                        String userMessage ="Donnez-moi une description detailler de l'image fournie";
                        String content = chatClient
                                .prompt()
                                .user(u -> u.text(userMessage).media(MimeTypeUtils.IMAGE_PNG, file.getResource()))
                                .call()
                                .content();
                        System.out.println(content);
                        textContent = textContent + "\n" + "IMAGE : " + urlImage + "\n" + "Desciption of the image :\n" + content;
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("Page", page);
                        metadata.put("media", urlImage);
                        Document doc = new Document(textContent, metadata);
                        imagesDocuments.add(doc);
                    }
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("Page", page);
                    metadata.put("media", media);
                    Document pageDoc = new Document(textContent, metadata);
                    vectorizeDocument(pageDoc);
                }
            }

        }
    }
}
