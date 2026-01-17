# Hotel SmartTrack CBSE

A Component-Based Software Engineering (CBSE) implementation of a Hotel Management System. This project demonstrates the same business logic implemented using two different architectural approaches: **Spring Boot** and **OSGi**.

---

## Project Overview

This repository contains two separate implementations of the Hotel SmartTrack system:

| Version     | Directory                           | Technology Stack                          | Purpose                                          |
| ----------- | ----------------------------------- | ----------------------------------------- | ------------------------------------------------ |
| Spring Boot | `hotel-smarttrack-cbse-springboot/` | Spring Boot 3.5.x, Spring Data JPA, Maven | Modern enterprise Java with dependency injection |
| OSGi        | `hotel-smarttrack-cbse-osgi/`       | Apache Felix, Bnd Maven Plugin, OSGi R8   | Modular runtime with hot-swappable bundles       |

Both versions follow the same component-based architecture and implement identical business functionality.

---

## Repository Structure

```
hotel-smarttrack-cbse/
|
|-- hotel-smarttrack-cbse-springboot/    # Spring Boot implementation
|   |-- pom.xml                          # Parent POM (multi-module)
|   |-- common/                          # Shared entities and service interfaces
|   |-- guest-management/                # Guest Management component
|   |-- room-management/                 # Room Management component
|   |-- reservation-management/          # Reservation Management component
|   |-- stay-management/                 # Check-In/Check-Out component
|   +-- billing-payment/                 # Billing and Payment component
|
|-- hotel-smarttrack-cbse-osgi/          # OSGi implementation
|   |-- pom.xml                          # Parent POM (multi-module)
|   |-- common-bundle/                   # Shared entities and service interfaces
|   |-- guest-management-bundle/         # Guest Management bundle
|   |-- room-management-bundle/          # Room Management bundle
|   |-- reservation-management-bundle/   # Reservation Management bundle
|   |-- stay-management-bundle/          # Check-In/Check-Out bundle
|   +-- billing-payment-bundle/          # Billing and Payment bundle
|
|-- project-details.md                   # Project requirements and specifications
|-- INTELLIJ_SETUP_GUIDE.md              # IDE setup instructions
+-- README.md                            # This file
```

---

## Component Architecture

Both implementations follow a shared component architecture based on CBSE principles:

### Core Components

| Component              | Responsibility                                           | Manager Class        |
| ---------------------- | -------------------------------------------------------- | -------------------- |
| Common (Base Library)  | Entity classes and service interfaces                    | N/A                  |
| Guest Management       | Guest registration, profile management, status tracking  | `GuestManager`       |
| Room Management        | Room inventory, type management, availability tracking   | `RoomManager`        |
| Reservation Management | Booking creation, room assignment, reservation lifecycle | `ReservationManager` |
| Stay Management        | Check-in/check-out processing, key card management       | `StayManager`        |
| Billing and Payment    | Invoice generation, payment processing, discounts        | `BillingManager`     |

### Entity Classes (in Common module)

- `Guest` - Hotel guest information
- `Room` - Individual room records
- `RoomType` - Room categories with pricing
- `Reservation` - Booking records
- `Stay` - Active/completed guest stays
- `Invoice` - Billing documents
- `Payment` - Payment transactions
- `IncidentalCharge` - Additional service charges

### Service Interfaces (in Common module)

- `GuestService`
- `RoomService`
- `ReservationService`
- `StayService`
- `BillingService`

---

## Spring Boot Version

### Technology Stack

- Java 17 (LTS)
- Spring Boot 3.5.9
- Spring Data JPA
- Maven (multi-module)

### Key Features

- Dependency injection via Spring Context
- JPA repositories for database access
- Transaction management with `@Transactional`
- Auto-configuration and embedded server support

### Building

```bash
cd hotel-smarttrack-cbse-springboot
mvn clean install
```

### Package Structure

