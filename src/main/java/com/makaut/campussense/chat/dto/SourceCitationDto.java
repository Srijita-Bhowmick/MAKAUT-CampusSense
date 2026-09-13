package com.makaut.campussense.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Source citation metadata for grounded answer verification")
public class SourceCitationDto {

    @Schema(description = "Subject or document title", example = "Operating Systems")
    private String subjectName;

    @Schema(description = "Official MAKAUT paper code", example = "PCC-CS501")
    private String subjectCode;

    @Schema(description = "Resource type", example = "SYLLABUS")
    private String resourceType;

    @Schema(description = "Syllabus module or exam section cited", example = "Module 3: Inter-process Communication")
    private String moduleOrSection;

    @Schema(description = "Examination or syllabus year", example = "2023")
    private Integer year;

    @Schema(description = "Cosine similarity score (0.0 to 1.0)", example = "0.884")
    private Float similarity;

    @Schema(description = "Direct URL to source document", example = "https://makautexam.net/syllabus/PCC-CS501.pdf")
    private String fileUrl;
}
