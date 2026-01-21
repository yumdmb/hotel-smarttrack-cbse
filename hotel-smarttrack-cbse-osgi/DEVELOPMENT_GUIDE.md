# Hotel SmartTrack OSGi - Development Guide

This guide provides step-by-step instructions for team members to develop their assigned OSGi bundles.

> **New to OSGi?** Don't worry! This guide explains everything you need to know. Read the "OSGi Basics for Beginners" section first!

---

## Table of Contents

1. [OSGi Basics for Beginners](#osgi-basics-for-beginners)
2. [Prerequisites](#prerequisites)
3. [Project Setup](#project-setup)
4. [Bundle Development Guide](#bundle-development-guide)
5. [Key OSGi Concepts](#key-osgi-concepts)
6. [Spring Boot vs OSGi Comparison](#comparison-spring-boot-vs-osgi)
7. [Verification Checklist](#verification-checklist)
8. [Common Issues & Solutions](#common-issues--solutions)
9. [Testing Your Bundle](#testing-your-bundle)

---

## OSGi Basics for Beginners

### What is OSGi?

**OSGi (Open Services Gateway initiative)** is a framework for building **modular Java applications**. Think of it like this:

```
┌────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT (Monolithic)                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  All code in one big application JAR                     │  │
│  │  - Controllers, Services, Repositories all bundled       │  │
│  │  - Start/stop the entire application                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                      OSGi (Modular)                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐           │
│  │ Bundle1 │  │ Bundle2 │  │ Bundle3 │  │ Bundle4 │   ...     │
│  │ (Guest) │  │ (Room)  │  │ (Stay)  │  │(Billing)│           │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘           │
│       │            │            │            │                 │
│       └────────────┴────────────┴────────────┘                 │
│                    Service Registry                            │
│       (Bundles find each other through interfaces)             │
└────────────────────────────────────────────────────────────────┘
```

### Key OSGi Terminology

| Term                 | What It Means                         | Spring Boot Equivalent     |
| -------------------- | ------------------------------------- | -------------------------- |
| **Bundle**           | A separate module/JAR file            | A package/module           |
| **Service**          | A registered interface implementation | A `@Service` class         |
| **Service Registry** | Where services are registered/found   | `ApplicationContext`       |
| **@Component**       | Marks a class as an OSGi DS component | `@Service` or `@Component` |
| **@Reference**       | Injects a service                     | `@Autowired`               |
| **@Activate**        | Method called when bundle starts      | `@PostConstruct`           |
| **@Deactivate**      | Method called when bundle stops       | `@PreDestroy`              |

### Why OSGi for This Project?

1. **True Modularity**: Each module is a separate JAR that can be started/stopped independently
2. **Loose Coupling**: Modules only know about interfaces, not implementations
3. **Dynamic**: Bundles can be added/removed/updated at runtime
4. **CBSE Demonstration**: Perfect for showing Component-Based Software Engineering

### Our Project Structure

```
hotel-smarttrack-cbse-osgi/
├── common-bundle/           ← Shared entities + interfaces (ALREADY DONE ✅)
├── guest-management-bundle/ ← Ma Wenting
├── room-management-bundle/  ← Eisraq Rejab
├── reservation-management-bundle/ ← Li Yuhang
├── stay-management-bundle/  ← Elvis Sawing
├── billing-payment-bundle/  ← Huang Di
└── application-bundle/      ← Integration (later)
```

---

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.8+**
3. **IDE**: IntelliJ IDEA or Eclipse (with Bndtools plugin recommended)

---

## Project Setup

### 1. Clone the Repository

```bash
git clone <repo-url>
cd hotel-smarttrack-cbse/hotel-smarttrack-cbse-osgi
```

### 2. Create Your Branch

```bash
git checkout -b <yourname>-<module>-osgi
# Example: git checkout -b elvis-stay-osgi
```

### 3. Build the Project

```bash
mvn clean install
```

---

## Bundle Development Guide

### Step 1: Update Your Bundle's pom.xml

Each bundle needs proper Maven configuration. Example for `stay-management-bundle`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.hotel.smarttrack.osgi</groupId>
        <artifactId>hotel-smarttrack-cbse-osgi</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>stay-management-bundle</artifactId>
    <packaging>jar</packaging>

    <name>Hotel SmartTrack - Stay Management Bundle</name>
    <description>Check-In/Check-Out Management OSGi Bundle</description>

    <dependencies>
        <!-- Common bundle for entities and interfaces -->
        <dependency>
            <groupId>com.hotel.smarttrack.osgi</groupId>
            <artifactId>common-bundle</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- OSGi Core API -->
        <dependency>
            <groupId>org.osgi</groupId>
            <artifactId>osgi.core</artifactId>
        </dependency>

        <!-- OSGi DS Annotations -->
        <dependency>
            <groupId>org.osgi</groupId>
            <artifactId>org.osgi.service.component.annotations</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>biz.aQute.bnd</groupId>
                <artifactId>bnd-maven-plugin</artifactId>
                <configuration>
                    <bnd><![CDATA[
Bundle-SymbolicName: ${project.artifactId}
Bundle-Name: ${project.name}
Bundle-Version: ${project.version}
Import-Package: \
    com.hotel.smarttrack.entity,\
    com.hotel.smarttrack.service,\
    org.osgi.service.component.annotations;resolution:=optional
Private-Package: com.hotel.smarttrack.stay
-dsannotations: *
                    ]]></bnd>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2: Create Package Structure

```
src/main/java/com/hotel/smarttrack/<your-module>/
├── <Module>Manager.java      # Service implementation
├── <Entity>Repository.java   # In-memory repository
└── (other classes)
```

### Step 3: Create In-Memory Repository

Since we don't use JPA in OSGi, create an in-memory repository:

```java
package com.hotel.smarttrack.stay;

import com.hotel.smarttrack.entity.Stay;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for Stay entities.
 * Thread-safe using ConcurrentHashMap.
 */
public class StayRepository {

    private final Map<Long, Stay> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Stay save(Stay entity) {
        if (entity.getStayId() == null) {
            entity.setStayId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getStayId(), entity);
        return entity;
    }

    public Optional<Stay> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Stay> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
```

### Step 4: Create Service Implementation with DS Annotations

```java
package com.hotel.smarttrack.stay;

import com.hotel.smarttrack.entity.*;
import com.hotel.smarttrack.service.*;
import org.osgi.service.component.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StayManager - OSGi Declarative Services implementation of StayService.
 *
 * Key annotations:
 * - @Component: Registers this as an OSGi DS component
 * - @Reference: Injects other OSGi services
 * - @Activate/@Deactivate: Lifecycle callbacks
 */
@Component(
    service = StayService.class,  // Register as StayService
    immediate = true               // Activate immediately when dependencies met
)
public class StayManager implements StayService {

    // In-memory repositories (created locally, not injected)
    private final StayRepository stayRepository = new StayRepository();
    private final IncidentalChargeRepository chargeRepository = new IncidentalChargeRepository();

    // ============ OSGi Service References ============

    /**
     * @Reference injects OSGi services similar to @Autowired
     *
     * cardinality options:
     * - MANDATORY (default): Service required, component won't activate without it
     * - OPTIONAL: Service optional, component activates even if unavailable
     * - MULTIPLE: Injects all available service implementations
     *
     * policy options:
     * - STATIC (default): Reference set at activation, doesn't change
     * - DYNAMIC: Reference can change at runtime, needs volatile field
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile RoomService roomService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile ReservationService reservationService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile GuestService guestService;

    // BillingService is optional - stay management can work without it
    @Reference(cardinality = ReferenceCardinality.OPTIONAL,
               policy = ReferencePolicy.DYNAMIC)
    private volatile BillingService billingService;

    // ============ Lifecycle Methods ============

    @Activate
    public void activate() {
        System.out.println("[StayManager] Bundle ACTIVATED");
        System.out.println("  - RoomService: " + (roomService != null ? "available" : "missing"));
        System.out.println("  - ReservationService: " + (reservationService != null ? "available" : "missing"));
        System.out.println("  - GuestService: " + (guestService != null ? "available" : "missing"));
        System.out.println("  - BillingService: " + (billingService != null ? "available" : "optional/missing"));
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[StayManager] Bundle DEACTIVATED");
    }

    // ============ Service Implementation ============

    @Override
    public Stay checkInGuest(Long reservationId) {
        // Get reservation via service interface
        Reservation reservation = reservationService.getReservationById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // Validate status
        String status = reservation.getStatus();
        if ("Cancelled".equals(status) || "No-Show".equals(status)) {
            throw new IllegalStateException("Cannot check-in: Reservation is " + status);
        }

        Room room = reservation.getAssignedRoom();
        if (room == null) {
            throw new IllegalStateException("No room assigned to reservation");
        }

        // Create stay record
        Stay stay = new Stay();
        stay.setReservation(reservation);
        stay.setGuest(reservation.getGuest());
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus("CHECKED_IN");

        // Update room status via RoomService
        roomService.updateRoomStatus(room.getRoomId(), "Occupied");

        return stayRepository.save(stay);
    }

    @Override
    public Stay checkInWalkIn(Long guestId, Long roomId) {
        Guest guest = guestService.getGuestById(guestId)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        Room room = roomService.getRoomById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        if (!"Available".equals(room.getStatus())) {
            throw new IllegalStateException("Room not available: " + room.getStatus());
        }

        Stay stay = new Stay();
        stay.setGuest(guest);
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus("CHECKED_IN");

        roomService.updateRoomStatus(roomId, "Occupied");
        return stayRepository.save(stay);
    }

    @Override
    public void checkOutGuest(Long stayId) {
        Stay stay = stayRepository.findById(stayId)
            .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        if (!"CHECKED_IN".equals(stay.getStatus())) {
            throw new IllegalStateException("Guest not checked in");
        }

        stay.setCheckOutTime(LocalDateTime.now());
        stay.setStatus("CHECKED_OUT");

        if (stay.getRoom() != null) {
            roomService.updateRoomStatus(stay.getRoom().getRoomId(), "Under Cleaning");
        }

        stayRepository.save(stay);

        // Generate invoice if billing service available
        if (billingService != null) {
            billingService.generateInvoice(stayId);
        }
    }

    @Override
    public void assignRoomAndCredentials(Long stayId, Long roomId, String keyCardNumber) {
        Stay stay = stayRepository.findById(stayId)
            .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        Room newRoom = roomService.getRoomById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // Release old room if different
        Room oldRoom = stay.getRoom();
        if (oldRoom != null && !oldRoom.getRoomId().equals(roomId)) {
            roomService.updateRoomStatus(oldRoom.getRoomId(), "Available");
        }

        stay.setRoom(newRoom);
        stay.setKeyCardNumber(keyCardNumber);
        roomService.updateRoomStatus(roomId, "Occupied");
        stayRepository.save(stay);
    }

    @Override
    public IncidentalCharge recordCharge(Long stayId, String serviceType,
                                          String description, BigDecimal amount) {
        Stay stay = stayRepository.findById(stayId)
            .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        if (!"CHECKED_IN".equals(stay.getStatus())) {
            throw new IllegalStateException("Cannot record charge: Guest not checked in");
        }

        IncidentalCharge charge = new IncidentalCharge();
        charge.setStay(stay);
        charge.setServiceType(serviceType);
        charge.setDescription(description);
        charge.setAmount(amount);
        charge.setChargeTime(LocalDateTime.now());

        return chargeRepository.save(charge);
    }

    @Override
    public List<IncidentalCharge> getChargesForStay(Long stayId) {
        return chargeRepository.findByStayId(stayId);
    }

    @Override
    public BigDecimal getOutstandingBalance(Long stayId) {
        // Implementation...
        return BigDecimal.ZERO;
    }

    @Override
    public Optional<Stay> getStayById(Long stayId) {
        return stayRepository.findById(stayId);
    }

    @Override
    public Optional<Stay> getActiveStayByRoom(String roomNumber) {
        return stayRepository.findAll().stream()
            .filter(s -> "CHECKED_IN".equals(s.getStatus()))
            .filter(s -> s.getRoom() != null && roomNumber.equals(s.getRoom().getRoomNumber()))
            .findFirst();
    }

    @Override
    public List<Stay> getActiveStays() {
        return stayRepository.findAll().stream()
            .filter(s -> "CHECKED_IN".equals(s.getStatus()))
            .toList();
    }

    @Override
    public List<Stay> getGuestStayHistory(Long guestId) {
        return stayRepository.findAll().stream()
            .filter(s -> s.getGuest() != null && guestId.equals(s.getGuest().getGuestId()))
            .toList();
    }
}
```

### Step 5: Implement Seed Data in @Activate Method

**CRITICAL:** Each bundle must load coordinated seed data to ensure cross-bundle references work!

#### Reference Document

See **[SEED_DATA_SPEC.md](SEED_DATA_SPEC.md)** for the complete seed data specification with exact IDs and code templates.

#### Overview

Since OSGi bundles use in-memory storage, data is loaded when the bundle activates and lost when Felix stops. This is actually beneficial for:

- Consistent testing and demos
- No database setup required
- Each developer has independent data
- Clean slate on every restart

#### Bundle Activation Order

Felix must activate bundles in dependency order:

```
1. guest-management-bundle       (no dependencies)
2. room-management-bundle         (no dependencies)
3. reservation-management-bundle  (depends on guest, room)
4. stay-management-bundle         (depends on guest, room, reservation)
5. billing-payment-bundle         (depends on stay)
```

#### Example: Guest Bundle (No Dependencies)

```java
@Component(service = GuestService.class, immediate = true)
public class GuestManager implements GuestService {

    private final GuestRepository guestRepository = new GuestRepository();

    @Activate
    public void activate() {
        System.out.println("[GuestManager] Activating...");
        loadSeedData();
        System.out.println("[GuestManager] Loaded " + guestRepository.findAll().size() + " guests");
    }

    private void loadSeedData() {
        // Create seed guests - these IDs must match SEED_DATA_SPEC.md
        guestRepository.save(new Guest(null, "John Doe", "john.doe@email.com",
            "+1-555-0101", "ID001", "Active", null));
        guestRepository.save(new Guest(null, "Jane Smith", "jane.smith@email.com",
            "+1-555-0102", "ID002", "Active", null));
        guestRepository.save(new Guest(null, "Bob Wilson", "bob.wilson@email.com",
            "+1-555-0103", "ID003", "Active", null));
    }
}
```

#### Example: Stay Bundle (With Dependencies)

```java
@Component(service = StayService.class, immediate = true)
public class StayManager implements StayService {

    // References to other services
    @Reference
    private volatile GuestService guestService;

    @Reference
    private volatile RoomService roomService;

    @Reference
    private volatile ReservationService reservationService;

    private final StayRepository stayRepository = new StayRepository();
    private final IncidentalChargeRepository chargeRepository = new IncidentalChargeRepository();

    @Activate
    public void activate() {
        System.out.println("[StayManager] Activating...");
        loadSeedData();
        System.out.println("[StayManager] Loaded " + stayRepository.findAll().size() + " stays");
    }

    private void loadSeedData() {
        // Get data from other services (they must be activated first!)
        Guest john = guestService.getGuestById(1L).orElseThrow();
        Room room201 = roomService.getRoomById(3L).orElseThrow();
        Reservation reservation1 = reservationService.getReservationById(1L).orElseThrow();

        // Create stay using those references
        Stay stay1 = new Stay(null, reservation1, john, room201,
            LocalDateTime.of(2026, 1, 25, 14, 0), null, "CHECKED_IN", "KC001");
        Stay savedStay = stayRepository.save(stay1);

        // Add incidental charges
        chargeRepository.save(new IncidentalCharge(null, savedStay, "Minibar",
            "2x Soda, 1x Chips", new BigDecimal("12.50"),
            LocalDateTime.of(2026, 1, 25, 20, 30)));
        chargeRepository.save(new IncidentalCharge(null, savedStay, "Room Service",
            "Dinner - Steak with salad", new BigDecimal("45.00"),
            LocalDateTime.of(2026, 1, 25, 19, 0)));
    }
}
```

#### Key Rules for Seed Data

| Rule                                     | Explanation                                             |
| ---------------------------------------- | ------------------------------------------------------- |
| **Use exact IDs from SEED_DATA_SPEC.md** | Ensures cross-bundle references work                    |
| **Load in @Activate method**             | Data loads when bundle starts                           |
| **Use @Reference to get other data**     | Never access another bundle's repository directly       |
| **Check service availability**           | Use `.orElseThrow()` to fail fast if dependency missing |
| **Print confirmation**                   | Log how many entities were loaded                       |

#### Testing Seed Data

After Felix starts all bundles, verify in the console:

```
[GuestManager] Loaded 4 guests
[RoomManager] Loaded 3 room types
[RoomManager] Loaded 5 rooms
[ReservationManager] Loaded 2 reservations
[StayManager] Loaded 1 stays
[StayManager] Loaded 2 incidental charges
[BillingManager] Loaded 1 invoices
```

#### Data Lifecycle

```
Felix starts
    ↓
Bundles activate in order
    ↓
Each @Activate loads seed data
    ↓
Services ready to use with test data
    ↓
(You use the app, create/modify data)
    ↓
Felix stops
    ↓
All in-memory data lost
    ↓
Felix restarts
    ↓
Seed data reloads (clean slate)
```

---

## Key OSGi Concepts

### 1. Bundle = Module

A bundle is the OSGi equivalent of a JAR file with special metadata in `MANIFEST.MF`:

- `Bundle-SymbolicName`: Unique identifier
- `Export-Package`: Packages visible to other bundles
- `Import-Package`: Packages required from other bundles
- `Private-Package`: Internal packages (not visible to others)

### 2. Services = Interfaces

In OSGi, services are registered in a **Service Registry**:

- Components register their services (implementations)
- Other components look up services by interface type
- This enables loose coupling between bundles

### 3. Declarative Services (DS)

DS annotations simplify service wiring:

| Annotation    | Purpose                              |
| ------------- | ------------------------------------ |
| `@Component`  | Declares a DS component              |
| `@Reference`  | Injects a service reference          |
| `@Activate`   | Called when component is activated   |
| `@Deactivate` | Called when component is deactivated |

### 4. Reference Cardinality

| Value          | Meaning                                         |
| -------------- | ----------------------------------------------- |
| `MANDATORY`    | Component won't activate without this service   |
| `OPTIONAL`     | Component activates even if service unavailable |
| `MULTIPLE`     | Injects all service implementations             |
| `AT_LEAST_ONE` | Requires at least one, can have many            |

---

## Comparison: Spring Boot vs OSGi

| Spring Boot           | OSGi DS                           |
| --------------------- | --------------------------------- |
| `@Service`            | `@Component(service = X.class)`   |
| `@Autowired`          | `@Reference`                      |
| Constructor injection | Field injection with `@Reference` |
| `@PostConstruct`      | `@Activate`                       |
| `@PreDestroy`         | `@Deactivate`                     |
| `ApplicationContext`  | Bundle Lifecycle                  |

---

## Verification Checklist

Before submitting your PR, verify:

- [ ] `mvn clean install` succeeds
- [ ] No compilation errors
- [ ] Bundle JAR contains `META-INF/MANIFEST.MF`
- [ ] Manifest has correct `Export-Package` / `Import-Package`
- [ ] All `@Reference` dependencies are on interfaces from common-bundle
- [ ] `@Activate` method prints confirmation message
- [ ] No Spring annotations used (no `@Service`, `@Autowired`, etc.)

---

## Common Issues & Solutions

### Issue: "Package X not found"

**Solution**: Ensure the package is listed in `Import-Package` in your bnd configuration.

### Issue: "Service not found for reference"

**Solution**: Check that the service bundle is installed and activated. Verify the service interface is correctly registered.

### Issue: Build fails with bnd errors

**Solution**: Check bnd instructions in pom.xml. Ensure all exported/imported packages exist.

---

## Testing Your Bundle

### Using Apache Felix

1. Download [Apache Felix](https://felix.apache.org/downloads.cgi)
2. Start Felix: `java -jar bin/felix.jar`
3. Install bundles:
   ```
   g! install file:path/to/common-bundle-1.0-SNAPSHOT.jar
   g! install file:path/to/your-bundle-1.0-SNAPSHOT.jar
   g! start <bundle-id>
   ```
4. List services:
   ```
   g! services
   ```

---
