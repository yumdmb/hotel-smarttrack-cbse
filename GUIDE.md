# Hotel SmartTrack - Developer Guide

This guide explains how to develop and integrate your module into the Hotel SmartTrack system.

---

## Spring Boot

### Project Structure

```
hotel-smarttrack-cbse-springboot/
├── common/                    # Shared code (entities, services, repositories)
│   └── src/main/java/com/hotel/smarttrack/
│       ├── entity/            # All @Entity classes go here
│       ├── repository/        # All @Repository interfaces go here
│       └── service/           # Service interfaces go here
│
├── room-management/           # Eisraq's module
├── guest-management/          # Module for guest operations
├── reservation-management/    # Module for reservations
├── stay-management/           # Elvis's module
├── billing-payment/           # Huang Di's module
│
└── application/               # Main app that runs everything
```

### How to Develop Your Module

#### Step 1: Create Your Branch

```bash
git checkout main
git pull origin main
git checkout -b <yourname>-<module>
# Example: git checkout -b elvis-stay
```

#### Step 2: Add Entities to `common` Module

All entities MUST go in the `common` module:

```
common/src/main/java/com/hotel/smarttrack/entity/YourEntity.java
```

> **Note:** You do NOT need to write SQL! Hibernate auto-creates tables from entities.

#### Step 3: Add Repository to `common` Module

```
common/src/main/java/com/hotel/smarttrack/repository/YourRepository.java
```

#### Step 4: Implement Your Manager in Your Module

Your module contains the business logic:

```
your-module/src/main/java/com/hotel/smarttrack/yourmodule/YourManager.java
```

### How to Run

```bash
cd hotel-smarttrack-cbse-springboot
mvn clean compile
mvn spring-boot:run -pl application
```

Access H2 Console: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:hotelsmarttrack`
- Username: `sa`, Password: (empty)

### How to Integrate Your Module

1. Open `application/pom.xml`
2. Uncomment your module dependency
3. Run `mvn clean compile` to verify
4. Commit your changes

### Important Rules

| Rule                        | Description                                                  |
| --------------------------- | ------------------------------------------------------------ |
| ✅ Entities in `common`     | All `@Entity` classes must be in `common/entity/`            |
| ✅ Repositories in `common` | All `@Repository` interfaces must be in `common/repository/` |
| ✅ Managers in your module  | Your `@Component` classes go in your module                  |
| ❌ Don't commit H2 files    | `.mv.db` files are git-ignored                               |

---

## Terminal Console UI

The application uses a **terminal-based menu system**. Each module has its own console, called from a central `MainMenuConsole`.

### Architecture

```
application/
  └── MainMenuConsole.java      ← Main hub, runs on startup

your-module/
  └── YourModuleConsole.java    ← Your menu, called from MainMenu
  └── YourManager.java          ← Business logic
```

### How to Create Your Console

**Step 1:** Create console class in your module:

```java
// your-module/src/main/java/com/hotel/smarttrack/yourmodule/YourModuleConsole.java

package com.hotel.smarttrack.yourmodule;

import org.springframework.stereotype.Component;
import java.util.Scanner;

@Component
public class YourModuleConsole {

    private final YourService yourService;
    private Scanner scanner;

    public YourModuleConsole(YourService yourService) {
        this.yourService = yourService;
    }

    // This method is called by MainMenuConsole
    public void showMenu(Scanner scanner) {
        this.scanner = scanner;

        boolean running = true;
        while (running) {
            System.out.println("\n=== YOUR MODULE MENU ===");
            System.out.println("1. Option 1");
            System.out.println("2. Option 2");
            System.out.println("0. Back to Main Menu");

            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> doOption1();
                case "2" -> doOption2();
                case "0" -> running = false;  // Returns to main menu
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private void doOption1() { /* your code */ }
    private void doOption2() { /* your code */ }
}
```

**Step 2:** Tell the team lead to wire it in `MainMenuConsole`:

```java
// application/.../MainMenuConsole.java

// Add to constructor:
private final YourModuleConsole yourModuleConsole;

// Add to switch case:
case "X" -> yourModuleConsole.showMenu(scanner);
```

### Key Points

| ✅ Do                                         | ❌ Don't                              |
| --------------------------------------------- | ------------------------------------- |
| Use `showMenu(Scanner scanner)` method        | Don't implement `CommandLineRunner`   |
| Keep console in YOUR module                   | Don't put console in `application`    |
| Option "0" should exit with `running = false` | Don't call `System.exit()`            |
| Pass the scanner from MainMenu                | Don't create new `Scanner(System.in)` |

### Currently Implemented

| Module                 | Console                 | Status      |
| ---------------------- | ----------------------- | ----------- |
| Stay Management        | `StayManagementConsole` | ✅ Ready    |
| Room Management        | -                       | Not started |
| Guest Management       | -                       | Not started |
| Reservation Management | -                       | Not started |
| Billing & Payment      | -                       | Not started |

---

## OSGi

_Coming soon..._

---

## Git Workflow

1. **Always pull latest `main` before starting work**

   ```bash
   git checkout main
   git pull origin main
   git checkout <your-branch>
   git merge main
   ```

2. **Branch naming convention**: `<yourname>-<module>`
   - `elvis-stay`
   - `eisraq-room`
   - `huang_di-billing`

3. **Commit frequently with clear messages**

   ```bash
   git commit -m "feat(stay): add check-in functionality"
   ```

4. **Create Pull Request to merge into `main`**
