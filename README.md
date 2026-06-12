# Library Management System

Welcome to the Library Management System project! This application has been refactored from a monolithic architecture into a professional **3-Layer Architecture** (Presentation, Service, Data Access/Repository). It is built using **JavaFX** for the graphical user interface and **Microsoft SQL Server** for the database, managed via **Maven**.

## 🏗️ Architecture Overview

The codebase is organized into distinct layers to separate concerns, making it easier to maintain, scale, and test:

1. **Presentation Layer (`src/controller`, `src/views`)**
   - Contains JavaFX controllers and FXML views.
   - Responsible for UI rendering and user interactions.
   - **No SQL queries exist here**. Controllers strictly communicate with the Service Layer.

2. **Business Logic Layer (`src/service`)**
   - Contains service classes (`BookService`, `MemberService`, `InvoiceService`, `AuthService`).
   - Handles all business rules, validations, and complex transactions (e.g., creating an invoice while updating book quantities).

3. **Data Access Layer / Repository (`src/repository`)**
   - Contains repositories (`BookRepository`, `MemberRepository`, `InvoiceRepository`, `AccountRepository`).
   - Directly interacts with the SQL Server Database.
   - Manages CRUD operations using secure `PreparedStatement`s and `try-with-resources` to prevent connection leaks.

4. **Configuration (`src/config`)**
   - `DatabaseConfig.java` loads connection properties from `config.properties`, keeping your database credentials secure and out of the source code.

## ⚙️ Prerequisites

To run this project, you need the following installed on your machine:
- **Java Development Kit (JDK) 11 or 17** (Java 17 recommended for JavaFX).
- **Apache Maven** (for dependency management and building).
- **Microsoft SQL Server** (Express or Developer edition).

## 🚀 Getting Started

### 1. Database Setup
1. Open Microsoft SQL Server Management Studio (SSMS).
2. Execute the `LibraryManagementDB.sql` script to create the database schema, tables, policies, and initial data.
3. Note: The `trg_CalculateInvoiceTotal` trigger using cursors is now obsolete since the transaction logic is handled in `InvoiceService.java`.

### 2. Configure Database Connection
1. Open the file `src/config.properties`.
2. Update the credentials to match your SQL Server setup:
   ```properties
   db.url=jdbc:sqlserver://YOUR_SERVER_NAME:1433;databaseName=Library;encrypt=true;trustServerCertificate=true;
   db.user=sa
   db.password=your_password_here
   ```

### 3. Running the Application

You can run the application in two ways:

#### Option A: Using Maven (Command Line)
Open your terminal in the project root directory and run:
```bash
mvn clean javafx:run
```
*(This will compile the project, download the necessary JDBC and JavaFX dependencies, and launch the application).*

#### Option B: Using Visual Studio Code / Eclipse
1. Ensure you have the **Extension Pack for Java** installed in VS Code.
2. Open the `LibraryProject` folder.
3. The project will automatically be recognized as a Maven project (thanks to `pom.xml`).
4. Locate `src/controller/App.java` (or simply `App.java` if it's in the default package).
5. Click the **Run** button provided by the IDE above the `main` method.

## 🔐 Default Login Credentials
- **Manager (Admin):** 
  - Username: `admin`
  - Password: `123` (Note: Passwords should be hashed in a production environment).

Enjoy building and extending your professional JavaFX application!
