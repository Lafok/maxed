# Maxed Messenger

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)
![React](https://img.shields.io/badge/React-18-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange)
![Kafka](https://img.shields.io/badge/Kafka-Streaming-black)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![MinIO](https://img.shields.io/badge/MinIO-S3_Storage-purple)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Search-yellow)

A scalable, real-time messaging platform built with a **Modular Monolith** architecture.
Designed to handle high loads using event-driven patterns and industry-standard infrastructure.

## 📚 Demo & Features Showcase

### ⚡ Real-time Interactions (RabbitMQ & Redis)
| **Typing Indicators** | **Read Receipts** |
|:---:|:---:|
| ![Typing GIF](https://github.com/user-attachments/assets/2d8a22ca-c80c-4f53-8d1d-44b9176bb27f) | ![Read Status GIF](https://github.com/user-attachments/assets/341c894f-db8e-4a27-8796-5bcf62298cb6) |
| *Ephemeral events broadcasted via RabbitMQ without hitting the DB.* | *Real-time status updates synced across sessions.* |

### 🔍 Advanced Search & Media (Kafka, Elastic, MinIO)
| **Full-text Search (CQRS)** | **Secure Media Sharing** |
|:---:|:---:|
| ![Search GIF](https://github.com/user-attachments/assets/0ebb4f95-efdb-4e48-b92a-ffce0039473f) | ![Media GIF](https://github.com/user-attachments/assets/0d13843b-e7b3-4966-a66b-cd0b78cdeca7) |
| *Async indexing via **Kafka** to **Elasticsearch** for fuzzy search.* | *Image uploads to **MinIO** with secure Presigned URLs.* |

### 👤 User Discovery & Profile
| **User Search & Instant Chat** | **Avatar Updates** |
|:---:|:---:|
| ![User Search GIF](https://github.com/user-attachments/assets/612011ab-988c-46d6-b7b5-3c706394c7c3) | ![Avatar GIF](https://github.com/user-attachments/assets/fda2151e-05bf-4865-b062-f571199d8ab6) |
| *Optimized SQL search & **Instant Chat Creation** via WebSocket events.* | *User profile updates reflected instantly via React Context & S3.* |