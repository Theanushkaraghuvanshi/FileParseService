File Parser CRUD API with Progress Tracking
===========================================

A Spring Boot application that supports uploading, storing, parsing, and retrieving files with real-time progress tracking.

Features
--------

-   File Upload API- Upload files with progress tracking

-   Real-time Progress Monitoring- Track upload and processing status

-   File Parsing- CSV file parsing with structured content storage

-   CRUD Operations - Complete file management (Create, Read, Update, Delete)

-   Error Handling - Comprehensive error handling with meaningful messages

-   Database Storage - H2 in-memory database for development

Technology Stack
----------------

-   Backend Framework: Spring Boot 3.x

-   Database: H2 Database (in-memory)

-   Build Tool: Maven

-   File Parsing: OpenCSV

-   Testing: JUnit 5, Mockito

Prerequisites
-------------

-   Java 17 or higher

-   Maven 3.6 or higher

-   Git

Setup Instructions
------------------

### 1\. Clone and Setup

bash

# Clone the repository
git clone <repository-url>
cd file-parser-api

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

### 2\. Verify Application is Running

The application will start on `http://localhost:8080`

Check if it's working:

bash

curl http://localhost:8080/api/files

### 3\. Access Database Console

H2 Database Console is available at: `http://localhost:8080/h2-console`

Login Credentials:

-   JDBC URL: `jdbc:h2:mem:testdb`

-   Username: `sa`

-   Password: (leave empty)

API Documentation
-----------------

### 1\. Upload a File

Endpoint: `POST /api/files`

Content-Type: `multipart/form-data`

Request:

bash

curl -X POST -F "file=@test.csv" http://localhost:8080/api/files

Response:

json

{
"file_id": "10b65b39-b2a2-4bcc-ad8f-6bde164251a7",
"message": "File uploaded successfully"
}

### 2\. Check Upload/Processing Progress

Endpoint: `GET /api/files/{file_id}/progress`

Request:

bash

curl http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7/progress

Response Examples:

During processing:

json

{
"file_id": "10b65b39-b2a2-4bcc-ad8f-6bde164251a7",
"status": "processing",
"progress": 42
}

After completion:

json

{
"file_id": "10b65b39-b2a2-4bcc-ad8f-6bde164251a7",
"status": "ready",
"progress": 100
}

### 3\. Get File Content

Endpoint: `GET /api/files/{file_id}`

Request:

bash

curl http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7

Response Examples:

If processing is not complete:

json

{
"message": "File upload or processing in progress. Please try again later."
}

If processing is complete:

json

{
"file_id": "10b65b39-b2a2-4bcc-ad8f-6bde164251a7",
"file_name": "test.csv",
"file_size": 1024,
"status": "ready",
"created_at": "2023-12-07T10:30:45.123456",
"parsed_content": [
{
"name": "John Doe",
"email": "john@example.com",
"age": "30"
},
{
"name": "Jane Smith",
"email": "jane@example.com",
"age": "25"
}
]
}

### 4\. List All Files

Endpoint: `GET /api/files`

Request:

bash

curl http://localhost:8080/api/files

Response:

json

[
{
"id": "10b65b39-b2a2-4bcc-ad8f-6bde164251a7",
"fileName": "test.csv",
"filePath": "uploads/10b65b39-b2a2-4bcc-ad8f-6bde164251a7_test.csv",
"fileSize": 1024,
"status": "READY",
"progress": 100,
"createdAt": "2023-12-07T10:30:45.123456",
"updatedAt": "2023-12-07T10:31:15.654321"
}
]

### 5\. Delete a File

Endpoint: `DELETE /api/files/{file_id}`

Request:

bash

curl -X DELETE http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7

Response:

json

{
"message": "File deleted successfully"
}

Error Responses
---------------

File Not Found:

json

{
"message": "File not found"
}

Unsupported File Type:

json

{
"message": "Only CSV files are supported for parsing."
}

Internal Server Error:

json

{
"message": "Could not upload the file: [error details]"
}

Postman Collection
------------------

### Import Instructions

1.  Download the Postman collection from the `/postman` folder

2.  Open Postman

3.  Click Import → Upload Files → Select the collection file

4.  All endpoints will be imported with sample requests

### Manual Setup

Create a new collection in Postman with these requests:

1.  Upload File

    -   Method: POST

    -   URL: `{{baseUrl}}/api/files`

    -   Body: form-data

        -   Key: `file`, Type: File, Value: [select your file]

2.  Check Progress

    -   Method: GET

    -   URL: `{{baseUrl}}/api/files/{{fileId}}/progress`

3.  Get File Content

    -   Method: GET

    -   URL: `{{baseUrl}}/api/files/{{fileId}}`

4.  List Files

    -   Method: GET

    -   URL: `{{baseUrl}}/api/files`

5.  Delete File

    -   Method: DELETE

    -   URL: `{{baseUrl}}/api/files/{{fileId}}`

Environment Variables:

-   `baseUrl`: `http://localhost:8080`

-   `fileId`: [retrieved from upload response]

Sample Test Files
-----------------

Create a sample CSV file (`test.csv`) for testing:

csv

name,email,age,department
John Doe,john@example.com,30,Engineering
Jane Smith,jane@example.com,25,Marketing
Bob Johnson,bob@example.com,35,Sales

Testing
-------

### Run Unit Tests

bash

mvn test

### Test with CURL

bash

# Upload a file
curl -X POST -F "file=@test.csv" http://localhost:8080/api/files

# Check progress (replace with actual file ID)
curl http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7/progress

# Get file content
curl http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7

# List all files
curl http://localhost:8080/api/files

# Delete a file
curl -X DELETE http://localhost:8080/api/files/10b65b39-b2a2-4bcc-ad8f-6bde164251a7

Configuration
-------------

The application uses the following configuration in `application.properties`:

properties

# Server configuration
server.port=8080

# H2 Database
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# File upload settings
spring.servlet.multipart.max-file-size=10GB
spring.servlet.multipart.max-request-size=10GB