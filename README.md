# Library-Management-System

A Java-based, offline-first, console-driven Library Management System. Implements core data structures and file-based persistence.

## ✅ Current Features

- 🔐 **Authentication**
  - User sign-up with role support (e.g. `Librarian`, `Member`)
  - Sign-in and sign-out with session tracking
- ⚙️ **Session Management**
  - Tracks logged-in user using local file storage (`data/session.dat`)
- 🧱 Modular CLI router for easy expansion (`CommandRouter`)

---

## 🖥️ How to Use

> **Requirements:** Java 17+ and Gradle 8+

### 🔁 Running the app

```bash
./gradlew run --args="<your command>"
```

### 🔑 Example Commands

#### ➕ Sign Up (with optional role)

```bash
./gradlew run --args="auth signup -u admin -p 1234 --confirm-password 1234 --role Librarian"
```

#### 🔐 Sign In

```bash
./gradlew run --args="auth signin -u admin -p 1234"
```

#### 🔓 Sign Out

```bash
./gradlew run --args="auth signout"
```

---

## 📂 Current Project Structure

- `CommandRouter.java` - CLI entry point and dispatcher
- `AuthHandler.java` - Handles authentication-related commands

---

## 🧠 Developer Notes

### 🔧 How to Extend

To add new CLI features:

1. Add a case to `CommandRouter.java`:

   ```java
   case "book":
       BookHandler.handle(args, options);
       break;
   ```

2. Create `BookHandler.java` for book-related operations.

3. Use:

   ```java
   User currentUser = SessionService.getCurrentUser();
   ```

   ...to check if someone is logged in or determine their role.

### 🛠 Next Modules (suggested)

- `BookHandler`:

  - `add-book`
  - `borrow-book`
  - `return-book`

- `RoleValidator`:

  - Enforce RBAC (e.g., only librarians can add books)

---

## 🗃️ Data Files

Stored in the `data/` directory (auto-created if missing):

- `users.dat` — stores all registered users
- `session.dat` — stores the currently signed-in user

> These use plain text format for now — will be replaced by a real DB in future versions.

---

## 🧪 Manual Testing Guide

Try these in order:

```bash
# 1. Sign up a new user
./gradlew run --args="auth signup -u admin -p 1234 --confirm-password 1234 --role Librarian"

# 2. Sign in
./gradlew run --args="auth signin -u admin -p 1234"

# 3. Sign out
./gradlew run --args="auth signout"

# 4. Sign in with wrong password (expect failure)
./gradlew run --args="auth signin -u admin -p wrong"
```

---

## 📦 Build & Clean

```bash
# Clean previous builds
./gradlew clean

# Rebuild the project
./gradlew build
```

---

## 🙋 Getting Involved

Contributions welcome!

- Fork the repo
- Clone and make changes
- Follow the command-routing pattern (`CommandRouter`)
- Submit a pull request

---
