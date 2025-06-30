package com.example.avancedrag.Service.Imp;

import com.example.avancedrag.Service.FileStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
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

    public static void vectorizeDocuments(List<Document> documentList) {
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> chunks = tokenTextSplitter.split(documentList);
        vectorStore.accept(chunks);
    }

    public static void loadDataIntoVectorStore(Resource[] resources) throws Exception {
        for (Resource resource : resources) {
            String filename = Objects.requireNonNull(resource.getFilename()).toLowerCase();

            if (filename.endsWith(".pdf")) {
                processPdf(resource);
            } else if (filename.endsWith(".docx")) {
                processDocx(resource);
            } else if (filename.endsWith(".pptx")) {
                processPptx(resource);
            }else if (filename.endsWith(".xlsx")) {
                processExcel(resource);
            } else {
                System.out.println("Type de fichier non supporté : " + filename);
            }
        }
    }


    public static void processPdf(Resource resource) throws IOException {
//        for (Resource resource : Resources) {
            PDDocument document = PDDocument.load(resource.getInputStream());
            PDPageTree pdPages = document.getPages();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            int index = 0;
            int page = 0;

            for (PDPage pdPage : pdPages) {
                ++page;
                pdfTextStripper.setStartPage(page);
                pdfTextStripper.setEndPage(page);
                PDResources resources = pdPage.getResources();
                List<Document> pageDocuments = new ArrayList<>();

                String textContent = pdfTextStripper.getText(document);
                Map<String, Object> metadataText = new HashMap<>();
                metadataText.put("Page", page);
                Document textDoc = new Document(textContent, metadataText);
                pageDocuments.add(textDoc);

                for (var c : resources.getXObjectNames()) {
                    PDXObject pdxObject = resources.getXObject(c);
                    if (pdxObject instanceof PDImageXObject image) {
                        ++index;
                        MultipartFile file = ImageConverter.convertToMultipart(image, index);
                        String urlImage = fileStorageService.upload(file);

                        String userMessage = "Donnez-moi une description detaillée de l'image fournie";
                        String content = chatClient
                                .prompt()
                                .user(u -> u.text(userMessage).media(MimeTypeUtils.IMAGE_PNG, file.getResource()))
                                .call()
                                .content();
                        String imageContent =  "URL IMAGE : " + urlImage + "\n" + "Desciption of the image :\n" + content;
                        Map<String, Object> metadataImage = new HashMap<>();
                        metadataImage.put("Page", page);
                        metadataImage.put("media", urlImage);

                        Document imageDoc = new Document(imageContent, metadataImage);
                        pageDocuments.add(imageDoc);
                    }
                }

                vectorizeDocuments(pageDocuments);
            }
//        }
    }

    public static void processDocx(Resource resource) throws Exception {
        List<Document> documents = new ArrayList<>();

        try (XWPFDocument doc = new XWPFDocument(resource.getInputStream())) {
            StringBuilder textContent = new StringBuilder();

            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                textContent.append(paragraph.getText()).append("\n");
            }

            Map<String, Object> metadata = Map.of("fileName", resource.getFilename());
            documents.add(new Document(textContent.toString(), metadata));

            int index = 0;
            for (XWPFPictureData pictureData : doc.getAllPictures()) {
                ++index;
                byte[] imageBytes = pictureData.getData();
                String extension = pictureData.suggestFileExtension();
                MultipartFile imageFile = ImageConverter.convertToMultipart(imageBytes, extension, index);

                String urlImage = fileStorageService.upload(imageFile);
                String prompt = "Donnez-moi une description détaillée de l'image fournie";
                String content = chatClient
                        .prompt()
                        .user(u -> u.text(prompt).media(MimeTypeUtils.IMAGE_PNG, imageFile.getResource()))
                        .call()
                        .content();
                String imageContent =  "URL IMAGE : " + urlImage + "\n" + "Desciption of the image :\n" + content;
                Map<String, Object> meta = new HashMap<>();
                meta.put("fileName", resource.getFilename());
                meta.put("media", urlImage);
                documents.add(new Document(imageContent, meta));
            }
        }

        vectorizeDocuments(documents);
    }


    public static void processPptx(Resource resource) throws Exception {
        List<Document> documents = new ArrayList<>();
        try (XMLSlideShow ppt = new XMLSlideShow(resource.getInputStream())) {
            int index = 0;
            int slideNumber = 0;

            for (XSLFSlide slide : ppt.getSlides()) {
                ++slideNumber;
                StringBuilder slideText = new StringBuilder();

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        slideText.append(textShape.getText()).append("\n");
                    }
                }

                Map<String, Object> metaText = Map.of(
                        "slide", slideNumber,
                        "fileName", resource.getFilename()
                );
                documents.add(new Document(slideText.toString(), metaText));

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFPictureShape picture) {
                        ++index;
                        byte[] imageBytes = picture.getPictureData().getData();
                        String extension = picture.getPictureData().suggestFileExtension();
                        MultipartFile imageFile = ImageConverter.convertToMultipart(imageBytes, extension, index);

                        String urlImage = fileStorageService.upload(imageFile);
                        String prompt = "Donnez-moi une description détaillée de l'image fournie";
                        String content = chatClient
                                .prompt()
                                .user(u -> u.text(prompt).media(MimeTypeUtils.IMAGE_PNG, imageFile.getResource()))
                                .call()
                                .content();
                        String imageContent =  "URL IMAGE : " + urlImage + "\n" + "Desciption of the image :\n" + content;
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("slide", slideNumber);
                        meta.put("media", urlImage);
                        meta.put("fileName", resource.getFilename());
                        documents.add(new Document(imageContent, meta));
                    }
                }
            }
        }

        vectorizeDocuments(documents);
    }


    public static void processExcel(Resource resource) throws Exception {
        List<Document> documents = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(resource.getInputStream())) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                StringBuilder sheetText = new StringBuilder();
                sheetText.append("Feuille : ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String cellValue = switch (cell.getCellType()) {
                            case STRING -> cell.getStringCellValue();
                            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
                            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> cell.getCellFormula();
                            case BLANK, ERROR, _NONE -> "";
                        };
                        sheetText.append(cellValue).append(" | ");
                    }
                    sheetText.append("\n");
                }

                Map<String, Object> metadataText = Map.of(
                        "sheetName", sheet.getSheetName(),
                        "fileName", resource.getFilename(),
                        "sheetIndex", sheetIndex
                );
                documents.add(new Document(sheetText.toString(), metadataText));

                int index = 0;
                XSSFDrawing drawing = sheet.getDrawingPatriarch();
                if (drawing != null) {
                    for (XSSFShape shape : drawing.getShapes()) {
                        if (shape instanceof XSSFPicture picture) {
                            ++index;
                            XSSFPictureData pictureData = picture.getPictureData();
                            byte[] imageBytes = pictureData.getData();
                            String extension = pictureData.suggestFileExtension();
                            MultipartFile imageFile = ImageConverter.convertToMultipart(imageBytes, extension, index);

                            String urlImage = fileStorageService.upload(imageFile);
                            String prompt = "Donnez-moi une description détaillée de l'image fournie";
                            String content = chatClient
                                    .prompt()
                                    .user(u -> u.text(prompt).media(MimeTypeUtils.IMAGE_PNG, imageFile.getResource()))
                                    .call()
                                    .content();
                            String imageContent =  "URL IMAGE : " + urlImage + "\n" + "Desciption of the image :\n" + content;
                            Map<String, Object> imageMeta = new HashMap<>();
                            imageMeta.put("sheetName", sheet.getSheetName());
                            imageMeta.put("fileName", resource.getFilename());
                            imageMeta.put("media", urlImage);
                            imageMeta.put("sheetIndex", sheetIndex);

                            assert content != null;
                            documents.add(new Document(imageContent, imageMeta));
                        }
                    }
                }
            }
        }

        vectorizeDocuments(documents);
    }







}
