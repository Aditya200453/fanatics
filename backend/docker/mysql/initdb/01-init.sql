/* =========================================================================
FILE: docker/mysql/initdb/01-init.sql

PURPOSE:
- Creates 2 databases: cms and auth_db
- Creates all tables for CMS (9 tables)
- Creates users table for auth_db + inserts default admin user

NOTES:
- This runs ONLY on first initialization when /var/lib/mysql is empty.
- If you already have mysql_data volume, this won't run again unless you remove volume.
======================================================================== */

-- ----------------------------
-- 1) Create Databases
-- ----------------------------
CREATE DATABASE IF NOT EXISTS cms;
CREATE DATABASE IF NOT EXISTS auth_db;

-- ----------------------------
-- 2) CMS Schema (9 tables)
-- Source: your schema (matches your existing init script)
-- ----------------------------
USE cms;

CREATE TABLE IF NOT EXISTS patient (
  patient_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  age INT NOT NULL,
  dob DATE,
  gender VARCHAR(10),
  phone VARCHAR(15) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  address VARCHAR(255),
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS speciality (
  speciality_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctor (
  doctor_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  experience INT NOT NULL,
  qualification VARCHAR(100),
  phone VARCHAR(15),
  email VARCHAR(100) UNIQUE NOT NULL,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS speciality_doctor_map (
  speciality_id INT NOT NULL,
  doctor_id INT NOT NULL,
  PRIMARY KEY (speciality_id, doctor_id),
  FOREIGN KEY (speciality_id) REFERENCES speciality(speciality_id),
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
);

CREATE TABLE IF NOT EXISTS appointment (
  appointment_id INT AUTO_INCREMENT PRIMARY KEY,
  patient_id INT NOT NULL,
  doctor_id INT NOT NULL,
  appointment_date DATE NOT NULL,
  appointment_time TIME NOT NULL,
  status VARCHAR(20) DEFAULT 'BOOKED',
  symptoms VARCHAR(255),
  remarks VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id)
);

CREATE TABLE IF NOT EXISTS diagnostic_test (
  test_id INT AUTO_INCREMENT PRIMARY KEY,
  test_name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255),
  cost DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patient_test (
  patient_id INT NOT NULL,
  test_id INT NOT NULL,
  test_date DATE NOT NULL,
  result VARCHAR(255),
  status VARCHAR(20) DEFAULT 'PENDING',
  PRIMARY KEY (patient_id, test_id),
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
  FOREIGN KEY (test_id) REFERENCES diagnostic_test(test_id)
);

CREATE TABLE IF NOT EXISTS prescription (
  prescription_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id INT NOT NULL,
  prescription_date DATE NOT NULL,
  diagnosis VARCHAR(255),
  notes VARCHAR(500),
  FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id)
);

CREATE TABLE IF NOT EXISTS prescription_medicine (
  prescription_medicine_id INT AUTO_INCREMENT PRIMARY KEY,
  prescription_id INT NOT NULL,
  medicine_name VARCHAR(100) NOT NULL,
  dosage VARCHAR(50),
  duration VARCHAR(50),
  instructions VARCHAR(255),
  FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id)
);

-- ----------------------------
-- 3) AUTH_DB Schema (users table)
-- Source: Auth_DB_Schema.docx (includes STAFF + PENDING)
-- ----------------------------
USE auth_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('ADMIN', 'DOCTOR', 'PATIENT', 'STAFF') NOT NULL,
  status ENUM('ACTIVE', 'PENDING', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- default admin user (bcrypt already in your schema doc)
INSERT INTO users (email, password, role, status)
VALUES
('admin@clinic.com',
--  '$2a$10$7QF3z5T7l6XvZJqZKpGzJOtP5KkS9e1qz0f1vB5n9LQwQ1JtCq5Jm',
 '$2a$10$zEXRSbr.Ds4TRY0dvhOnCuCvlSBP9B2nbZZkBi1QsO/xt2k01lEgq',
 'ADMIN',
 'ACTIVE');