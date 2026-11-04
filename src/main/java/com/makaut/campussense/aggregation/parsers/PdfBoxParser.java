package com.makaut.campussense.aggregation.parsers;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PdfBoxParser {

    private static final Pattern MODULE_PATTERN = Pattern.compile(
            "(Module\\s+\\d+[^:\\n]*:[^\\n]*(?:\\n(?!Module\\s+\\d+)[^\\n]*)*)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile(
            "(?:Course\\s+Code|Paper\\s+Code|Subject\\s+Code)[:\\s]+([A-Z]{2,4}-[A-Z]{2,4}\\d{3}[A-Z]?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "(?:Credits?|Contact\\s+Hours?)[:\\s]+([\\d\\w:.]+)",
            Pattern.CASE_INSENSITIVE
    );

    @Getter
    @Builder
    public static class ParsedPdfResult {
        private final String rawText;
        private final int pageCount;
        private final String detectedSubjectCode;
        private final String detectedCredits;
        private final List<String> extractedModules;
    }

    /**
     * Extracts text and structured metadata using Apache PDFBox 3.0.3 Loader.loadPDF.
     */
    public ParsedPdfResult parsePdf(byte[] pdfBytes) throws IOException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF byte array cannot be empty or null");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return extractDocumentContent(document);
        }
    }

    public ParsedPdfResult parsePdf(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        try (InputStream is = inputStream) {
            byte[] bytes = is.readAllBytes();
            return parsePdf(bytes);
        }
    }

    public ParsedPdfResult parsePdf(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist and not be null");
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            return extractDocumentContent(document);
        }
    }

    private ParsedPdfResult extractDocumentContent(PDDocument document) throws IOException {
        int pageCount = document.getNumberOfPages();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String rawText = stripper.getText(document);

        // Detect Subject Code
        String detectedSubjectCode = null;
        Matcher codeMatcher = COURSE_CODE_PATTERN.matcher(rawText);
        if (codeMatcher.find()) {
            detectedSubjectCode = codeMatcher.group(1).trim();
        }

        // Detect Credits
        String detectedCredits = null;
        Matcher creditMatcher = CREDIT_PATTERN.matcher(rawText);
        if (creditMatcher.find()) {
            detectedCredits = creditMatcher.group(1).trim();
        }

        // Extract Syllabus Modules
        List<String> modules = new ArrayList<>();
        Matcher moduleMatcher = MODULE_PATTERN.matcher(rawText);
        while (moduleMatcher.find()) {
            modules.add(moduleMatcher.group(1).trim());
        }

        log.info("Parsed PDF successfully. Pages: {}, Modules extracted: {}, Subject Code: {}",
                pageCount, modules.size(), detectedSubjectCode);

        return ParsedPdfResult.builder()
                .rawText(rawText)
                .pageCount(pageCount)
                .detectedSubjectCode(detectedSubjectCode)
                .detectedCredits(detectedCredits)
                .extractedModules(modules)
                .build();
    }
}
