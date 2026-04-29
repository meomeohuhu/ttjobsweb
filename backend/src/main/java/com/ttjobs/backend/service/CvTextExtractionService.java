package com.ttjobs.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class CvTextExtractionService {

    public String extractText(byte[] data, String contentType, String filename) {
        if (data == null || data.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file is empty");
        }
        String type = contentType == null ? "" : contentType.toLowerCase();
        if (type.contains("pdf") || hasExtension(filename, ".pdf")) {
            return extractPdf(data);
        }
        if (type.contains("word") || hasExtension(filename, ".docx")) {
            return extractDocx(data);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported CV file type");
    }

    private String extractPdf(byte[] data) {
        try (InputStream input = new ByteArrayInputStream(data);
             PDDocument document = PDDocument.load(input)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to read PDF");
        }
    }

    private String extractDocx(byte[] data) {
        try (InputStream input = new ByteArrayInputStream(data);
             XWPFDocument document = new XWPFDocument(input)) {
            return document.getParagraphs().stream()
                    .map(p -> p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to read DOCX");
        }
    }

    private boolean hasExtension(String filename, String ext) {
        if (filename == null) {
            return false;
        }
        return filename.toLowerCase().endsWith(ext);
    }
}
