# Public Grievance Tracker

A production-grade complaint management portal built with Spring Boot 4, Thymeleaf, MySQL, and deployed on Render.

## Tech Stack

- Backend: Java 21, Spring Boot 4.0.8, Spring MVC, Spring Security
- Frontend: Thymeleaf, Bootstrap 5.3.3
- Database: MySQL 8 (Aiven Cloud), Flyway migrations
- Deployment: Docker, Render (dev + production environments)
- CI/CD: GitHub Actions (automated build, test, deploy)

## Roles

- CITIZEN: Register, submit complaints, track status, cancel pending complaints
- ADMIN: Manage all complaints, update status, add remarks, view all users
- OFFICER: Same as ADMIN

## Features

### Citizen

- Register and login
- Submit complaint with title, category, location, description, optional image
- View own complaints with status tracking
- Cancel pending complaints
- View admin remarks on complaints

### Admin/Officer

- View all complaints with search and filtering
- Update complaint status with workflow rules
- Add remarks to complaints
- View status change history and audit trail
- View all registered users

## Status Workflow

PENDING → IN_PROGRESS or REJECTED

IN_PROGRESS → RESOLVED, REJECTED, or PENDING

## API Routes

### Public

- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `GET /actuator/health`

### Citizen (`/citizen/**`)

- `GET /citizen/dashboard`
- `GET /citizen/submit-complaint`
- `POST /citizen/submit-complaint`
- `GET /citizen/complaint/{id}`
- `POST /citizen/complaint/{id}/cancel`

### Admin (`/admin/**`)

- `GET /admin/dashboard`
- `GET /admin/complaint/{id}`
- `POST /admin/complaint/{id}/status`
- `POST /admin/complaint/{id}/comment`
- `GET /admin/users`

## Environment Variables

```env
DATABASE_URL=jdbc:mysql://host:port/dbname?ssl-mode=REQUIRED&serverTimezone=UTC
DB_USERNAME=
DB_PASSWORD=
ADMIN_EMAIL=
ADMIN_PASSWORD=
PORT=8080
UPLOAD_DIR=uploads/complaints/
```

## Local Development

1. Clone the repository.
2. Copy `.env.example` to `.env` and fill in the values.
3. Start MySQL locally or use Aiven Cloud.
4. Run `./mvnw spring-boot:run`.

## CI/CD Pipeline

- `ci.yml`: Runs on every push and pull request — build, test, dependency check
- `deploy-dev.yml`: Manual trigger — deploys to the development environment
- `deploy-prod.yml`: Manual trigger with `CONFIRM` required — deploys to production

## Deployment

Deployed on Render using Docker.

Two environments: development and production.

Database hosted on Aiven Cloud MySQL.
