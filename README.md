# Smart Bank Management System

A robust, full-stack banking application designed to handle core financial operations securely. Built with a Spring Boot backend and a responsive Vanilla JavaScript frontend, this system features robust JWT-based authentication and role-based access control to ensure data privacy and secure transactions.

## 🚀 Key Features

*   **Role-Based Access Control (RBAC):** Distinct dashboards and API endpoints tailored for Admins, Clerks, and Customers.
*   **Secure Authentication:** Stateless session management utilizing JSON Web Tokens (JWT) and custom security filters for all incoming requests[cite: 1].
*   **Account Management:** Customer onboarding, account creation, and structured KYC (Know Your Customer) review processes[cite: 1].
*   **Secure Transactions:** Internal fund transfers protected by OTP (One-Time Password) verification to ensure transaction integrity[cite: 1].
*   **Loan Processing:** End-to-end loan application workflows and status tracking mechanisms[cite: 1].
*   **Global Error Handling:** Centralized exception handling to provide standardized, predictable API responses to the client-side application[cite: 1].

## 🛠️ Tech Stack

**Backend System**
*   **Language:** Java
*   **Framework:** Spring Boot, Spring Security, Spring Data JPA
*   **Database:** MySQL with Hibernate ORM
*   **Authentication:** JWT (JSON Web Tokens)
*   **Build Tool:** Maven[cite: 1]

**Frontend Interface**
*   **Core:** HTML5, CSS3, Vanilla JavaScript
*   **Integration:** Fetch API for asynchronous REST communication[cite: 2]

**Testing & Tools**
*   **API Testing:** Postman[cite: 1]
*   **Version Control:** Git & GitHub

## 📂 Project Structure

```text
Smart-Bank-Management/
├── BankManagement/             # Spring Boot Backend Application
│   ├── src/main/java/...       # Java source code (Controllers, Services, Repositories, Security)
│   ├── src/main/resources/     # Application configurations (application.properties)
│   └── pom.xml                 # Maven dependencies
└── smartbank-frontend/         # Vanilla JavaScript Frontend
    ├── css/style.css           # UI Styling
    ├── js/app.js               # Client-side routing and API integration
    └── index.html              # Main application entry point