```
com.hotel.smarttrack
|-- entity/          # JPA entity classes (in common module)
|-- service/         # Service interfaces (in common module)
|-- billing/         # BillingManager implementation
|-- guest/           # GuestManager implementation
|-- room/            # RoomManager implementation
|-- reservation/     # ReservationManager implementation
+-- stay/            # StayManager implementation
```

---

## OSGi Version

### Technology Stack

- Java 17 (LTS)
- OSGi R8 (Core and Compendium)
- Apache Felix Framework 7.x
- Bnd Maven Plugin 7.1.0

### Key Features

- Modular runtime with bundle lifecycle management
- Hot deployment and dynamic service binding
- Declarative Services for component registration
- Explicit import/export package declarations

### Building

```bash
cd hotel-smarttrack-cbse-osgi
mvn clean install
```

### Bundle Configuration

Each bundle contains OSGi manifest instructions:

- `Bundle-SymbolicName` - Unique bundle identifier
- `Export-Package` - Packages exposed to other bundles
- `Private-Package` - Internal implementation packages
- `Import-Package` - Required dependencies from other bundles

### Package Structure

```
com.hotel.smarttrack.osgi
|-- entity/          # Entity classes (in common-bundle)
|-- service/         # Service interfaces (in common-bundle)
|-- billing.impl/    # BillingManager implementation
|-- guest.impl/      # GuestManager implementation
|-- room.impl/       # RoomManager implementation
|-- reservation.impl/# ReservationManager implementation
+-- stay.impl/       # StayManager implementation
```

---

## Comparison: Spring Boot vs OSGi

| Aspect               | Spring Boot                          | OSGi                                |
| -------------------- | ------------------------------------ | ----------------------------------- |
| Module Type          | Maven JAR                            | OSGi Bundle                         |
| Dependency Injection | Spring DI (`@Autowired`, `@Service`) | Declarative Services (`@Component`) |
| Runtime              | Embedded Tomcat/Jetty                | Apache Felix / Eclipse Equinox      |
| Classloading         | Single classloader                   | Bundle-specific classloaders        |
| Hot Deployment       | Requires restart                     | Bundles can be updated at runtime   |
| Service Discovery    | Spring Application Context           | OSGi Service Registry               |
| Configuration        | `application.properties`             | ConfigAdmin / Metatype              |

---

## Prerequisites

### Required Software

| Software      | Version  | Notes                                          |
| ------------- | -------- | ---------------------------------------------- |
| JDK           | 17 (LTS) | Eclipse Temurin or Amazon Corretto recommended |
| Maven         | 3.6.3+   | Usually bundled with IDE                       |
| IntelliJ IDEA | 2023.x+  | Community or Ultimate edition                  |
| Apache Felix  | 7.x      | Required for OSGi runtime (optional for build) |

### IDE Setup

See [INTELLIJ_SETUP_GUIDE.md](INTELLIJ_SETUP_GUIDE.md) for detailed instructions on:

- Creating and configuring both project versions
- Installing the OSGi plugin
- Setting up Apache Felix framework
- Building and running the projects

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yumdmb/hotel-smarttrack-cbse.git
cd hotel-smarttrack-cbse
```

### Build Spring Boot Version

```bash
cd hotel-smarttrack-cbse-springboot
mvn clean install -DskipTests
```

### Build OSGi Version

```bash
cd hotel-smarttrack-cbse-osgi
mvn clean install
```

---

## Project Specifications

For detailed requirements and design specifications, see [project-details.md](project-details.md).

---

## Contributing

1. Create a feature branch from `master`
2. Make your changes
3. Ensure both Spring Boot and OSGi versions build successfully
4. Submit a pull request

### Branch Naming Convention

- `feature/component-name` - New features
- `fix/issue-description` - Bug fixes
- `refactor/description` - Code refactoring

---

## Authors

- Hotel SmartTrack Development Team

---

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [OSGi Specification](https://osgi.org/specification/)
- [Apache Felix](https://felix.apache.org/)
- [Bnd Tools](https://bnd.bndtools.org/)
