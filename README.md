# Public Grievance Tracker

A full-stack Public Grievance Management System that enables 
citizens to submit complaints and track their resolution status 
in real time. Built with role-based access control for admins 
and citizens.

## Features
- Role-based access: Admin and Citizen roles
- Submit, track, and resolve complaints end-to-end
- Secure authentication with Spring Security
- Real-time complaint status tracking
- Responsive UI with Thymeleaf templates
- RESTful API design

## Tech Stack
- **Backend:** Java, Spring Boot 4.0, Spring MVC
- **Security:** Spring Security (role-based auth)
- **Database:** MySQL, Spring Data JPA, Hibernate
- **Frontend:** Thymeleaf, HTML, CSS
- **Build Tool:** Maven
- **Other:** Lombok

## Project Structure
src/
├── controller/    → Handles HTTP requests
├── service/       → Business logic layer
├── repository/    → Database operations (JPA)
├── model/         → Entity classes
└── config/        → Security configuration

## Setup & Run
```bash
# Clone the repository
git clone https://github.com/Raghav-Sharma03/public-grievance-tracker.git

# Configure MySQL in application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/grievance_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Run with Maven
./mvnw spring-boot:run
```

## API Overview
- `POST /register` — New user registration
- `POST /login` — User authentication
- `POST /grievance/submit` — Submit a complaint
- `GET /grievance/status/{id}` — Track complaint status
- `PUT /admin/grievance/{id}/resolve` — Admin resolves complaint

## Author
**Raghav Sharma** — [GitHub](https://github.com/Raghav-Sharma03) 
| [LinkedIn](https://linkedin.com/in/raghav-sharma-478191270)
