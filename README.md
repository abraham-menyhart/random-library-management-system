# Library Management System

RESTful web service for managing library books and borrowers built with Spring Boot 3.x and Java 21.

## Quick Start

1. **Start database:**
   ```bash
   docker-compose up -d
   ```

2. **Run application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Test API:**
   ```bash
   curl http://localhost:8080/api/books
   ```

## API Documentation

- **OpenAPI Specification**: `openapi.yml`
- **HTTP Examples**: `api-examples.http` (IntelliJ IDEA)
- **Health Check**: http://localhost:8080/actuator/health

## Development

- **Java 21** + **Spring Boot 3.2.1**
- **PostgreSQL** database (Docker)
- **Maven** build tool
- **45 tests** with **61% coverage**

Run tests: `mvn test`