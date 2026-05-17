# user-service

User Service is a Spring Boot based microservice responsible for handling authentication, 
authorization, user management, OTP verification, and profile-related operations for the ecommerce application.

____________________________________________________________________________________________________

## Features

- User Registration
- User Login Authentication
- JWT Token Generation & Validation
- OTP Generation & Verification
- Role-Based Access Control
- Logged-in User Profile Management
- Update User & Profile APIs
- Delete User APIs
- Request Validation using DTOs
- Global Exception Handling
- Redis Caching Support
- Audit Information Tracking

____________________________________________________________________________________________________

## Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Redis
- Maven
- Lombok
- JWT Authentication
- Swagger / OpenAPI

____________________________________________________________________________________________________

## Architecture

The application follows layered architecture:

```text
Controller → Service → Repository → Database
```

____________________________________________________________________________________________________

## Design Patterns Used

- Dependency Injection
- Repository Pattern
- DTO Pattern
- Layered Architecture

____________________________________________________________________________________________________
## Project Structure

```
src/main/java
│
├── entity
├── controller
├── repository
├── service
├── config
├── security
```

____________________________________________________________________________________________________
## Configuration

Update the following properties inside:

```properties
src/main/resources/application.properties
```

Example:

```properties
# Application
spring.application.name=user-service
server.port=8095

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/user_service_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# JWT
jwt.secret=your_secret_key
jwt.expiration=3600000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```
```
Set environment variables before running locally:

$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_USER_SECRET="your_jwt_secret"

____________________________________________________________________________________________________
## Build Project

```bash
mvn clean install
```

____________________________________________________________________________________________________
## Run Project

```bash
mvn spring-boot:run
```

____________________________________________________________________________________________________
## Swagger Documentation

After starting the application:

Swagger UI:
```text
http://localhost:8095/swagger-ui/index.html
```

OpenAPI Docs:
```text
http://localhost:8095/v3/api-docs
```

____________________________________________________________________________________________________
## Main APIs

### Authentication APIs

| Method | API | Description |
|---|---|---|
| POST | `/auth/register` | Register user |
| POST | `/auth/login` | User login |
| POST | `/auth/otp/generate` | Generate OTP |
| POST | `/auth/otp/verify` | Verify OTP |

____________________________________________________________________________________________________
### User APIs

| Method | API | Description |
|---|---|---|
| GET | `/api/users` | Fetch users |
| GET | `/api/users/profile` | Get logged-in user profile |
| GET | `/api/users/username/{username}` | Fetch user by username |
| PUT | `/api/users` | Update user |
| PUT | `/api/users/profile` | Update current user profile |
| DELETE | `/api/users/{username}` | Delete user |

____________________________________________________________________________________________________

## Security

- JWT-based authentication
- Spring Security integration
- Protected APIs using authorization filters
- Password encryption using BCrypt

____________________________________________________________________________________________________

## Caching

Redis is used for:

- OTP storage
- Temporary caching
- Rate limiting support

____________________________________________________________________________________________________

## Profiles

```text
application-dev.properties   → Development Environment
application-prod.properties  → Production Environment
```
