![Deme](./demo.png)

# EbenLib Library Management System

A Java‑based, offline‑first, console‑driven library management CLI. Written in pure Java, no external DB, data persisted to CSV/text files.

---

## 🚀 Quick Start

### Requirements

- Java 17+ (or your GraalVM distribution set as `JAVA_HOME`)
- Gradle 8+

### How to run

For dev, use the utility psh script to run

```psh
  ./run.ps1 --interactive

  # For help.

  .\run.ps1 --help

```

### Install as a Global Command

1. Build your native image or Jar:

   ```bash
   ./gradlew build
   ```

2. (Optional, for Jar) Create a launcher script named `ebenlib`:

   ```bash
   #!/usr/bin/env bash
   java -jar /full/path/to/ebenlib.jar "$@"
   ```

3. Make it executable and move it into your `$PATH`:

   ```bash
   chmod +x ebenlib
   sudo mv ebenlib /usr/local/bin/
   ```

Now you can run:

```bash
ebenlib --help
ebenlib auth signin
ebenlib --interactive
```

---

## 📚 Command Reference

```text
ebenlib <command> [options]
```

### Core Commands

| Command            | Description                                                       |
| ------------------ | ----------------------------------------------------------------- |
| `--interactive`    | Launch the interactive, menu‑driven UI                            |
| `auth signin`      | Sign in to your account                                           |
| `auth signup`      | Register a new Reader or Librarian                                |
| `auth signout`     | Sign out of the current session                                   |
| `user list`        | List all users (Librarian only)                                   |
| `user delete`      | Delete a user account                                             |
| `user promote`     | Promote a user to Librarian                                       |
| `user demote`      | Demote a Librarian to Reader                                      |
| `user deactivate`  | Suspend a user’s account                                          |
| `user activate`    | Reactivate a suspended user                                       |
| `book add`         | Add a book to inventory (Librarian only)                          |
| `book update`      | Update book details                                               |
| `book delete`      | Remove a book from inventory                                      |
| `book list`        | List all books                                                    |
| `book search`      | Search books by title/author/ISBN                                 |
| `book stats`       | Show stats for a given book (times borrowed, overdue count, etc.) |
| `borrow request`   | Request to borrow a book                                          |
| `borrow approve`   | Approve a borrow request (Librarian only)                         |
| `borrow reject`    | Reject a borrow request (Librarian only)                          |
| `borrow return`    | Return a borrowed book                                            |
| `borrow list`      | List all pending borrow requests                                  |
| `borrow history`   | Show your personal borrowing history                              |
| `profile view`     | View your user profile                                            |
| `profile update`   | Update your username                                              |
| `profile password` | Change your password                                              |
| `system seed`      | Initialize or reset system data (Librarian only)                  |
| `system import`    | allows to get in data from external sources                       |
| `system export`    | allows for backing up data                                        |
| `system config`    | allows for configuring some system functions                      |
| `report views`     | Report: summary stats                                             |
| `report books`     | Report: book report stats                                         |
| `report borrows`   | Report: borrow stats                                              |
| `report users`     | Report: user stats                                                |
| `test`             | Run the built‑in console UI tests                                 |
| `--help`, `-h`     | Show this help message                                            |

---

### 🔧 Examples

```bash
# Sign up new Librarian
ebenlib auth signup

# Sign in
ebenlib auth signin

# Add a new book
ebenlib book add --title="1984" --author="Orwell" --copies=5

# Search books
ebenlib book search --title="Potter"

# Borrow a book
ebenlib borrow request --book-id=42

# Run the interactive menu
ebenlib --interactive
```

---

## 🗂 Project Structure

```java

./Library-Management-System/*
        ├─ app/*
        |       ├─ src/*
        |       |       ├─ main/*
        |       |       |       ├─ java/*
        |       |       |       |       └─ org/*
        |       |       |       |               └─ ebenlib/*
        |       |       |       |               ├─ cli/*
        |       |       |       |               |       ├─ AuthHandler.java
        |       |       |       |               |       ├─ CommandRouter.java
        |       |       |       |               |       ├─ ConsoleThemeTest.java
        |       |       |       |               |       ├─ ConsoleUI.java
        |       |       |       |               |       ├─ InteractiveMenus.java
        |       |       |       |               |       ├─ InteractiveShell.java
        |       |       |       |               |       ├─ TablePrinter.java
        |       |       |       |               |       └─ User.java
        |       |       |       |               └─ App.java
        |       |       |       └─ resources/*
        |       |       |               ├─ session.txt
        |       |       |               └─ users.csv
        |       |       └─ test/*
        |       |               ├─ java/*
        |       |               |       └─ org/*
        |       |               |               └─ ebenlib/*
        |       |               |               └─ AppTest.java
        |       |               └─ resources/*
        |       └─ build.gradle
        ├─ gradle/*
        |       ├─ wrapper/*
        |       |       ├─ gradle-wrapper.jar
        |       |       └─ gradle-wrapper.properties
        |       └─ libs.versions.toml
        ├─ .gitattributes
        ├─ .gitignore
        ├─ demo.png
        ├─ gradle.properties
        ├─ gradlew
        ├─ gradlew.bat
        ├─ README.md
        ├─ run.ps1
        └─ settings.gradle

```

---

## 🌱 Developer Notes

- **Adding new commands**:

  1. Extend `CommandRouter.route(...)`.
  2. Implement a new handler class (e.g. `BookHandler`).
  3. Wire it in `CommandRouter` and add stubs in `InteractiveMenus`.

- **Interactive UI**:
  Uses ANSI colors, pagination, spinners—fully themable via `ConsoleTheme`.

- **Native packaging**:
  For lightning‑fast startup, use GraalVM’s `native-image` to compile into a standalone `ebenlib` binary. See [GraalVM docs](https://www.graalvm.org/).

---

## ✅ What’s Next

- Implement actual logic behind the **stubs** (`BookHandler`, `BorrowHandler`, etc.)
- Add **JUnit tests** under `/test` to cover each service
- Integrate **fine calculation**, **overdue monitoring**, and **report generation**

---

## 🤝 Contributing

1. Fork this repo
2. Create your feature branch (`git checkout -b feature/xyz`)
3. Commit your changes (`git commit -m "feat: add XYZ"`)
4. Push to your branch (`git push origin feature/xyz`)
5. Open a Pull Request

Happy coding! 🚀
