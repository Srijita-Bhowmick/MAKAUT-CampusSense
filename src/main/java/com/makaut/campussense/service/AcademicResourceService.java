package com.makaut.campussense.service;

import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.repository.CourseResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class AcademicResourceService {

    private final CourseResourceRepository courseResourceRepository;

    public Page<CourseResource> searchResources(
            StreamType stream,
            Integer semester,
            String subjectCode,
            ResourceType resourceType,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("year").descending().and(Sort.by("subjectCode").ascending()));
        return courseResourceRepository.searchResources(stream, semester, subjectCode, resourceType, pageable);
    }

    public List<CourseResource> getBySubjectCode(String subjectCode) {
        return courseResourceRepository.findBySubjectCodeIgnoreCase(subjectCode);
    }

    public List<CourseResource> getByStreamAndSemester(StreamType stream, Integer semester) {
        return courseResourceRepository.findByStreamAndSemester(stream, semester);
    }

    public CourseResource getById(Long id) {
        return courseResourceRepository.findById(id)
                .orElseThrow(() -> new com.makaut.campussense.exception.ResourceNotFoundException("Course resource not found with ID: " + id));
    }
}
