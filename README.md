<br />
<div align="center">
  <h3 align="center">User Service</h3>

  <p align="center">
    A robust Spring Boot microservice for managing users and their payment cards with integrated caching and automated CI/CD.
    <br />
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li><a href="#key-features">Key Features</a></li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#configuration">Configuration</a></li>
      </ul>
    </li>
    <li><a href="#api-endpoints">API Endpoints</a></li>
    <li><a href="#database--auditing">Database & Auditing</a></li>
    <li><a href="#architecture">Architecture</a></li>
    <li><a href="#testing">Testing</a></li>
    <li><a href="#ci-cd-pipeline">CI/CD Pipeline</a></li>
    <li><a href="#environment-variables">Environment Variables</a></li>
  </ol>
</details>

## About The Project

User Service is a comprehensive backend application built to manage user profiles and their associated payment cards. The system enforces strict business rules, such as a maximum limit of 5 cards per user, and ensures high performance through Redis caching and optimized database operations.

### Built With

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-green?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-✓-blue?style=for-the-badge&logo=docker)
![Liquibase](https://img.shields.io/badge/Liquibase-✓-blue?style=for-the-badge&logo=liquibase)

---

## Key Features

- **Complete CRUD Operations**: Full lifecycle management for users and payment cards
- **Business Rule Enforcement**: Maximum of 5 payment cards per user
- **Advanced Search & Filtering**: Pagination and filtering using JPA Specifications
- **Caching Layer**: Redis integration for performance optimization
- **Database Migration**: Automatic schema management with Liquibase
- **RESTful API**: Comprehensive API with Swagger documentation
- **Data Validation**: Input validation with custom error handling
- **Audit Trail**: Automatic tracking of created and updated timestamps
- **Comprehensive Testing**: Unit and integration tests with Testcontainers
- **CI/CD Pipeline**: Automated build, test, and deployment with GitHub Actions

---

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+


### Configuration

The application supports multiple profiles:
- **local**: For local development
- **docker**: For containerized deployment
- **test**: For running tests

---

## API Endpoints

### User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/users` | Create a new user |
| `GET` | `/api/v1/users` | Get all users (paginated) |
| `GET` | `/api/v1/users/{id}` | Get user by ID |
| `PUT` | `/api/v1/users/{id}` | Update user |
| `PATCH` | `/api/v1/users/{id}` | Change user status |
| `DELETE` | `/api/v1/users/{id}` | Delete user |
| `GET` | `/api/v1/users/{id}/cards` | Get user with payment cards |
| `POST` | `/api/v1/users/search` | Search users with criteria |
| `GET` | `/api/v1/users/active` | Get all active users |

### Payment Card Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/cards` | Create a new payment card |
| `GET` | `/api/v1/cards` | Get all cards (paginated) |
| `GET` | `/api/v1/cards/{id}` | Get card by ID |
| `PUT` | `/api/v1/cards/{id}` | Update card |
| `PATCH` | `/api/v1/cards/{id}` | Change card status |
| `DELETE` | `/api/v1/cards/{id}` | Delete card |
| `POST` | `/api/v1/cards/search` | Search cards with criteria |

### API Documentation
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8081/v3/api-docs`

---

## Database & Auditing

### Database Schema
```sql
users
├── id (PK)
├── name
├── surname
├── email (UNIQUE)
├── birth_date
├── active
├── created_at
├── updated_at
└── INDEX (name, surname)

payment_cards
├── id (PK)
├── user_id (FK → users.id)
├── number
├── holder
├── expiration_date
├── active
├── created_at
├── updated_at
└── INDEX (user_id)
```

### Key Constraints
- Each user can have maximum 5 payment cards
- Email addresses must be unique
- Foreign key constraint between payment_cards and users
- Automatic timestamp auditing on all entities

---

## Architecture

### Layers
1. **Controller Layer**: REST endpoints with DTOs
2. **Service Layer**: Business logic and caching
3. **Repository Layer**: Data access with JPA
4. **Model Layer**: JPA entities

### Caching Strategy
- User data cached in Redis with 2-hour TTL
- Cache eviction on user/card updates
- Two cache regions: `users` and `users-with-cards`

### Data Flow
```
Client → Controller (DTO) → Service → Repository (Entity) → Database
                                  ↓
                               Redis Cache
```

---

## Testing

### Test Types
- **Unit Tests**: Service layer methods
- **Integration Tests**: Full flow with Testcontainers
- **Repository Tests**: Database operations

### Running Tests
```bash
# All tests
./mvnw test

# Integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Unit tests only
./mvnw test -Dtest="*ServiceImplTest"
```

### Test Coverage
- PostgreSQL container for database testing
- Redis container for cache testing
- Complete API endpoint testing

---

## CI/CD Pipeline

### GitHub Actions Workflow
1. **Build**: Compile source code and resolve dependencies
2. **Test**: Execute all tests with Testcontainers
3. **Code Analysis**: SonarQube quality gate
4. **Docker Build**: Create Docker image
5. **Docker Image Publishing**: Build and push image to Docker Registry


### Pipeline Triggers
- Push to `tasks/*` branches
- Pull requests from `tasks/USR-0` to `main`
- Manual trigger

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `DB_NAME` | Database name | `user_service_db` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `SPRING_PROFILES_ACTIVE` | Active profiles | `local` |

Example `.env` file:
```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
DB_NAME=user_service_db
DB_HOST=localhost
DB_PORT=5432

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Application
SPRING_PROFILES_ACTIVE=local
```

