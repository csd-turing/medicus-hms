# 🏥 Medicus HMS — Hospital Management System

Medicus HMS is a **Spring Boot–based Hospital Management System** designed to manage patients, appointments, medical records, and related healthcare operations.
This project follows clean architecture practices with **Controller → Service → Repository → Entity** layers and includes **unit tests + golden test rules compliance**.

---

## ✅ Features

| Module             | Features                                  |
| ------------------ | ----------------------------------------- |
| Patient Management | Create, view, update, list patients       |
| Search             | Search patients by name or mobile number  |
| Validation         | Custom patient validation logic           |
| DTO & Mapper       | DTO mapping using manual mapper class     |
| Tests              | JUnit + Mockito with F2P & P2P compliance |
| REST APIs          | Fully documented REST endpoints           |

---

## 📂 Project Structure

```
src/main/java/com/csd/medicus
  ├── controller     # REST controllers
  ├── service        # Business logic
  ├── repository     # Spring Data JPA repositories
  ├── model          # JPA Entities
  ├── dto            # Data Transfer Objects
  ├── mapper         # Mapper classes
  └── validator      # Validation
```

---

## ⚙️ Tech Stack

* **Java 17**
* **Spring Boot**
* **Spring Data JPA**
* **H2 / MySQL**
* **JUnit 5 + Mockito**
* **Maven**

---

## 🚀 Running the Project

### **1️⃣ Clone Repo**

```bash
git clone https://github.com/csd-turing/medicus-hms.git
cd medicus-hms
```

### **2️⃣ Build**

```bash
mvn clean install
```

### **3️⃣ Run**

```bash
mvn spring-boot:run
```

---

## 🧪 Running Tests

```bash
mvn test
```

All **F2P** and **P2P** tests must pass before merge ✅

---

## 📡 API Endpoints

| Method | Endpoint                         | Description       |
| ------ | -------------------------------- | ----------------- |
| POST   | `/api/v1/patients`               | Create patient    |
| GET    | `/api/v1/patients/{id}`          | Get patient by ID |
| GET    | `/api/v1/patients/search?query=` | Search patients   |

### Example Search Call

```bash
GET /api/v1/patients/search?query=ram
```

---

## 🧾 Contribution Guidelines

### Issue Format

```
Fixes #<issue-number>
```

### PR Rules ✅

* Link to issue using: `Fixes #X`
* Include tests (F2P & P2P)
* Provide PR description

---

## 👨‍💻 Maintainers

| Name            | Role         |
| --------------- | ------------ |
| CSD Turing Team | Dev & Review |

---

### ⭐ Support

If you find this project useful, give it a ⭐ to support development.
