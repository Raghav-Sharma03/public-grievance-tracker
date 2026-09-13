-- V1: Baseline schema for Grievance Tracker
-- This captures the existing database structure

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    image_path VARCHAR(255),
    location VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_complaint_citizen FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME,
    complaint_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_comment_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indexes for frequently queried columns
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_citizen_created ON complaints(user_id, created_at DESC);
CREATE INDEX idx_comments_complaint_created ON comments(complaint_id, created_at DESC);