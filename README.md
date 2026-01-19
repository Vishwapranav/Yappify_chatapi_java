# 💬 Yappify - Real-Time Chat Application Backend

A production-ready, scalable real-time messaging platform built with Spring Boot, MongoDB, Apache Kafka, and WebSocket. Features include one-to-one chat, group messaging, JWT authentication, and event-driven architecture.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green.svg)](https://www.mongodb.com/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Environment Variables](#-environment-variables)
- [Docker Deployment](#-docker-deployment)
- [Testing](#-testing)
- [Contrinuting](#-contributing)
- [Author](#-author)
- [Acknowledgements](#-acknowledgments)
---

## ✨ Features

### 👤 User Management
- ✅ User registration and authentication
- ✅ JWT-based secure authentication
- ✅ BCrypt password encryption
- ✅ User search with regex filtering

### 💬 One-to-One Chat
- ✅ Create or access existing conversations
- ✅ Real-time message delivery
- ✅ Message persistence
- ✅ Read receipt tracking

### 👥 Group Chat
- ✅ Create group chats (3+ members)
- ✅ Admin-controlled group management
- ✅ Add/remove members
- ✅ Rename groups
- ✅ Transfer admin rights
- ✅ Leave group functionality
- ✅ Auto-admin reassignment on admin leave

### 🔐 Security
- ✅ JWT token-based authentication
- ✅ Spring Security integration
- ✅ CORS configuration
- ✅ Password encryption with BCrypt
- ✅ Role-based access control

### 🚀 Real-Time Features
- ✅ WebSocket (STOMP) for instant messaging
- ✅ Apache Kafka for asynchronous processing
- ✅ Event-driven architecture
- ✅ Scalable message delivery

### 📊 Additional Features
- ✅ Timestamp auditing (created/updated)
- ✅ Comprehensive error handling
- ✅ Swagger API documentation
- ✅ Docker containerization
- ✅ Kafka UI for monitoring

---

## 🏗️ Architecture

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Client    │────────▶│  Spring Boot │────────▶│   MongoDB   │
│  (Postman)  │◀────────│   REST API   │◀────────│    Atlas    │
└─────────────┘         └──────────────┘         └─────────────┘
                              │    ▲
                              │    │
                         WebSocket │
                              │    │
                              ▼    │
                        ┌──────────────┐
                        │ Apache Kafka │
                        │   (Broker)   │
                        └──────────────┘
                              │    ▲
                              │    │
                         Producer/Consumer
                              │    │
                              ▼    │
                        ┌──────────────┐
                        │   Zookeeper  │
                        └──────────────┘
```

### Key Components:
- **REST API**: Handles HTTP requests for CRUD operations
- **WebSocket**: Real-time bidirectional communication using STOMP
- **Kafka**: Asynchronous message processing and event streaming
- **MongoDB**: Document-based NoSQL database for data persistence
- **Docker**: Containerization for all services

---

## 🛠️ Tech Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data MongoDB** - Database access
- **Spring Security** - Authentication & authorization
- **Spring WebSocket** - Real-time communication

### Message Broker
- **Apache Kafka 3.x** - Event streaming platform
- **Zookeeper** - Kafka coordination

### Database
- **MongoDB Atlas** - Cloud NoSQL database

### Security
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **BCrypt** - Password hashing

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation

### Documentation & Monitoring
- **Swagger/OpenAPI 3.0** - API documentation
- **Kafka UI** - Kafka cluster monitoring

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/yappify-chat.git
cd yappify-chat
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory:

```bash
# MongoDB Configuration
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/yappify?retryWrites=true&w=majority

# JWT Configuration (Generate a secure 256-bit key)
JWT_SECRET=your-secure-jwt-secret-key-minimum-256-bits-long
JWT_EXPIRATION_MS=2592000000

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Important**: Add `.env` to `.gitignore` to keep credentials secure!

### 3. Option A: Run with Docker (Recommended)

#### Start All Services

```bash
# Start Kafka, Zookeeper, MongoDB (local), and the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

#### Access Points:
- **API**: http://localhost:5050
- **Swagger UI**: http://localhost:5050/swagger-ui.html
- **Kafka UI**: http://localhost:8081

### 3. Option B: Run Locally

#### Step 1: Start Kafka & Zookeeper

```bash
# Start only Kafka services with Docker
docker-compose up -d yappify-kafka yappify-zookeeper yappify-kafka-ui
```

#### Step 2: Update MongoDB URI

In `src/main/resources/application.properties`:

```properties
spring.mongodb.uri=${MONGODB_URI}
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=2592000000
```

#### Step 3: Run the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:5050**

---

## 📚 API Documentation

### Base URL
```
http://localhost:5050/api
```

### Interactive Documentation
Access Swagger UI: **http://localhost:5050/swagger-ui.html**

<img width="1889" height="567" alt="swagger_yappify" src="https://github.com/user-attachments/assets/95bc97f4-d511-4cb1-a66d-af67de9b849a" />
<img width="1803" height="627" alt="swagger_yappify3" src="https://github.com/user-attachments/assets/7aa257f3-7019-4c8f-aca3-9f243d5751a7" />
<img width="1832" height="768" alt="swagger_yappify1" src="https://github.com/user-attachments/assets/04b31253-4e37-4571-b785-decd8a30c9b0" />
<img width="1809" height="491" alt="swagger_yappify2" src="https://github.com/user-attachments/assets/bb81c704-1f94-488b-8949-e297ddd5888c" />

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/user/` | Register new user | ❌ |
| POST | `/user/login` | User login | ❌ |
| GET | `/user?search={query}` | Search users | ✅ |

**Example: Register User**
```bash
curl -X POST http://localhost:5050/api/user/ \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "user": {
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Chat Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/chat/` | Create/access one-to-one chat | ✅ |
| GET | `/chat/` | Get all user chats | ✅ |
| GET | `/chat/{chatId}` | Get chat by ID | ✅ |
| POST | `/chat/group` | Create group chat | ✅ |
| PUT | `/chat/rename` | Rename group (admin) | ✅ |
| PUT | `/chat/groupadd` | Add user to group (admin) | ✅ |
| PUT | `/chat/groupremove` | Remove user (admin) | ✅ |
| POST | `/chat/group/{chatId}/leave` | Leave group | ✅ |
| DELETE | `/chat/group/{chatId}` | Delete group (admin) | ✅ |
| PUT | `/chat/group/{chatId}/transfer-admin` | Transfer admin rights | ✅ |

**Example: Create Group Chat**
```bash
curl -X POST http://localhost:5050/api/chat/group \
  -H "Content-Type: application/json" \
  -H "userId: YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Team Chat",
    "users": ["userId1", "userId2"]
  }'
```

### Message Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/message/` | Send message | ✅ |
| GET | `/message/{chatId}` | Get all messages in chat | ✅ |

**Example: Send Message**
```bash
curl -X POST http://localhost:5050/api/message/ \
  -H "Content-Type: application/json" \
  -H "userId: YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "chatId": "CHAT_ID",
    "content": "Hello, World!"
  }'
```

---

## 🔐 Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/yappify` |
| `JWT_SECRET` | Secret key for JWT (min 256 bits) | `your-very-long-secret-key-here` |
| `JWT_EXPIRATION_MS` | Token expiration in milliseconds | `2592000000` (30 days) |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` |
| `SERVER_PORT` | Application port | `5050` |

### Generate Secure JWT Secret

```bash
# Linux/Mac
openssl rand -base64 64

# Or use online generator
# https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

---

## 🐳 Docker Deployment

### Docker Compose Services

```yaml
services:
  - chatapi-app      # Spring Boot application
  - yappify-kafka    # Kafka broker
  - yappify-zookeeper # Zookeeper for Kafka
  - yappify-kafka-ui  # Kafka monitoring UI
```

### Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f chatapi-app

# Restart specific service
docker-compose restart chatapi-app

# Stop all services
docker-compose down

# Remove all data (volumes)
docker-compose down -v

# Rebuild and start
docker-compose up -d --build
```

### Check Service Health

```bash
# Check running containers
docker ps

# Check application health
curl http://localhost:5050/actuator/health

# Check Kafka topics
docker exec -it yappify-kafka kafka-topics --list --bootstrap-server localhost:9092
```

---

## 🧪 Testing

### Using Postman

<img width="350" height="200" alt="postman_yappify" src="https://github.com/user-attachments/assets/e5d3a0fe-3991-49be-971d-7079e2a5b5a5" />
<img width="350" height="450" alt="postman_yappify1" src="https://github.com/user-attachments/assets/75ff0208-7060-4015-a720-4395ea2499e9" />
<img width="350" height="450" alt="postman_yappify2" src="https://github.com/user-attachments/assets/c45e5bb8-975f-4736-99ef-d236c6d13671" />
<img width="350" height="450" alt="postman_yappify3" src="https://github.com/user-attachments/assets/3bd58a01-eeb9-471b-9ddc-47358aa3912f" />

---

## 🔧 Configuration Files

### application.properties

```properties
spring.application.name=chatapi
server.port=5050

# MongoDB
spring.mongodb.uri=${MONGODB_URI}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS:2592000000}

# Kafka
spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=chat-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### docker-compose.yml

See the complete file in the repository root.

---

## 🐛 Troubleshooting

### Common Issues

**1. MongoDB Connection Failed**
```bash
# Check MongoDB Atlas IP Whitelist
# Add your IP or use 0.0.0.0/0 for development

# Test connection
mongosh "mongodb+srv://YOUR_CONNECTION_STRING"
```

**2. Kafka Not Starting**
```bash
# Check if ports are available
lsof -i :9092  # Kafka
lsof -i :2181  # Zookeeper

# Restart Kafka services
docker-compose restart yappify-kafka yappify-zookeeper
```

**3. JWT Token Invalid**
```bash
# Ensure JWT_SECRET is at least 32 characters (256 bits)
# Regenerate secret:
openssl rand -base64 64
```

**4. Port Already in Use**
```bash
# Find process using port 5050
lsof -i :5050  # Mac/Linux
netstat -ano | findstr :5050  # Windows

# Kill process or change port in application.properties
server.port=8080
```

**5. Docker Container Not Starting**
```bash
# Check logs
docker-compose logs chatapi-app

# Rebuild image
docker-compose up -d --build

# Remove all containers and restart
docker-compose down
docker-compose up -d
```

---

## 📊 Monitoring

### Kafka UI
Access: **http://localhost:8081**

<img width="1919" height="599" alt="kafka_yappify" src="https://github.com/user-attachments/assets/1ff8c524-9044-49fb-bc61-bc1100b0ff22" />
<img width="1919" height="535" alt="kafka_yappify1" src="https://github.com/user-attachments/assets/2d340af1-a02c-43e8-87e2-bd758808eaf5" />
<img width="1919" height="549" alt="kafka_yappify2" src="https://github.com/user-attachments/assets/09b0544b-048f-4bb4-9415-c3f9ba8ab8df" />
<img width="1919" height="711" alt="kafka_yappify3" src="https://github.com/user-attachments/assets/f02777f8-afce-4441-aae1-b346838b0d0f" />
<img width="1919" height="1029" alt="kafka_yappify4" src="https://github.com/user-attachments/assets/ee452bba-7613-406d-8c0c-def86499590e" />
<img width="1919" height="567" alt="kafka_yappify5" src="https://github.com/user-attachments/assets/63af7128-2cf5-4560-89c3-ad5b5ddf6cd4" />
<img width="1919" height="921" alt="kafka_yappify6" src="https://github.com/user-attachments/assets/b77398d0-8f6a-470c-9239-a46351bfc26e" />


Features:
- View topics and messages
- Monitor consumer lag
- Check broker health
- Inspect message content


### Application Health
```bash
# Health check endpoint
curl http://localhost:5050/actuator/health

# Check Spring Boot metrics
curl http://localhost:5050/actuator/metrics
```

---


## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

---

## 👨‍💻 Author

**Vishwapranav**

- LinkedIn: ([https://linkedin.com/in/yourprofile](https://www.linkedin.com/in/vishwapranav/))
- Email: vishwapranav2003@gmail.com

---

## 🙏 Acknowledgments

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Docker Documentation](https://docs.docker.com/)

---

