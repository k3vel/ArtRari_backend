# 🎨 ArtRari - Online Art Auction Platform

![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

> A robust, high-concurrency backend service for an online art auction platform. Built with **Spring Boot** and **Java 23**, it features real-time bidding, role-based access control, and an anti-sniper system.

## 🚀 Engineering Highlights

*   **Concurrency Safe Bidding:** The core bidding engine is rigorously tested against race conditions. Bids are handled safely under high concurrent loads to ensure data integrity when multiple users bid on the same lot simultaneously.
*   **Anti-Sniper Mechanism:** Implemented business logic that automatically extends the auction timer by 10 minutes if a bid is placed within the final 10 minutes, ensuring fair play.
*   **Real-time Notifications:** Utilizes **Spring WebSockets** to provide live updates for outbid alerts, auction starts/ends, and new bids without page reloads.
*   **Rate Limiting & Caching:** Integrated **Bucket4j** with **Redis** to prevent API abuse and DDOS attacks, alongside general Redis caching for high-read endpoints (e.g., active exhibitions).
*   **Robust Testing:** Comprehensive test coverage including integration tests using **Testcontainers** (PostgreSQL) to ensure the application behaves correctly in a production-like environment.

## 🛠️ Tech Stack

*   **Core:** Java 23, Spring Boot (Web, Data JPA, Security, WebSockets)
*   **Database & Migrations:** PostgreSQL 15, Flyway
*   **Caching & Rate Limiting:** Redis, Bucket4j
*   **Security:** JWT Authentication, Role-Based Access Control (User, Curator, Admin)
*   **File Storage:** Cloudinary (Artwork media)
*   **Testing:** JUnit 5, Testcontainers, Spring Boot Test
*   **Infrastructure:** Docker, Docker Compose
*   **Documentation:** Swagger / OpenAPI 3

## 🏗️ System Architecture & Domain

The platform supports distinct user roles with strict business boundaries:
1.  **Users:** Can upload artworks, browse exhibitions, place bids, and pay for won lots.
2.  **Curators:** Responsible for grouping artworks into thematic virtual *Exhibitions* and initiating *Auctions*.
3.  **Admins:** Moderate the platform, manage user bans, and have the authority to cancel fraudulent bids or halt auctions.

### Key Business Rules
*   An artwork can only participate in one active auction at a time.
*   Bids must strictly exceed the current price by a predefined "auction step".
*   If a lot goes unsold, it automatically becomes available for future exhibitions.

## 💻 Running the Project Locally

The project is fully containerized for a smooth developer experience.

### Prerequisites
*   Docker & Docker Compose
*   Java 23 (if running outside Docker)
*   Maven

### Quick Start

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/ArtRari.git
    cd ArtRari
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file in the root directory based on `.env.example`:
    ```env
    JWT_SECRET=your_super_secret_jwt_key_here
    CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
    ```

3.  **Start the infrastructure and application:**
    This will spin up PostgreSQL, Redis, and the Spring Boot Backend application.
    ```bash
    docker-compose up -d --build
    ```

4.  **Access the API:**
    *   The application will be running at: `http://localhost:8080`
    *   Swagger UI (API Documentation): `http://localhost:8080/swagger-ui/index.html`

## 🧪 Testing

To run the test suite (requires Docker to be running for Testcontainers):

```bash
mvn clean test
```
*Note: The test suite includes concurrent bid integration tests (`BidControllerConcurrencyTestIT`) utilizing `ExecutorService` and `CountDownLatch` to prove system stability under concurrent loads.*

---
*Developed by Pavlo Kerbikov*