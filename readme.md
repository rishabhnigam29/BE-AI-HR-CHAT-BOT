# BE-AI-HR-CHAT-BOT

## Overview

**BE-AI-HR-CHAT-BOT** is a backend service that powers an AI-driven HR chatbot. It is intended to support use cases such as:

- Candidate Q&A (roles, hiring process, policies)
- Candidate screening and pre-interview interactions
- Internal HR assistance (FAQs, basic workflows)

The project is implemented as a Java Maven application and is designed to be extended with additional AI providers, data sources, and HR-specific logic.

---

## Tech Stack

- **Java** (JDK 17 recommended)
- **Maven**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **PostgreSQL + pgvector** (vector database support)
- **Spring AI** with **Ollama** (chat + embeddings)
- **Docker / Docker Compose** for local infrastructure

---

## Project Structure

```text
BE-AI-HR-CHAT-BOT/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ... (controllers, services, models)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.yml
│   └── test/
│       └── java/
└── README.md
```

---

## Prerequisites

Make sure the following are installed on your system:

- Java JDK 11 or higher (`java -version`)
- Maven (`mvn -version`)
- (Optional) Docker, if containerization is used

---

## Build

To clean and build the project:

```bash
mvn clean package
```

To build without running tests:

```bash
mvn clean package -DskipTests
```

The compiled JAR will be available in the `target/` directory.

---

## Run

### Run using Maven

```bash
mvn spring-boot:run
```

### Run using the packaged JAR

Replace `<artifactId>` and `<version>` with values from `pom.xml`:

```bash
java -jar target/<artifactId>-<version>.jar
```

---

## Configuration

Application configuration is defined in `src/main/resources/application.yaml`.

### Application Settings

```yaml
spring:
  application:
    name: hrchatbot
```

### Database & JPA

- Uses **PostgreSQL** with **pgvector** extension
- Hibernate schema is auto-managed

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### AI Configuration (Spring AI + Ollama)

The chatbot uses **Ollama** as the AI runtime for both chat and embeddings.

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: gemma3:4b
      chat:
        options:
          temperature: 0.4
          model: gemma3:4b
      embedding:
        options:
          model: mxbai-embed-large
```

### Vector Store (pgvector)

Embeddings are stored in PostgreSQL using **pgvector** with an HNSW index.

```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 1024
        max-document-batch-size: 10
```

### File Upload Limits

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

---

## API Endpoints

Actual endpoints and payloads should be confirmed by reviewing the controller classes. Typical endpoints in this project may include:

- `POST /api/chat` – Send a message to the HR chatbot and receive a response
- `POST /api/screen` – Candidate screening interaction
- `GET /health` – Application health check

API request and response models are defined in the DTO or model packages.

---

## Testing

Run all unit and integration tests using:

```bash
mvn test
```

If multiple Maven profiles exist (e.g., integration tests), refer to `pom.xml` for details.

---

## Docker & Local Infrastructure

This project uses **Docker Compose** to run PostgreSQL with pgvector locally.

### Docker Compose (`compose.yaml`)

```yaml
services:
  pgvector:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: mydatabase
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    labels:
      - "org.springframework.boot.service-connection=postgres"
```

### Start Database

```bash
docker compose up -d
```

Spring Boot will automatically connect to the database using the service connection.

> Make sure PostgreSQL is running before starting the application.

---

## Development Notes

- Open the project as a **Maven project** in IntelliJ IDEA
- Locate the main class by searching for `public static void main(String[] args)`
- Use IntelliJ run/debug configurations for local development
- Follow standard Spring Boot layering: controller → service → repository

---

## Git Repository

- **Repository:** BE-AI-HR-CHAT-BOT
- **Branch:** `main`
- **Remote:** `origin`

---

## Contributing

1. Fork the repository
2. Create a feature branch (`feature/your-feature-name`)
3. Commit changes with clear messages
4. Run tests locally
5. Open a pull request with a concise description

---

## License

Add a `LICENSE` file to the repository and specify the license here (e.g., MIT, Apache-2.0).

---

## Learning Resources

If you want to learn how to build AI-powered applications like this using **Spring AI**, **Java**, **chatbots**, and **RAG systems**, check out the following course:

- **Udemy Course:** *Spring AI – Build Java AI Apps, Chatbots & RAG Systems (2026)*  
  https://www.udemy.com/course/spring-ai-build-java-ai-apps-chatbots-rag-systems-2026/?referralCode=F89549F5C0391866566B

This repository aligns closely with the concepts taught in the course, including:
- Spring AI integration
- Ollama-based LLMs and embeddings
- pgvector-backed vector stores
- Real-world backend architecture

---

## Maintainer

- **GitHub:** `rishabhnigam29`

