# SyllabusAI

A Spring Boot application for parsing and managing educational syllabi from PDF files. The system extracts structured information including topics, materials, and deadlines from uploaded syllabus documents.

## 🚀 Features

- **PDF Syllabus Parsing**: Upload and extract structured data from PDF syllabus files
- **Topic Management**: Organize course topics with metadata (week, difficulty level, description)
- **Material Tracking**: Manage learning materials with titles, types, and links
- **Deadline Management**: Track assignments, exams, and other important dates
- **RESTful API**: Comprehensive API for all syllabus operations
- **Database Integration**: JPA-based persistence with PostgreSQL

## 🏗️ System Architecture

### Core Entities

- **User**: System users who upload and manage syllabi
- **Syllabus**: Main document containing course information
- **Topic**: Individual course topics with week and difficulty metadata
- **Material**: Learning resources (books, articles, videos)
- **Deadline**: Important dates and assignments

### Database Schema

```
users
├── id (PK)
├── first_name
├── last_name
├── email
├── password

syllabi
├── id (PK)
├── user_id (FK → users)
├── filename
├── upload_date
├── status

topics
├── id (PK)
├── syllabus_id (FK → syllabi)
├── title
├── description
├── week
├── difficulty_level

materials
├── id (PK)
├── syllabus_id (FK → syllabi)
├── title
├── type
├── link

deadlines
├── id (PK)
├── syllabus_id (FK → syllabi)
├── title
├── date
├── type
```

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.x, Java 21
- **Database**: PostgreSQL with JPA/Hibernate
- **PDF Processing**: Apache PDFBox
- **Build Tool**: Maven
- **API Documentation**: Spring REST

## 📦 Project Structure

```
com.syllabusai/
├── controller/          # REST API endpoints
├── service/            # Business logic layer
├── repository/         # Data access layer
├── model/              # JPA entities
├── dto/               # Data transfer objects
├── mapper/            # Entity-DTO converters
├── config/            # Configuration classes
└── builder/           # Builder patterns
```

## 🔧 Installation & Setup

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL database

### Configuration

1. Clone the repository:
```bash
git clone <repository-url>
cd syllabusai
```

2. Configure database connection in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/syllabusai
spring.datasource.username=your-username
spring.datasource.password=your-password
```

3. Build the application:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## 📚 API Endpoints

### Syllabus Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/syllabus/upload` | Upload and parse PDF syllabus |
| GET | `/api/syllabus/{id}` | Get syllabus details |
| GET | `/api/syllabus/{id}/topics` | Get syllabus topics |
| GET | `/api/syllabus/{id}/materials` | Get syllabus materials |
| GET | `/api/syllabus/{id}/deadlines` | Get syllabus deadlines |
| DELETE | `/api/syllabus/{id}` | Delete syllabus |

### Example Usage

**Upload Syllabus:**
```bash
curl -X POST -F "file=@syllabus.pdf" http://localhost:8080/api/syllabus/upload
```

**Get Syllabus Topics:**
```bash
curl http://localhost:8080/api/syllabus/1/topics
```

## 🔄 Data Flow

1. **Upload**: User uploads PDF syllabus file
2. **Parsing**: System extracts text using PDFBox
3. **Processing**: Text is analyzed and structured into entities
4. **Storage**: Entities are saved to database with relationships
5. **API Response**: Structured data returned to client

## 🎯 Key Components

### PDF Processing
- **PDFBoxConfig**: Configuration for PDF text extraction
- **PDFConfigManager**: Singleton manager for PDF processing settings

### Data Mapping
- **SyllabusMapper**: Converts Syllabus entities to DTOs
- **TopicMapper**: Handles Topic entity-DTO conversion
- **MaterialMapper**: Manages Material data transformation
- **DeadlineMapper**: Converts Deadline entities

### Business Logic
- **SyllabusService**: Core syllabus processing operations
- **ProgressTracker**: Tracks syllabus processing progress
- **SyllabusBuilder**: Builder pattern for Syllabus creation

## 🗃️ Database Configuration

The application uses JPA with the following key configurations:
- Automatic table generation from entities
- Cascade operations for related entities
- Proper indexing and foreign key relationships
- Timestamp tracking for upload dates

## 🚦 Status Codes

- `200 OK`: Successful operation
- `201 Created`: Resource created successfully
- `204 No Content`: Successful deletion
- `400 Bad Request`: Invalid input
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server-side error

## 🔮 Future Enhancements

- Integration with learning management systems
- Advanced search and filtering capabilities
- Export functionality for structured data


## 📄 License

This project is licensed under the MIT License.

---

*Built with Spring Boot and ❤️ for educational purposes*

