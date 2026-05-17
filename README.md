# Public Grievance Tracker — Complaint Resolution System

A full-stack complaint management system built with **Java · Spring Boot · Spring Security · MySQL · Thymeleaf**.  
Citizens can submit and track complaints. Admins assign and resolve them. Every route is secured by role.

---

## The Problem It Solves

Most public grievance portals are black holes — you submit a complaint and never know what happens next.  
This system gives **citizens real-time visibility** into their complaint status, and gives **admins a structured workflow** to assign, act on, and resolve issues — with every action tracked.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot, Spring MVC |
| Security | Spring Security (RBAC) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL |
| Frontend | Thymeleaf (server-side rendering) |
| Build Tool | Maven |

---

## Architecture

```
Browser (Thymeleaf Views)
        ↓
Spring MVC Controllers  ←──  Spring Security (Auth + Role Guard)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (Spring Data JPA)
        ↓
MySQL Database
```

---

## Role-Based Access Control

Two roles, completely separated at the controller level:

| Action | Citizen | Admin |
|---|---|---|
| Register / Login | ✅ | ✅ |
| Submit complaint | ✅ | ❌ |
| Track own complaints | ✅ | ❌ |
| View all complaints | ❌ | ✅ |
| Assign complaint to self | ❌ | ✅ |
| Resolve / close complaint | ❌ | ✅ |

Unauthorized route access is blocked at the controller level using Spring Security's method-level and URL-level authorization — not just hidden on the UI.

---

## Complaint Lifecycle

```
[Citizen Submits]
       ↓
   PENDING
       ↓
[Admin Assigns to self]
       ↓
   IN_PROGRESS
       ↓
[Admin Resolves]
       ↓
   RESOLVED
```

Citizens can track their complaint status at any point in this lifecycle.

---

## Database Schema (Normalized)

### Users
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| name | VARCHAR | |
| email | VARCHAR | unique |
| password | VARCHAR | BCrypt hashed |
| role | ENUM | CITIZEN / ADMIN |

### Complaints
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| title | VARCHAR | |
| description | TEXT | |
| status | ENUM | PENDING / IN_PROGRESS / RESOLVED |
| created_at | TIMESTAMP | |
| citizen_id | BIGINT | FK → Users |
| assigned_admin_id | BIGINT | FK → Users (nullable) |

---

## API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/register` | Public | Registration page |
| POST | `/register` | Public | Create account |
| GET | `/login` | Public | Login page |
| POST | `/login` | Public | Authenticate |

### Citizen
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/citizen/dashboard` | CITIZEN | View own complaints |
| GET | `/citizen/complaint/new` | CITIZEN | Submit complaint form |
| POST | `/citizen/complaint/submit` | CITIZEN | Submit complaint |
| GET | `/citizen/complaint/{id}` | CITIZEN | Track complaint status |

### Admin
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/admin/dashboard` | ADMIN | View all complaints |
| POST | `/admin/complaint/{id}/assign` | ADMIN | Assign to self |
| POST | `/admin/complaint/{id}/resolve` | ADMIN | Mark as resolved |

---

## Setup & Run

### Prerequisites
- Java 17+
- MySQL 8+
- Maven

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/Raghav-Sharma03/public-grievance-tracker.git
cd public-grievance-tracker

# 2. Create MySQL database
CREATE DATABASE grievance_tracker;

# 3. Configure application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/grievance_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# 4. Run the app
mvn spring-boot:run
```

App starts at `http://localhost:8080`

---

## Security Implementation

- Passwords hashed with **BCryptPasswordEncoder** — never stored in plain text
- Session-based authentication managed by Spring Security
- Role checks enforced at both **URL level** (HttpSecurity config) and **controller level** (`@PreAuthorize`)
- CSRF protection enabled (Spring Security default)
- Unauthorized access redirects to `/login`, not a 403 error page

---

## What I Learned

- How Spring Security's filter chain works under the hood
- Designing role-based systems where access rules are enforced server-side, not just in the UI
- Building a normalized relational schema that reflects real-world entity relationships
- Integrating Thymeleaf with Spring MVC for clean server-side rendering with dynamic data

---

## Planned Enhancements

- [ ] Email notifications on status change
- [ ] Admin analytics dashboard (complaints by category, avg resolution time)
- [ ] REST API layer for mobile client support
- [ ] Pagination on admin complaint list

---

## Author

**Raghav Sharma**  
[GitHub](https://github.com/Raghav-Sharma03) · [LinkedIn](https://linkedin.com/in/raghav-sharma-478191270)
