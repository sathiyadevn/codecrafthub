package com.dev.codecrafthub.service;

import com.dev.codecrafthub.model.Course;
import com.dev.codecrafthub.model.CourseStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final String FILE_PATH = "courses.json";
    private final ObjectMapper objectMapper;
    private final File dataFile;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CourseService() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.dataFile = new File(FILE_PATH);
        initFile();
    }

    private synchronized void initFile() {
        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                objectMapper.writeValue(dataFile, new ArrayList<Course>());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize JSON storage file: " + e.getMessage(), e);
        }
    }

    private synchronized List<Course> readCoursesFromFile() {
        try {
            if (!dataFile.exists() || dataFile.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dataFile, new TypeReference<List<Course>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private synchronized void writeCoursesToFile(List<Course> courses) {
        try {
            objectMapper.writeValue(dataFile, courses);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to JSON storage file: " + e.getMessage(), e);
        }
    }

    public List<Course> getAllCourses() {
        return readCoursesFromFile();
    }

    public Optional<Course> getCourseById(Long id) {
        return readCoursesFromFile().stream()
                .filter(course -> Objects.equals(course.getId(), id))
                .findFirst();
    }

    public Course createCourse(Course course) {
        List<Course> courses = readCoursesFromFile();
        long nextId = courses.stream()
                .mapToLong(c -> c.getId() != null ? c.getId() : 0L)
                .max()
                .orElse(0L) + 1L;

        course.setId(nextId);
        course.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        courses.add(course);
        writeCoursesToFile(courses);
        return course;
    }

    public Optional<Course> updateCourse(Long id, Course updatedData) {
        List<Course> courses = readCoursesFromFile();
        for (int i = 0; i < courses.size(); i++) {
            Course current = courses.get(i);
            if (Objects.equals(current.getId(), id)) {
                current.setName(updatedData.getName());
                current.setDescription(updatedData.getDescription());
                current.setTargetDate(updatedData.getTargetDate());
                current.setStatus(updatedData.getStatus());
                writeCoursesToFile(courses);
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    public boolean deleteCourse(Long id) {
        List<Course> courses = readCoursesFromFile();
        boolean removed = courses.removeIf(course -> Objects.equals(course.getId(), id));
        if (removed) {
            writeCoursesToFile(courses);
        }
        return removed;
    }

    public List<Course> searchCourses(String query) {
        if (query == null || query.isBlank()) {
            return getAllCourses();
        }
        String q = query.toLowerCase();
        return readCoursesFromFile().stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(q)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStats() {
        List<Course> courses = readCoursesFromFile();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_courses", courses.size());
        stats.put("not_started", courses.stream().filter(c -> c.getStatus() == CourseStatus.NOT_STARTED).count());
        stats.put("in_progress", courses.stream().filter(c -> c.getStatus() == CourseStatus.IN_PROGRESS).count());
        stats.put("completed", courses.stream().filter(c -> c.getStatus() == CourseStatus.COMPLETED).count());
        return stats;
    }
}