package com.makaut.campussense.aggregation.parsers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfBoxParserTest {

    private PdfBoxParser pdfBoxParser;

    @BeforeEach
    void setUp() {
        pdfBoxParser = new PdfBoxParser();
    }

    @Test
    @DisplayName("Should extract text and detect syllabus module using PDFBox 3.0.3 Loader.loadPDF")
    void testPdfBoxExtraction() throws IOException {
        // Create a test PDF in memory using PDFBox 3.0.3
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Course Code: PCC-CS501");
                cs.newLineAtOffset(0, -20);
                cs.showText("Module 1: Introduction to Operating Systems and Architecture.");
                cs.newLineAtOffset(0, -20);
                cs.showText("Credits: 3");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        PdfBoxParser.ParsedPdfResult result = pdfBoxParser.parsePdf(pdfBytes);

        assertNotNull(result);
        assertEquals(1, result.getPageCount());
        assertTrue(result.getRawText().contains("PCC-CS501"));
        assertEquals("PCC-CS501", result.getDetectedSubjectCode());
        assertEquals("3", result.getDetectedCredits());
        assertFalse(result.getExtractedModules().isEmpty());
        assertTrue(result.getExtractedModules().get(0).contains("Module 1"));
    }
}
