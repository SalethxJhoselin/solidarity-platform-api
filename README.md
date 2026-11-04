# UAGRM Solidarity Community Platform - Backend REST API

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Database%20Migrations-blueviolet.svg?style=flat&logo=liquibase)](https://www.liquibase.com/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2496ED.svg?style=flat&logo=docker)](https://www.docker.com/)
[![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0%20Swagger%20UI-85EA2D.svg?style=flat&logo=swagger)](http://localhost:8080/swagger-ui/index.html)
[![Render](https://img.shields.io/badge/Deploy-Render-46E3B7.svg?style=flat&logo=render)](https://render.com/)

A production-grade, enterprise-ready RESTful backend built with **Spring Boot 3**, **Java 21**, and **PostgreSQL** for the Autonomous University Gabriel René Moreno (UAGRM). The platform powers a mutual aid network enabling students, faculty, and administrative staff to publish help requests, offer volunteer assistance, donate physical goods, coordinate collective campaigns, and track requests through an auditable lifecycle.

---

## Architecture & Engineering Highlights

This codebase has been architected following **Senior Java / Tech Lead international standards**:

```
bo.edu.uagrm.backend
├── config             # Centralized WebMvc, OpenAPI 3.0, and application setup
├── controllers        # RESTful API controllers with dual-route aliasing
├── dtos               # Strongly-typed Request and Response Transfer Objects
├── entities           # JPA Domain models (Auditing, Relationships)
├── enums              # Decoupled Domain Enums (Domain-Driven Design cohesion)
├── exceptions         # GlobalExceptionHandler with RFC-compliant error mapping & SLF4J
├── mappers            # Dedicated Mapping layer decoupling services from DTO transformations
├── repositories       # Spring Data JPA repositories with @EntityGraph (N+1 query free)
│   └── specifications # Dynamic multi-criteria predicates (CriteriaBuilder)
├── security           # JWT stateless auth, AuthenticationFacade, @CurrentUser Resolver
└── services           # Domain business logic with transactional boundaries
```

### Key Architectural Patterns Applied:
1. **Security Facade & Custom Argument Resolver:**
   - Implements `AuthenticationFacade` to decouple controllers from Spring Security context.
   - Custom `@CurrentUser Usuario user` method parameter annotation via `CurrentUserArgumentResolver`.
2. **Dedicated Mapping Layer (Single Responsibility Principle):**
   - Eliminates inline DTO builders from services using dedicated mappers (`PublicacionMapper`, `ReporteMapper`, `ComentarioMapper`, `DonacionMapper`).
3. **JPA Query Optimization (Zero N+1 SELECTs):**
   - Uses JPA `@EntityGraph(attributePaths = {...})` on repositories to fetch associations via optimized `LEFT OUTER JOIN`s.
4. **Zero-Downtime Multi-Route Aliasing:**
   - All 14 controllers expose canonical **English REST endpoints** (`/publications`, `/categories`, `/donations`) while supporting legacy aliases (`/publicaciones`, `/categorias`) for 100% backward compatibility.
5. **Structured Observability:**
   - English-standard `@Slf4j` event logging for state transitions, moderation actions, and detailed stack trace logging in `GlobalExceptionHandler`.

---

## Interactive API Documentation (OpenAPI / Swagger 3)

The application provides an interactive **Swagger UI** for testing endpoints and reviewing data contracts:

- **Swagger UI Console:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI 3.0 Specification (JSON):** `http://localhost:8080/v3/api-docs`

> **Authentication in Swagger UI:** Click the **Authorize** button in Swagger UI and supply `Bearer <JWT_TOKEN>` obtained from `POST /api/v1/auth/login`.

---

## Canonical REST Endpoints Matrix

| Resource Domain | Canonical English Route | Compatibility Alias | Description |
| :--- | :--- | :--- | :--- |
| **Authentication** | `POST /api/v1/auth/login`<br>`POST /api/v1/auth/register` | — | User authentication and JWT issuance |
| **User Profile** | `GET /api/v1/profile/my-profile`<br>`PUT /api/v1/profile/my-profile` | `/perfil/mi-perfil` | Profile management via `@CurrentUser` |
| **Publications Feed**| `GET /api/v1/publications`<br>`GET /api/v1/publications/{id}` | `/publicaciones` | Public active publication directory |
| **Search & Filter** | `GET /api/v1/publications/search` | `/publicaciones/buscar` | Multi-criteria dynamic filtering |
| **User Publications**| `GET /api/v1/publications/my-publications` | `/publicaciones/mis-publicaciones` | Authenticated user's publications |
| **Create Request** | `POST /api/v1/publications/need` | `/publicaciones/necesidad` | Help request registration (HU9) |
| **Create Volunteer**| `POST /api/v1/publications/volunteering` | `/publicaciones/voluntariado` | Volunteer schedule & areas (HU10) |
| **Create Donation** | `POST /api/v1/publications/donation` | `/publicaciones/donacion` | Physical donation offer (HU11) |
| **Collective Event** | `POST /api/v1/publications/collective-activity` | `/publicaciones/actividad-colectiva` | Campaigns, raffles, and fairs (HU12) |
| **Categories** | `GET /api/v1/categories` | `/categorias` | Help categories catalog (HU7) |
| **Resource Types** | `GET /api/v1/resource-types` | `/tipos-recurso` | Donable items catalog (HU8) |
| **Comments** | `POST /api/v1/comments`<br>`GET /api/v1/publications/{id}/comments` | `/comentarios` | Discussion and community thread (HU15) |
| **Donation Receipt**| `POST /api/v1/donations/fulfill` | `/donaciones/concretar` | Fulfill aid & mark request RESUELTA (HU17) |
| **Tracking** | `GET /api/v1/tracking/requests/{id}` | `/seguimiento/solicitudes/{id}` | Lifecycle milestone timeline (HU21) |
| **Smart Matching** | `GET /api/v1/recommendations/match/{id}` | `/recomendaciones/match/{id}` | Multi-variable compatibility match (HU22) |
| **Moderation** | `GET /api/v1/reports/inbox`<br>`POST /api/v1/reports/publications/{id}/block` | `/reportes/bandeja` | Offensive content flags & triage (HU18, HU19) |
| **Administration** | `GET /api/v1/admin/users`<br>`POST /api/v1/admin/assign-roles` | `/admin/usuarios` | User permissions & roles (HU4, HU6) |
| **Audit Logs** | `GET /api/v1/admin/audit/publications/{id}` | `/admin/auditoria` | Immutable modification history (HU20) |
| **Verification** | `POST /api/v1/verification/academic-record` | `/validacion/registro-academico` | Academic status registration (HU5) |

---

## Environment Variables Configuration

Copy `.env.example` to `.env` or set these in your hosting environment:

| Variable | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `PORT` | No | `8080` | HTTP port on which Tomcat listens |
| `DB_URL` | **Yes** | `jdbc:postgresql://<host>:5432/<db>?sslmode=require` | PostgreSQL JDBC Connection URL |
| `DB_USER` | **Yes** | `postgres` | Database username |
| `DB_PASSWORD` | **Yes** | `••••••••` | Database user password |
| `JWT_SECRET` | **Yes** | `bXktdmVyeS1zZWNyZXQtYW5k...` | Base64 HMAC-SHA512 Secret Key (min 32 bytes) |
| `JWT_EXPIRATION` | No | `86400000` (24h) | JWT validity duration in milliseconds |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000,https://*.vercel.app` | Allowed CORS origins for frontend web client |
| `UPLOAD_DIR` | No | `uploads` | Directory for evidence images and attachments |

---

## Getting Started (Local Development)

### Prerequisites
- **JDK 21** or higher.
- **Maven 3.9+** (or use the included `./mvnw`).
- **PostgreSQL 16** (or Supabase remote pooler).

### Running with Maven Wrapper
```bash
# Clone the repository
git clone https://github.com/your-username/backend-uagrm.git
cd backend-uagrm

# Run unit and integration tests
./mvnw clean test

# Start the application
./mvnw spring-boot:run
```
The server will start at `http://localhost:8080`.

---

## Running with Docker & Multi-Stage Packaging

The project includes an optimized multi-stage `Dockerfile`:
```bash
# Build the Docker image
docker build -t backend-uagrm:latest .

# Run container with environment configuration
docker run -d -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://your-db-host:5432/postgres?sslmode=require" \
  -e DB_USER="your-db-user" \
  -e DB_PASSWORD="your-db-password" \
  -e JWT_SECRET="your-base64-secret" \
  backend-uagrm:latest
```

---

## Cloud Deployment (Render)

This repository includes a native [render.yaml](render.yaml) blueprint:
1. In the **Render Dashboard**, select **New +** $\rightarrow$ **Blueprint**.
2. Connect your GitHub repository.
3. Render automatically detects the multi-stage Docker build, healthcheck path (`/actuator/health`), and required environment variables.
4. Input your `DB_PASSWORD` and `JWT_SECRET` in the Render Environment Variables tab.
5. Deployments trigger automatically on every `git push origin main`.

---

## Automated Testing & CI/CD

The test suite covers controllers, domain services, security argument resolvers, and JPA query graphs:
```bash
# Run all tests
./mvnw test

# Compile tests without executing
./mvnw test-compile
```
A continuous integration pipeline is defined in `.github/workflows/ci.yml` to automatically validate builds, run unit tests, and check code hygiene on every Pull Request.

