# Rooflet Core
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)

A lightweight, open-source property management API built with Spring Boot.
## Overview

Rooflet Core is a backend REST API for managing rental properties, tenants, rent collections, expenses, and portfolios. It provides a solid foundation for building property management applications.

## Features

- **Property Management**: Create and manage rental properties
- **Tenant Management**: Track tenant information and relationships
- **Rent Collection**: Record and monitor rent payments
- **Expense Tracking**: Manage property-related expenses
- **Portfolio Management**: Organize properties into portfolios with role-based access
- **User Management**: Handle user accounts and authentication
- **Feedback System**: Collect user feedback

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- Flyway (Database Migrations)
- H2 (Development)
- MySQL 8.0 (Production)
- Maven
- Docker & Docker Compose

### Development Mode

1. Clone the repository:
```bash
git clone https://github.com/rooflet/rooflet-core.git
cd rooflet-core
```

2. Run the application in development mode:
```bash
make start
```

The application will start on `http://localhost:8081` with an in-memory H2 database (`admin`/`admin`).