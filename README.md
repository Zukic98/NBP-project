# Advanced Databases - JDBC Project

This repository contains a Java-based backend application developed for the **Advanced Databases** course. The project strictly focuses on direct database interaction using the **pure JDBC API** (without ORM frameworks like Hibernate or Spring Data JPA).

## 🚓 About The Project (SUDS)

**SUDS (Sistem za upravljanje dokazima i slučajevima / Evidence and Case Management System)** is a comprehensive backend system designed for law enforcement agencies. It digitalizes the tracking of criminal cases, suspects, and the critical chain of custody for forensic evidence.

### Key Features
* **Case Management:** Create and track criminal cases, assigning lead investigators.
* **Suspect Tracking:** Register suspects and link them to specific cases and criminal offenses.
* **Evidence & Chain of Custody:** Log physical evidence and strictly track its movement (who handed it over, who received it, and when) to maintain legal integrity.
* **Personnel Management:** View and manage police station staff and forensic experts.

## 🛠️ Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 4.0.4 (Web & REST API only)
* **Database Access:** Pure JDBC (`java.sql.*`)
* **Database:** Oracle Database (`ojdbc11`)
* **Build Tool:** Maven

## 🏛️ Architecture

The project strictly follows a **3-Tier Architecture** to separate concerns:
1. **Controllers (REST API):** Handle incoming HTTP requests and return JSON responses.
2. **Services (Business Logic):** Contain all validation and business rules.
3. **Repositories (Data Access):** The *only* layer that interacts with the Oracle database using raw SQL queries (`PreparedStatement`, `ResultSet`).
* *Note: Data Transfer Objects (DTOs) are used to format data for the frontend, while Models represent raw database tables.*

## 📥 Getting Started

To get a local copy up and running, follow these steps:

### 1. Prerequisites
* Java Development Kit (JDK 17 or 21) installed.
* Maven installed.
* An active connection to the Oracle Database.

### 2. Clone the repository
```bash
git clone [https://github.com/Zukic98/NBP-project.git](https://github.com/Zukic98/NBP-project.git)
cd NBP-project
```

### 3. Configure the Database Connection
Do not commit your real database credentials to GitHub! 
Create or modify the `src/main/resources/application.yml` file with your Oracle database details:

```yaml
server:
  port: 8080

db:
  url: jdbc:oracle:thin:@//YOUR_HOST:PORT/YOUR_SERVICE_NAME
  username: YOUR_USERNAME
  password: YOUR_PASSWORD
```

### 4. Build and Run
You can run the application directly from your IDE (e.g., IntelliJ IDEA) by executing the `SudsApplication.java` main class, or via the terminal using Maven:

```bash
mvn clean install
mvn spring-boot:run
```
The REST API will be available at `http://localhost:8080`.
