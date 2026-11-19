# Task Manager

A RESTful task management service built with Spring Boot, PostgreSQL, and Apache Kafka.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Data Model](#data-model)
- [Kafka Events](#kafka-events)
- [Configuration](#configuration)
- [Testing](#testing)
- [Performance Considerations](#performance-considerations)

## 🎯 Overview

This service provides a complete task management system with the following capabilities:
- Create and manage tasks
- Assign tasks to users
- Track task status changes
- Event-driven architecture using Kafka for task lifecycle events
- Pagination support for large datasets

**Design Goals:**
- Support up to 10,000 users
- Handle up to 100,000 tasks
- Event-driven notifications via Kafka
- Containerized deployment with Docker

## ✨ Features

- **Task Management**: Create, retrieve, and update tasks
- **User Assignment**: Assign users to tasks with automatic event publishing
- **Status Tracking**: Update task status through defined workflow states
- **Pagination**: Efficient data retrieval with configurable page sizes
- **Event Publishing**: Automatic Kafka events for task creation and assignment
- **Error Handling**: Comprehensive exception handling with meaningful error responses
- **Containerization**: Full Docker support with Docker Compose orchestration

## 🛠 Technology Stack

- **Java 21** - Programming language
- **Spring Boot 4.1.2** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Kafka** - Event streaming
- **PostgreSQL 16** - Relational database
- **Apache Kafka 7.7.1** - Message broker
- **MapStruct 1.6.3** - Object mapping
- **Gradle 9.1.0** - Build tool
- **Docker & Docker Compose** - Containerization

## 🏗 Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────┐
│         REST Controller             │
│      (TaskController.java)          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Service Layer               │
│       (TaskService.java)            │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼─────┐   ┌─────▼──────┐
│ Repository │   │   Kafka    │
│   (JPA)    │   │ Publisher  │
└──────┬─────┘   └─────┬──────┘
       │               │
┌──────▼─────┐   ┌─────▼──────┐
│ PostgreSQL │   │   Kafka    │
└────────────┘   └────────────┘
```

### Key Components

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and transaction management
- **Repositories**: Data access layer using Spring Data JPA
- **Mappers**: DTO-Entity conversion using MapStruct
- **Event Publishers**: Kafka event publishing for task lifecycle events
- **Exception Handlers**: Global exception handling for consistent error responses

## 📦 Prerequisites

- **Docker** and **Docker Compose** (recommended)
- OR:
  - Java 21 JDK
  - Gradle 9.1.0+
  - PostgreSQL 16+
  - Apache Kafka 7.7.1+

## 🚀 Getting Started

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd TaskManager
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL on port `5432`
   - Kafka on port `9092`
   - Task Manager application on port `8080`

3. **Verify services are running**
   ```bash
   docker-compose ps
   ```

4. **View application logs**
   ```bash
   docker-compose logs -f app
   ```

5. **Stop all services**
   ```bash
   docker-compose down
   ```

### Option 2: Local Development

1. **Start PostgreSQL**
   ```bash
   docker-compose up -d postgres
   ```

2. **Start Kafka**
   ```bash
   docker-compose up -d kafka
   ```

3. **Build the application**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will be available at `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Endpoints

#### 1. Get All Tasks (with pagination)
```http
GET /task?page=0&size=20
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20, min: 1, max: 100) - Page size

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Task name",
      "description": "Task description",
      "status": "OPENED",
      "assignee": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

#### 2. Get Task by ID
```http
GET /task/{id}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Task name",
  "description": "Task description",
  "status": "OPENED",
  "assignee": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**Error Response:** `404 Not Found`
```json
{
  "message": "Task not found with id: 1",
  "timestamp": "2026-11-19T08:40:00"
}
```

#### 3. Create Task
```http
POST /task
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "New task",
  "description": "Task description",
  "status": "OPENED"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "New task",
  "description": "Task description",
  "status": "OPENED",
  "assignee": null
}
```

**Note:** Creates a Kafka event of type `TASK_CREATED`

#### 4. Assign User to Task
```http
PATCH /task/{id}/assignee
Content-Type: application/json
```

**Request Body:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Task name",
  "description": "Task description",
  "status": "OPENED",
  "assignee": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**Note:** Creates a Kafka event of type `TASK_ASSIGNED`

#### 5. Change Task Status
```http
PATCH /task/{id}/status?status=IN_PROGRESS
```

**Query Parameters:**
- `status` (required) - New task status

**Available Statuses:**
- `OPENED`
- `IN_PROGRESS`
- `SOLVED`
- `CLOSED`
- `BACKLOG`

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Task name",
  "description": "Task description",
  "status": "IN_PROGRESS",
  "assignee": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### Error Responses

All error responses follow this format:
```json
{
  "message": "Error description",
  "timestamp": "2026-11-19T08:40:00"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## 💾 Data Model

### Task Entity
```java
{
  "id": Long,              // Primary key
  "name": String,          // Task name (required)
  "description": String,   // Task description
  "status": TaskStatus,    // Task status (enum)
  "assignee": User         // Assigned user (optional)
}
```

### User Entity
```java
{
  "id": Long,      // Primary key
  "name": String,  // User name (required)
  "email": String  // User email (required, unique)
}
```

### Task Status Enum
- `OPENED` - Task is created and open
- `IN_PROGRESS` - Task is being worked on
- `SOLVED` - Task is completed
- `CLOSED` - Task is closed
- `BACKLOG` - Task is in backlog

## 📨 Kafka Events

The application publishes events to the `task-events` topic for the following operations:

### Event Structure
```json
{
  "eventType": "TASK_CREATED | TASK_ASSIGNED",
  "taskId": 1,
  "taskName": "Task name",
  "taskStatus": "OPENED",
  "assigneeId": 1,
  "assigneeName": "John Doe",
  "assigneeEmail": "john@example.com",
  "timestamp": "2026-11-19T08:40:00"
}
```

### Event Types

1. **TASK_CREATED** - Published when a new task is created
   - Contains task details
   - `assigneeId`, `assigneeName`, `assigneeEmail` are null

2. **TASK_ASSIGNED** - Published when a user is assigned to a task
   - Contains task details and assignee information
   - All fields are populated

### Kafka Configuration

- **Topic:** `task-events`
- **Bootstrap Servers:** `localhost:9092` (configurable)
- **Serialization:** JSON
- **Consumer Group:** `taskmanager-group`

## ⚙️ Configuration

### Environment Variables

The application can be configured using the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/taskmanager` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `taskmanager` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `POSTGRES_DB` | PostgreSQL database name | `taskmanager` |
| `POSTGRES_USER` | PostgreSQL user | `taskmanager` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `postgres` |

### Application Configuration

Configuration is managed in [`application.yaml`](src/main/resources/application.yaml):

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/taskmanager}
    username: ${SPRING_DATASOURCE_USERNAME:taskmanager}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

app:
  kafka:
    task-events-topic: task-events
```

## 🧪 Testing

### Run Tests
```bash
./gradlew test
```

### Test Coverage
The project includes:
- Unit tests for service layer
- Integration tests for repository layer
- Kafka event publishing tests

## 📊 Performance Considerations

The application is designed to handle:
- **Up to 10,000 users**
- **Up to 100,000 tasks**

### Optimization Strategies

1. **Pagination**: All list endpoints support pagination to limit data transfer
2. **Database Indexing**: Primary keys and foreign keys are indexed
3. **Connection Pooling**: HikariCP for efficient database connections
4. **Lazy Loading**: JPA relationships use lazy loading by default
5. **Kafka Async Publishing**: Events are published asynchronously

### Recommended Production Settings

For production deployment, consider:
- Increase database connection pool size
- Configure Kafka partitions for parallel processing
- Enable database query caching
- Add monitoring and logging (e.g., Prometheus, Grafana)
- Implement rate limiting for API endpoints
- Add authentication and authorization

## 🐳 Docker

### Build Docker Image
```bash
docker build -t taskmanager:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Docker Compose Services
- **postgres**: PostgreSQL database with persistent volume
- **kafka**: Apache Kafka in KRaft mode (no Zookeeper required)
- **app**: Task Manager application

## 📝 Project Structure

```
TaskManager/
├── src/
│   ├── main/
│   │   ├── java/org/example/taskmanager/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── event/            # Kafka event models
│   │   │   ├── exception/        # Exception handling
│   │   │   ├── kafka/            # Kafka publishers
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   ├── model/            # Enums and models
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   └── TaskManagerApplication.java
│   │   └── resources/
│   │       └── application.yaml  # Application configuration
│   └── test/                     # Test files
├── gradle/                       # Gradle wrapper
├── build.gradle                  # Gradle build configuration
├── docker-compose.yml            # Docker Compose configuration
├── Dockerfile                    # Docker image definition
└── README.md                     # This file
```
