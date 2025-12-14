# Maxed Messenger

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-19-blue?style=flat-square&logo=react)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange?style=flat-square&logo=rabbitmq)
![Kafka](https://img.shields.io/badge/Kafka-Streaming-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-Caching-red?style=flat-square&logo=redis)
![MinIO](https://img.shields.io/badge/MinIO-S3_Storage-purple?style=flat-square&logo=minio)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Search-yellow?style=flat-square&logo=elasticsearch)

A modern, scalable, real-time messaging platform built with a **Modular Monolith** architecture.
Designed to handle high concurrency using event-driven patterns, ensuring data consistency and low latency.

[![Frontend Repo](https://img.shields.io/badge/Maxed_Frontend-React_App-blue?style=for-the-badge&logo=react)](https://github.com/Lafok/maxed_frontend)
---

## 🏗 System Architecture & Engineering

This project demonstrates the transition from a simple CRUD application to a distributed system architecture suitable for high-load environments.

### 1. Secure Media Delivery (S3 & Presigned URLs)
**The Problem:** Handling binary data in the database bloats storage and slows down backups.
**The Solution:**
*   Files are uploaded directly to **MinIO (S3)**.
*   The database stores only metadata and references.
*   Access is granted via short-lived **Presigned URLs** to ensure security (IDOR protection).

![Media Architecture](https://github.com/user-attachments/assets/f9a7e76e-6693-4215-9c51-09a4017cc806)

**Demo:**
![Send Media Demo](https://github.com/user-attachments/assets/0d13843b-e7b3-4966-a66b-cd0b78cdeca7)

---

### 2. High-Performance Initial Load
**The Problem:** The "N+1" query problem when fetching chat lists along with user statuses and last messages.
**The Solution:**
*   **Chat Service** aggregates data from **PostgreSQL** (Chat metadata) and **Redis** (Real-time presence).
*   Data is fetched in parallel batches, reducing latency during application startup.

![Initial Load Architecture](https://github.com/user-attachments/assets/73db3c41-27ea-431a-868a-8a4e4bb1831f)

**Demo (Instant Chat Creation & Load):**
![User Search Demo](https://github.com/user-attachments/assets/612011ab-988c-46d6-b7b5-3c706394c7c3)

---

### 3. Real-time Presence System (Redis)
**The Problem:** Writing "Online/Offline" status to a disk-based DB (Postgres) creates unnecessary I/O load.
**The Solution:**
*   **Redis (TTL Keys):** Used as an ephemeral store for user presence.
*   **WebSocket Events:** `SessionConnected` / `SessionDisconnect` events trigger updates in Redis and broadcast via RabbitMQ.

![Presence Architecture](https://github.com/user-attachments/assets/ff7941aa-99ad-4ab1-a5d7-3a7ac75edb59)

**Demo (Profile & Status Updates):**
![Avatar Change Demo](https://github.com/user-attachments/assets/fda2151e-05bf-4865-b062-f571199d8ab6)

---

### 4. Ephemeral Typing Indicators
**The Problem:** Storing "Is typing..." events in a database is resource-intensive and useless for history.
**The Solution:**
*   **Zero-Database approach.**
*   Events are routed directly through **RabbitMQ** topics (`/topic/chats.{id}.typing`).
*   Frontend uses debouncing to handle high-frequency input events.

![Typing Architecture](https://github.com/user-attachments/assets/69cc01ed-359a-4188-986a-01dd9dd1486d)

**Demo:**
![Typing Demo](https://github.com/user-attachments/assets/2d8a22ca-c80c-4f53-8d1d-44b9176bb27f)

---

### 5. Distributed Search (CQRS with Kafka)
**The Problem:** `LIKE %...%` queries in SQL are slow on large datasets.
**The Solution:**
*   **CQRS Pattern:** Write operations go to PostgreSQL (ACID).
*   **Eventual Consistency:** A `MessageCreatedEvent` is published to **Kafka**.
*   **Elasticsearch:** Consumes the event and indexes the text for fuzzy search.

**Demo:**
![Search Demo](https://github.com/user-attachments/assets/0ebb4f95-efdb-4e48-b92a-ffce0039473f)

---

### 6. Read Receipts (Data Consistency)
**Description:** Real-time synchronization of message status ("Sent" vs "Read").
*   Uses bulk updates in PostgreSQL for performance.
*   Broadcasts read events via RabbitMQ to update UI across multiple sessions.

**Demo:**
![Read Status Demo](https://github.com/user-attachments/assets/341c894f-db8e-4a27-8796-5bcf62298cb6)

---

## 🛠 Tech Stack

**Backend**
*   **Java 21, Spring Boot 3**
*   **Spring Security** (JWT Authentication)
*   **Spring Data JPA** (PostgreSQL)
*   **Spring Data Redis** (Cache & Presence)
*   **Spring Kafka** (Event Streaming)
*   **Spring AMQP** (RabbitMQ)

**Infrastructure (Dockerized)**
*   **PostgreSQL:** Primary relational database.
*   **RabbitMQ:** Message broker for WebSocket STOMP relay.
*   **Redis:** In-memory store for high-speed status checks.
*   **Kafka + Zookeeper:** Decoupling search indexing from chat logic.
*   **Elasticsearch:** Full-text search engine.
*   **MinIO:** S3-compatible object storage.

**Frontend**
*   **React 19, TypeScript**
*   **Tailwind CSS** (Styling)
*   **STOMPjs** (WebSocket Client)
*   **Axios** (HTTP Client)

---

## 🐳 Getting Started

The entire infrastructure is defined in `docker-compose.yml` for easy setup.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Lafok/maxed
    ```

2.  **Start Infrastructure:**
    ```bash
    docker-compose up -d
    ```
    *This will spin up Postgres, Redis, RabbitMQ, Kafka, Elastic, and MinIO.*

3.  **Run Backend:**
    ```bash
    ./gradlew bootRun
    ```

4.  **Run Frontend:**
    *Open a new terminal and clone the frontend repository:*
    ```bash
    git clone https://github.com/Lafok/maxed_frontend
    cd maxed_frontend
    npm install
    npm run dev
    ```

### Access Points
*   **Frontend:** `http://localhost:5173`
*   **Swagger API Docs:** `http://localhost:8080/swagger-ui.html`
*   **RabbitMQ Console:** `http://localhost:15672` (guest/guest)
*   **MinIO Console:** `http://localhost:9001` (minioadmin/minioadmin)