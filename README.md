
# Credit Card Management System

A full-stack credit card management application inspired by modern fintech platforms.

Built as a software engineering and SDET portfolio project, with a focus on secure backend development, automated testing, API quality, and continuous integration.

## Project Status

**Under active development**

Currently implemented:
- Spring Boot backend initialization
- PostgreSQL database integration
- User registration REST API
- Input validation with Jakarta Bean Validation
- BCrypt password hashing
- Email uniqueness validation
- Health check endpoint
- JUnit 5, Mockito, and MockMvc automated tests
- 13 passing automated tests

Planned features:
- Secure login and authentication
- Credit card registration and management
- Monthly credit card statements
- Transaction categorization
- Simulated credit card bill payments
- React user interface
- Selenium/TestNG end-to-end tests
- GitHub Actions CI/CD

## Technology Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4.1.2 |
| Database | PostgreSQL 17 |
| Persistence | Spring Data JPA, Hibernate |
| Security | Spring Security, BCrypt |
| API Testing | JUnit 5, Mockito, MockMvc |
| UI Automation (planned) | Selenium, TestNG |
| Frontend (planned) | React |
| Build | Maven Wrapper |
| Version Control | Git, GitHub |

## Project Structure

```text
credit-card-management/
├── backend/
│   ├── src/main/java/com/creditcard/backend/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── BackendApplication.java
│   ├── src/test/java/com/creditcard/backend/
│   │   ├── controller/
│   │   ├── service/
│   │   └── BackendApplicationTests.java
│   ├── pom.xml
│   └── mvnw.cmd
└── README.md
```

## Prerequisites

- Java 21
- PostgreSQL 17
- Git
- VS Code or IntelliJ IDEA

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/cm6-zombie/Credit-Card-Management-System.git
cd Credit-Card-Management-System/backend
```

### 2. Create the database

```sql
CREATE DATABASE credit_card_db;
```

### 3. Configure the database password

Set the environment variable `DB_PASSWORD` to the password for your local PostgreSQL user.

For Windows PowerShell:

```powershell
$securePassword = Read-Host "PostgreSQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
```

### 4. Run the backend

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs at:

`http://localhost:8080`

## Implemented API Endpoints

### Health Check

`GET /api/health`

Returns the application health status.

### User Registration

`POST /api/auth/signup`

Example request:

```json
{
  "name": "Test User",
  "email": "testuser@example.com",
  "password": "TestPass123!"
}
```

Expected success status: `201 Created`.

Passwords are hashed before storage.

## Automated Testing

Run the complete test suite:

```powershell
.\mvnw.cmd test
```

Current automated tests:

- 6 signup controller tests
- 6 user service tests
- 1 Spring Boot context test

**Latest verified result: 13 tests passed, 0 failures.**

The current application context test requires access to the configured PostgreSQL database. Dedicated test database isolation is planned.

## Security Notes

- BCrypt password hashing
- Environment-variable-based database password
- Duplicate email checks
- Input validation

Authentication and authorization are still under development. This application is not production-ready.

Use synthetic test data only. Do not submit real credit card numbers, CVVs, banking credentials, or financial records.

## Roadmap

1. Authentication and authorization
2. Credit card management
3. Monthly statements and transactions
4. Payment simulation
5. React frontend
6. End-to-end testing
7. CI/CD and deployment

## Author

[cm6-zombie](https://github.com/cm6-zombie)

## Disclaimer

This is an educational portfolio project and is not affiliated with CRED or any financial institution. Payments are simulated; no real financial transactions are processed.
