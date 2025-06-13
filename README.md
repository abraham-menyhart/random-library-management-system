# Library Management System

A comprehensive RESTful web service for managing library books and borrowers, built with Spring Boot 3.x and Java 21.

## Features

- **Book Management**: Add books, view all books, borrow books
- **Borrower Management**: Create borrowers, view borrower details, track borrowed books
- **Data Persistence**: PostgreSQL database with JPA/Hibernate
- **REST API**: Full RESTful endpoints with proper HTTP status codes
- **Error Handling**: Global exception handling with structured error responses
- **Observability**: OpenTelemetry tracing, Prometheus metrics, health checks
- **Testing**: Comprehensive test suite with 80%+ coverage

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **PostgreSQL** (production)
- **H2 Database** (testing)
- **Lombok** (boilerplate reduction)
- **OpenTelemetry** (tracing)
- **Micrometer** (metrics)
- **Maven** (build tool)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ (for production)
- IntelliJ IDEA (recommended IDE)

## Getting Started

### 1. Database Setup

Start PostgreSQL database using Docker:

```bash
docker-compose up -d
```

This will start PostgreSQL on port 5432 with:
- Database: `library_db`
- Username: `library_user`
- Password: `library_pass`

### 2. Running from IntelliJ IDEA

#### Option A: Using Run Configuration
1. Open the project in IntelliJ IDEA
2. Navigate to `src/main/java/com/library/LibraryManagementApplication.java`
3. Right-click on the class and select "Run 'LibraryManagementApplication'"
4. The application will start on `http://localhost:8080`

#### Option B: Using Maven Tool Window
1. Open the Maven tool window (View → Tool Windows → Maven)
2. Navigate to `library-management-system → Plugins → spring-boot`
3. Double-click on `spring-boot:run`

#### Option C: Using Terminal in IntelliJ
1. Open Terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run: `mvn spring-boot:run`

### 3. Running from Command Line

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Or build and run the JAR
mvn clean package
java -jar target/library-management-system-0.0.1-SNAPSHOT.jar
```

### 4. Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## API Endpoints

### Books
- `GET /api/books` - Get all books
- `POST /api/books` - Add a new book
- `POST /api/books/{bookId}/borrow/{borrowerId}` - Borrow a book

### Borrowers
- `GET /api/borrowers` - Get all borrowers
- `POST /api/borrowers` - Create a new borrower
- `GET /api/borrowers/{id}` - Get borrower by ID
- `GET /api/borrowers/{id}/books` - Get books borrowed by borrower

### Health & Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/health/liveness` - Liveness probe
- `GET /actuator/health/readiness` - Readiness probe

## Configuration

### Application Profiles

- **default**: Uses PostgreSQL database
- **test**: Uses H2 in-memory database for testing

### Key Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library_db
    username: library_user
    password: library_pass

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  tracing:
    sampling:
      probability: 1.0
```

## API Usage Examples

### Create a Borrower
```bash
curl -X POST http://localhost:8080/api/borrowers \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'
```

### Add a Book
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title": "1984", "author": "George Orwell", "isbn": "978-0451524935"}'
```

### Borrow a Book
```bash
curl -X POST http://localhost:8080/api/books/1/borrow/1
```

## Development

### Project Structure
```
src/
├── main/java/com/library/
│   ├── controller/     # REST controllers
│   ├── service/        # Business logic
│   ├── repository/     # Data access layer
│   ├── entity/         # JPA entities
│   ├── dto/            # Data transfer objects
│   ├── mapper/         # Entity-DTO mappers
│   ├── exception/      # Custom exceptions
│   └── config/         # Configuration classes
└── test/               # Test classes
```

### Code Quality
- Follow clean code principles
- Use Lombok for boilerplate reduction
- Comprehensive unit and integration tests
- Global exception handling
- Proper logging with SLF4J

### Metrics & Monitoring
- Custom business metrics (books borrowed, borrowers created)
- HTTP request metrics with percentiles
- Database health checks
- OpenTelemetry distributed tracing

## Testing

The project includes comprehensive tests:
- **Unit Tests**: Service and repository layer tests
- **Integration Tests**: Full application context tests
- **Web Tests**: Controller layer tests with MockMvc
- **Total**: 44 tests with high coverage

Run tests in IntelliJ:
1. Right-click on `src/test/java` folder
2. Select "Run 'All Tests'"

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running: `docker-compose ps`
- Check database logs: `docker-compose logs postgres`

### Port Already in Use
- Change port in `application.yml`: `server.port: 8081`
- Or kill process using port 8080: `lsof -ti:8080 | xargs kill -9`

### Build Issues
- Clean Maven cache: `mvn clean`
- Reload Maven project in IntelliJ: Maven tool window → Reload
- Ensure Java 21 is configured: File → Project Structure → Project SDK