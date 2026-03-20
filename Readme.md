# CodeLab Backend

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![JWT](https://img.shields.io/badge/JWT-0.12.6-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

**A production-ready REST API for a source code marketplace platform.**
Buy, sell, and download source code projects.

[Live API](https://codelab-backend-l36z.onrender.com) · [Frontend](https://codelab-frontend-hjey74uob-lucicore0001-7739s-projects.vercel.app) · [Swagger UI](https://codelab-backend-l36z.onrender.com/swagger-ui.html)

</div>

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Database Schema](#database-schema)
- [File Storage](#file-storage)
- [Environment Variables](#environment-variables)
- [Running Locally](#running-locally)
- [Deployment](#deployment)
- [Error Handling](#error-handling)

---

## Overview

CodeLab is a full-stack source code marketplace where developers can:
- Upload and sell their source code projects
- Browse and download projects from other developers
- Manage their public profile and portfolio

This repository contains the **Spring Boot REST API backend**.

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security 6 + JWT |
| OAuth2 | Google OAuth2 |
| Database | MySQL 8 (Aiven Cloud) |
| ORM | Spring Data JPA + Hibernate |
| File Storage | Cloudinary (images) + Supabase Storage (ZIPs) |
| Build Tool | Maven |
| Deployment | Render (Docker) |
| Documentation | Swagger / OpenAPI 3 |

---

## Features

### Authentication
- ✅ Register with email + username + password
- ✅ Login with email OR username
- ✅ JWT access token + refresh token
- ✅ Google OAuth2 login
- ✅ BCrypt password hashing (strength 12)
- ✅ Role-based access control (USER / ADMIN)

### Projects
- ✅ Upload project with cover image + ZIP file
- ✅ Browse all published projects (paginated)
- ✅ Search projects by title or tag
- ✅ Filter by tags
- ✅ Download ZIP (authenticated users only)
- ✅ Download counter
- ✅ Delete own project (owner or ADMIN)
- ✅ Update project details

### User Profiles
- ✅ Public profile (anyone can view)
- ✅ Private profile (own full details)
- ✅ Update profile info (bio, location, education, work)
- ✅ Upload profile image
- ✅ Profile stats (total projects, total downloads)

### Tags
- ✅ Auto-create tags on project upload
- ✅ Many-to-many project-tag relationship
- ✅ Get all unique tags

---

## Project Structure

```
src/main/java/com/codelab/backend/
│
├── config/
│   ├── ApplicationConfig.java      # UserDetailsService, BCrypt, AuthManager
│   ├── CloudinaryConfig.java       # Cloudinary bean setup
│   ├── CorsConfig.java             # CORS filter configuration
│   ├── SecurityConfig.java         # Spring Security filter chain
│   └── WebConfig.java              # Static resource handler
│
├── controller/
│   ├── AuthController.java         # /api/v1/auth/**
│   ├── ProjectController.java      # /api/v1/projects/**
│   └── UserController.java         # /api/v1/users/**
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── ProjectUploadRequest.java
│   │   └── UpdateProfileRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserSummaryResponse.java
│       ├── PublicProfileResponse.java
│       ├── OwnProfileResponse.java
│       ├── ProjectResponse.java
│       └── ProjectCardResponse.java
│
├── entity/
│   ├── User.java                   # UserDetails implementation
│   ├── Project.java
│   ├── Tag.java
│   └── Role.java                   # Enum: USER, ADMIN
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── AppException.java
│   ├── EmailAlreadyExistsException.java
│   ├── UsernameAlreadyExistsException.java
│   ├── InvalidCredentialsException.java
│   ├── UserNotFoundException.java
│   └── project/
│       ├── ProjectNotFoundException.java
│       ├── AccessDeniedException.java
│       └── FileStorageException.java
│
├── repository/
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   └── TagRepository.java
│
├── security/
│   ├── jwt/
│   │   ├── JwtService.java         # Token generation + validation
│   │   └── JwtAuthFilter.java      # Bearer token interceptor
│   └── oauth2/
│       ├── CustomOAuth2UserService.java
│       └── OAuth2SuccessHandler.java
│
└── service/
    ├── AuthService.java
    ├── ProjectService.java
    ├── UserProfileService.java
    ├── FileStorageService.java
    ├── CloudinaryService.java
    └── SupabaseStorageService.java
```

---

## API Endpoints

### Auth — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | Public | Register new user |
| POST | `/login` | Public | Login (email or username) |
| POST | `/refresh-token` | Public | Get new access token |

### Projects — `/api/v1/projects`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Public | Get all projects (paginated) |
| GET | `/{id}` | Public | Get project by ID |
| GET | `/search?q=` | Public | Search projects |
| GET | `/tags` | Public | Get all unique tags |
| GET | `/user/{username}` | Public | Get projects by username |
| POST | `/` | Required | Upload new project |
| PUT | `/{id}` | Required | Update project |
| DELETE | `/{id}` | Required | Delete project |
| GET | `/{id}/download` | Required | Download project ZIP |

### Users — `/api/v1/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/{username}` | Public | Get public profile |
| GET | `/{username}/projects` | Public | Get user's projects |
| GET | `/me` | Required | Get own full profile |
| PUT | `/me` | Required | Update own profile |
| POST | `/me/profile-image` | Required | Upload profile image |

---

## Authentication

### JWT Flow

```
POST /api/v1/auth/login
→ Returns: { accessToken, refreshToken, tokenType, user }

All protected requests:
→ Header: Authorization: Bearer <accessToken>

Token expires → use refresh token:
POST /api/v1/auth/refresh-token
{ "refreshToken": "eyJ..." }
→ Returns new accessToken + refreshToken
```

### Google OAuth2 Flow

```
1. Frontend redirects to:
   GET /oauth2/authorization/google

2. User logs in with Google

3. Backend redirects to:
   {FRONTEND_URL}/oauth2/callback?accessToken=eyJ...&refreshToken=eyJ...

4. Frontend stores tokens in localStorage
```

---

## Database Schema

```sql
users
  id, email, username, password, real_name, bio,
  location, education, work, website_url,
  profile_image_url, role, enabled,
  provider, provider_id, created_at

projects
  id, title, description, about,
  cover_image_url, zip_file_url, zip_file_name, zip_file_size,
  download_count, published, uploader_id,
  created_at, updated_at

tags
  id, name

project_tags
  project_id, tag_id
```

---

## File Storage

| File Type | Storage | Service | Limit |
|-----------|---------|---------|-------|
| Cover images | Cloudinary | `cloudinary.com` | 5MB |
| Profile images | Cloudinary | `cloudinary.com` | 5MB |
| ZIP files | Supabase Storage | `supabase.co` | 50MB |

---

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | MySQL JDBC URL | `jdbc:mysql://host:port/db` |
| `DATABASE_USERNAME` | DB username | `avnadmin` |
| `DATABASE_PASSWORD` | DB password | `AVNS_xxx` |
| `APP_JWT_SECRET` | JWT signing secret (Base64) | `404E635266...` |
| `APP_FRONTEND_URL` | Frontend URL for CORS | `https://yourapp.vercel.app` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | `xxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 secret | `GOCSPX-xxx` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | `dcazaswuj` |
| `CLOUDINARY_API_KEY` | Cloudinary API key | `492792899398769` |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | `0WKhnx...` |
| `SUPABASE_URL` | Supabase project URL | `https://xxx.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY` | Supabase service role JWT | `eyJhbGci...` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `PORT` | Server port | `8080` |

---

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8.0+
- Git

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/codelab-backend.git
cd codelab-backend

# 2. Create MySQL database
mysql -u root -p
CREATE DATABASE codelab;
exit;

# 3. Update application.yml
# Set your MySQL password in src/main/resources/application.yml

# 4. Run the application
mvn spring-boot:run

# 5. Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Testing with Postman

```bash
# Register
POST http://localhost:8080/api/v1/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

# Login
POST http://localhost:8080/api/v1/auth/login
{
  "email": "testuser",
  "password": "password123"
}

# Use token in Authorization header
Authorization: Bearer eyJ...
```

---

## Deployment

### Deployed on Render using Docker

```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/codelab-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-Dserver.port=8080","-jar","app.jar"]
```

### Infrastructure

```
Backend API    → Render (Docker, Free tier)
Database       → Aiven MySQL (Free tier, 1GB)
Image Storage  → Cloudinary (Free tier, 25GB bandwidth)
ZIP Storage    → Supabase Storage (Free tier, 1GB)
Frontend       → Vercel (Free tier)
```

---

## Error Handling

All errors return consistent JSON:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Email is already registered",
  "path": "/api/v1/auth/register",
  "timestamp": "2026-03-18T..."
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad request / Validation error |
| 401 | Unauthorized — missing or invalid token |
| 403 | Forbidden — insufficient permissions |
| 404 | Resource not found |
| 409 | Conflict — duplicate email/username |
| 500 | Internal server error |

---

## License

MIT License — feel free to use this project for learning and building.

---

<div align="center">
Built with ❤️ using Spring Boot 3.5.11
</div>