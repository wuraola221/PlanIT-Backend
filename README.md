# PlanIT-Backend

**PlanIT** is a developer task management and productivity platform designed to help development teams create, assign, track, and manage tasks efficiently.

It provides separate workflows for **Lead Developers** and **Developers**, improving task visibility, accountability, and team collaboration.

## Features

* JWT-based authentication and authorization
* Role-based access for Lead Developers and Developers
* Task creation, assignment, prioritization, and tracking
* Task status management
* Task types for features, bugs, incidents, and maintenance
* Deadlines and completion tracking
* Developer comments and task updates
* Email-based account activation
* Password reset functionality
* PostgreSQL database integration

## Tech Stack

* **Java**
* **Spring Boot**
* **Spring Security**
* **JWT**
* **PostgreSQL**
* **JPA / Hibernate**
* **Maven**
* **REST API**

## Project Structure

```text
PlanIT-Backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── pom.xml
├── mvnw
└── README.md
```

## Getting Started

### Prerequisites

* Java 17+
* Maven
* PostgreSQL

### Setup

Clone the repository:

```bash
git clone https://github.com/wuraola221/PlanIT-Backend.git
cd PlanIT-Backend
```

Configure the required environment variables:

```text
DB_PASSWORD
MAIL_USERNAME
MAIL_PASSWORD
JWT_SECRET
APP_BASE_URL
```

Create the PostgreSQL database and update the application configuration as required.

Run the application with:

```bash
./mvnw spring-boot:run
```

The API will be available at:

```text
http://localhost:8080/api/v2.0
```

## API

The backend provides RESTful endpoints for:

* Authentication
* User management
* Task management
* Task status updates
* Comments
* Account activation
* Password recovery

## Security

PlanIT uses **Spring Security and JWT-based authentication** with role-based authorization. Sensitive credentials are managed through environment variables and should not be committed to the repository.

## Author

**Wuraola Oyemade**

Computer Science Graduate | Backend Developer

Java • Spring Boot • PostgreSQL • REST APIs
