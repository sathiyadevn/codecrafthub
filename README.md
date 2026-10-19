# CodeCraftHub: Personalized Learning Platform (Backend API)

CodeCraftHub is a lightweight Spring Boot REST API that enables developers to track learning courses. It uses file-based JSON storage without an external database.

## Features
- Full CRUD operations over `/api/courses`
- File persistence using Jackson `ObjectMapper` (`courses.json`)
- CORS enabled for browser frontend integration
- Validation and error handling
- Bonus `/stats` and `/search` endpoints

## Prerequisites
- JDK 17 or higher
- Maven 3.8+
- curl or Postman

## Running the Application
```bash
./mvnw clean spring-boot:run