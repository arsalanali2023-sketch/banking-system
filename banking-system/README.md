# Banking System Backend API

A secure, RESTful Banking System backend built with **Spring Boot**, **Spring Security**, **JWT Authentication**, and **MySQL**.

## Tech Stack
- **Java 21 & Spring Boot** (REST APIs, Layered Architecture)
- **Spring Security & JWT** (Stateless Authentication)
- **Spring Data JPA & Hibernate** (ORM & Database Persistence)
- **MySQL** (Relational Database)
- **Lombok & Maven**

## Key Features
- User Registration & Login with BCrypt password hashing and JWT token generation
- Automatic 12-digit unique bank account number generation (`SAVINGS` / `CURRENT`)
- Atomic `@Transactional` Deposit, Withdrawal, and Account-to-Account Fund Transfers
- Complete Passbook / Transaction History tracking with unique reference IDs
- Global Exception Handling & Input Validation

## API Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new user & get JWT token |
| `POST` | `/api/auth/login` | Public | Login & get JWT token |
| `POST` | `/api/accounts` | Protected | Open a new bank account |
| `GET` | `/api/accounts` | Protected | Get all accounts of logged-in user |
| `GET` | `/api/accounts/{accountNumber}` | Protected | Check account details & balance |
| `POST` | `/api/transactions/deposit` | Protected | Deposit money into account |
| `POST` | `/api/transactions/withdraw` | Protected | Withdraw money from account |
| `POST` | `/api/transactions/transfer` | Protected | Transfer funds between accounts |
| `GET` | `/api/transactions/history/{accountNumber}` | Protected | View full transaction passbook |