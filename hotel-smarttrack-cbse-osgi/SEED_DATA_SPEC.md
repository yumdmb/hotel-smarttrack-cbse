# OSGi Seed Data Specification

> **What is this document?** This document defines the **initial test data** that each bundle must create when it starts. This ensures all teammates' bundles work together correctly!

---

## Understanding Seed Data in OSGi

### Why Do We Need Seed Data?

Unlike Spring Boot (which uses `data.sql` with a database), our OSGi bundles use **in-memory storage**. This means:

```
┌─────────────────────────────────────────────────────────────────┐
│                    HOW IT WORKS                                 │
│                                                                 │
│  Felix starts → Your bundle activates → @Activate runs          │
│                                              ↓                  │
│                               loadSeedData() creates test data  │
│                                              ↓                  │
│                               Data stored in ConcurrentHashMap  │
│                                              ↓                  │
│  Felix stops → Data LOST (that's okay - it reloads on restart!) │
└─────────────────────────────────────────────────────────────────┘
```

### Why This is Actually Good!

| Benefit                    | Explanation                                |
| -------------------------- | ------------------------------------------ |
| ✅ Clean slate every time  | Restart Felix → Same initial state         |
| ✅ No database setup       | No MySQL/PostgreSQL/H2 needed              |
| ✅ Independent development | Each teammate develops alone, no conflicts |
| ✅ Predictable demos       | Demo always works the same way             |
| ✅ Simple debugging        | Data issues? Just restart!                 |

### How Bundles Share Data

Bundles **do NOT** share a database. Instead:

```
┌──────────────┐    calls GuestService     ┌──────────────┐
│ stay-bundle  │ ─────────────────────────→│ guest-bundle │
│              │                           │              │
│ "I need      │   returns Guest object    │ (has Guest   │
│  Guest #1"   │ ←─────────────────────────│  in memory)  │
└──────────────┘                           └──────────────┘
```

This is why **IDs must match across bundles** - when Stay bundle asks for Guest ID 1, Guest bundle must have a Guest with ID 1!

---

## Bundle Activation Order

**CRITICAL:** Felix activates bundles based on their dependencies. If Bundle A `@Reference` Bundle B, then B must activate first!

```
1. guest-management-bundle       (no dependencies) → Activates FIRST
2. room-management-bundle         (no dependencies) → Can activate parallel with #1
3. reservation-management-bundle  (depends on guest, room) → Waits for #1 and #2
4. stay-management-bundle         (depends on guest, room, reservation) → Waits for #1, #2, #3
5. billing-payment-bundle         (depends on stay) → Waits for #4
```

OSGi DS (Declarative Services) handles this automatically! If `@Reference` services aren't available yet, your bundle waits.

---

## 1️⃣ Guest Management Bundle (Ma Wenting)

**File:** `guest-management-bundle/.../GuestManager.java`

### Seed Data

| ID  | Name        | Email                 | Phone       | ID Number | Status   |
| --- | ----------- | --------------------- | ----------- | --------- | -------- |
| 1   | John Doe    | john.doe@email.com    | +1-555-0101 | ID001     | ACTIVE   |
| 2   | Jane Smith  | jane.smith@email.com  | +1-555-0102 | ID002     | ACTIVE   |
| 3   | Bob Wilson  | bob.wilson@email.com  | +1-555-0103 | ID003     | ACTIVE   |
| 4   | Alice Brown | alice.brown@email.com | +1-555-0104 | ID004     | INACTIVE |

### Code Template

```java
@Activate
public void activate() {
    System.out.println("[GuestManager] Activating and loading seed data...");
    loadSeedData();
}

private void loadSeedData() {
    guestRepository.save(new Guest(null, "John Doe", "john.doe@email.com",
        "+1-555-0101", "ID001", "ACTIVE", null));
    guestRepository.save(new Guest(null, "Jane Smith", "jane.smith@email.com",
        "+1-555-0102", "ID002", "ACTIVE", null));
    guestRepository.save(new Guest(null, "Bob Wilson", "bob.wilson@email.com",
        "+1-555-0103", "ID003", "ACTIVE", null));
    guestRepository.save(new Guest(null, "Alice Brown", "alice.brown@email.com",
        "+1-555-0104", "ID004", "INACTIVE", "Account suspended"));

    System.out.println("[GuestManager] Loaded " + guestRepository.findAll().size() + " guests");
}
```

---

## 2️⃣ Room Management Bundle (Eisraq Rejab)

**File:** `room-management-bundle/.../RoomManager.java`

### Seed Data - Room Types

| ID  | Type Name | Description                               | Max Occupancy | Base Price | Tax Rate |
| --- | --------- | ----------------------------------------- | ------------- | ---------- | -------- |
| 1   | Standard  | Standard room with queen bed              | 2             | 100.00     | 0.10     |
| 2   | Deluxe    | Deluxe room with king bed and city view   | 2             | 150.00     | 0.10     |
| 3   | Suite     | Executive suite with separate living area | 4             | 250.00     | 0.10     |

### Seed Data - Rooms

| ID  | Room Number | Floor | Type ID | Status    |
| --- | ----------- | ----- | ------- | --------- |
| 1   | 101         | 1     | 1       | AVAILABLE |
| 2   | 102         | 1     | 1       | AVAILABLE |
| 3   | 201         | 2     | 2       | AVAILABLE |
| 4   | 202         | 2     | 2       | AVAILABLE |
| 5   | 301         | 3     | 3       | AVAILABLE |

### Code Template

```java
@Activate
public void activate() {
    System.out.println("[RoomManager] Activating and loading seed data...");
    loadSeedData();
}

private void loadSeedData() {
    // Create room types
    RoomType standard = roomTypeRepository.save(new RoomType(null, "Standard",
        "Standard room with queen bed", 2, new BigDecimal("100.00"), new BigDecimal("0.10")));
    RoomType deluxe = roomTypeRepository.save(new RoomType(null, "Deluxe",
        "Deluxe room with king bed and city view", 2, new BigDecimal("150.00"), new BigDecimal("0.10")));
    RoomType suite = roomTypeRepository.save(new RoomType(null, "Suite",
        "Executive suite with separate living area", 4, new BigDecimal("250.00"), new BigDecimal("0.10")));

    // Create rooms
    roomRepository.save(new Room(null, "101", 1, standard, "AVAILABLE"));
    roomRepository.save(new Room(null, "102", 1, standard, "AVAILABLE"));
    roomRepository.save(new Room(null, "201", 2, deluxe, "AVAILABLE"));
    roomRepository.save(new Room(null, "202", 2, deluxe, "AVAILABLE"));
    roomRepository.save(new Room(null, "301", 3, suite, "AVAILABLE"));

    System.out.println("[RoomManager] Loaded " + roomTypeRepository.findAll().size() + " room types");
    System.out.println("[RoomManager] Loaded " + roomRepository.findAll().size() + " rooms");
}
```

---

## 3️⃣ Reservation Management Bundle (Li Yuhang)

**File:** `reservation-management-bundle/.../ReservationManager.java`

### Seed Data

**IMPORTANT:** Must get guest and room data via `@Reference` services!

| ID  | Guest ID | Room Type ID | Room ID | Check-In   | Check-Out  | Guests | Status    |
| --- | -------- | ------------ | ------- | ---------- | ---------- | ------ | --------- |
| 1   | 1 (John) | 2 (Deluxe)   | 3 (201) | 2026-01-25 | 2026-01-27 | 2      | CONFIRMED |
| 2   | 2 (Jane) | 1 (Standard) | 1 (101) | 2026-01-26 | 2026-01-28 | 1      | CONFIRMED |

### Code Template

```java
@Reference
private volatile GuestService guestService;

@Reference
private volatile RoomService roomService;

private final ReservationRepository reservationRepository = new ReservationRepository();

@Activate
public void activate() {
    System.out.println("[ReservationManager] Activating and loading seed data...");
    loadSeedData();
}

private void loadSeedData() {
    // Get guests and rooms from other services
    Guest john = guestService.getGuestById(1L).orElseThrow();
    Guest jane = guestService.getGuestById(2L).orElseThrow();
    RoomType deluxe = roomService.getRoomTypeById(2L).orElseThrow();
    RoomType standard = roomService.getRoomTypeById(1L).orElseThrow();
    Room room201 = roomService.getRoomById(3L).orElseThrow();
    Room room101 = roomService.getRoomById(1L).orElseThrow();

    // Create reservations
    reservationRepository.save(new Reservation(null, john, deluxe, room201,
        LocalDate.of(2026, 1, 25), LocalDate.of(2026, 1, 27), 2, "CONFIRMED", "Late check-in requested"));
    reservationRepository.save(new Reservation(null, jane, standard, room101,
        LocalDate.of(2026, 1, 26), LocalDate.of(2026, 1, 28), 1, "CONFIRMED", null));

    System.out.println("[ReservationManager] Loaded " + reservationRepository.findAll().size() + " reservations");
}
```

---

## 4️⃣ Stay Management Bundle (Elvis Sawing - You!)

**File:** `stay-management-bundle/.../StayManager.java`

### Seed Data

**IMPORTANT:** Must get guest, room, and reservation data via `@Reference` services!

| ID  | Guest ID | Room ID | Reservation ID | Check-In Time    | Status     | Key Card |
| --- | -------- | ------- | -------------- | ---------------- | ---------- | -------- |
| 1   | 1 (John) | 3 (201) | 1              | 2026-01-25 14:00 | CHECKED_IN | KC001    |

### Code Template

```java
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
    System.out.println("[StayManager] Activating and loading seed data...");
    loadSeedData();
}

private void loadSeedData() {
    // Get data from other services
    Guest john = guestService.getGuestById(1L).orElseThrow();
    Room room201 = roomService.getRoomById(3L).orElseThrow();
    Reservation reservation1 = reservationService.getReservationById(1L).orElseThrow();

    // Create active stay
    Stay stay1 = new Stay(null, reservation1, john, room201,
        LocalDateTime.of(2026, 1, 25, 14, 0), null, "CHECKED_IN", "KC001");
    Stay savedStay = stayRepository.save(stay1);

    // Add some incidental charges
    chargeRepository.save(new IncidentalCharge(null, savedStay, "Minibar",
        "2x Soda, 1x Chips", new BigDecimal("12.50"), LocalDateTime.of(2026, 1, 25, 20, 30)));
    chargeRepository.save(new IncidentalCharge(null, savedStay, "Room Service",
        "Dinner - Steak with salad", new BigDecimal("45.00"), LocalDateTime.of(2026, 1, 25, 19, 0)));

    System.out.println("[StayManager] Loaded " + stayRepository.findAll().size() + " stays");
    System.out.println("[StayManager] Loaded " + chargeRepository.findAll().size() + " incidental charges");
}
```

---

## 5️⃣ Billing & Payment Bundle (Huang Di)

**File:** `billing-payment-bundle/.../BillingManager.java`

### Seed Data

**IMPORTANT:** Must get stay data via `@Reference StayService`!

| ID  | Stay ID | Total Amount | Amount Paid | Status |
| --- | ------- | ------------ | ----------- | ------ |
| 1   | 1       | 457.50       | 0.00        | UNPAID |

**Note:** Invoice will auto-calculate based on Stay 1:

- Room charges: 2 nights × $150 = $300.00
- Incidental charges: $12.50 + $45.00 = $57.50
- Taxes: $357.50 × 0.10 = $35.75
- **Total: $393.25**

### Code Template

> **Note:** The Invoice entity uses a simplified structure for basic payment tracking.
> Fields: `invoiceId`, `reservationId`, `stayId`, `amount`, `status`, `issuedAt`

```java
@Reference
private volatile StayService stayService;

private final InvoiceRepository invoiceRepository = new InvoiceRepository();

@Activate
public void activate() {
    System.out.println("[BillingManager] Activating and loading seed data...");
    loadSeedData();
}

private void loadSeedData() {
    // Get stay data and calculate total
    Stay stay1 = stayService.getStayById(1L).orElseThrow();
    BigDecimal totalAmount = calculateTotalCharges(1L); // Uses StayService for charges

    // Create invoice with simplified structure
    Invoice invoice = new Invoice();
    invoice.setStayId(1L);
    invoice.setAmount(totalAmount);
    invoice.setStatus("UNPAID");
    invoice.setIssuedAt(LocalDateTime.now());
    invoiceRepository.save(invoice);

    System.out.println("[BillingManager] Loaded " + invoiceRepository.findAll().size() + " invoices");
}
```

---

## Verification Checklist

After all bundles load seed data, verify:

- [ ] 4 guests exist (IDs 1-4)
- [ ] 3 room types exist (IDs 1-3)
- [ ] 5 rooms exist (IDs 1-5)
- [ ] 2 reservations exist (IDs 1-2)
- [ ] 1 active stay exists (ID 1)
- [ ] 2 incidental charges exist for Stay 1
- [ ] 1 invoice exists (ID 1) linked to Stay 1

---

## Testing Cross-Bundle References

From the Felix console or application-bundle, test:

```java
// Test that billing can get stay data
BillingService billing = ...; // get from service registry
Stay stay = billing.getInvoiceById(1L).get().getStay();
System.out.println("Stay guest: " + stay.getGuest().getName()); // Should print "John Doe"

// Test that stay can get reservation data
StayService stayService = ...;
Reservation res = stayService.getStayById(1L).get().getReservation();
System.out.println("Reservation room: " + res.getAssignedRoom().getRoomNumber()); // Should print "201"
```

---

## Important Notes

1. **Bundle activation order matters!** Felix must activate bundles in dependency order.
2. **Use `@Reference` to get data from other bundles**, never access repositories directly.
3. **All IDs must match** this specification for cross-references to work.
4. **Each developer owns only their bundle's seed data**, but references others via services.

---

## Troubleshooting Common Issues

### Issue: "Service not available" Error in @Activate

**Symptom:** `orElseThrow()` fails because service returns empty

**Cause:** The bundle you depend on hasn't activated yet (or doesn't exist)

**Solution:**

```java
// Check if services are available before loading seed data
@Activate
public void activate() {
    System.out.println("[StayManager] Activating...");
    if (guestService == null || roomService == null || reservationService == null) {
        System.out.println("[StayManager] WARNING: Required services not available!");
        return; // Skip seed data if services missing
    }
    loadSeedData();
}
```

### Issue: "Guest with ID 1 not found"

**Cause:** GuestManager used different seed data IDs than expected

**Solution:** Ensure all teammates use **exactly** the IDs specified in this document!

### Issue: Data disappears after restart

**This is expected behavior!** In-memory storage is reset every time Felix restarts. Run `loadSeedData()` in every `@Activate` method.

---

## Quick Reference: Bundle Assignments

| Bundle                          | Developer       | Creates                  | Depends On               |
| ------------------------------- | --------------- | ------------------------ | ------------------------ |
| `common-bundle`                 | Elvis (done ✅) | Entities, Interfaces     | None                     |
| `guest-management-bundle`       | Ma Wenting      | Guests 1-4               | common-bundle            |
| `room-management-bundle`        | Eisraq Rejab    | RoomTypes 1-3, Rooms 1-5 | common-bundle            |
| `reservation-management-bundle` | Li Yuhang       | Reservations 1-2         | guest, room              |
| `stay-management-bundle`        | Elvis Sawing    | Stays 1, Charges 1-2     | guest, room, reservation |
| `billing-payment-bundle`        | Huang Di        | Invoices 1               | stay                     |

---

## Quick Start Checklist

For each developer:

- [ ] 1. Pull latest `main` branch
- [ ] 2. Create your feature branch: `git checkout -b <yourname>-<module>-osgi`
- [ ] 3. Navigate to your bundle folder
- [ ] 4. Create `pom.xml` (copy from DEVELOPMENT_GUIDE.md)
- [ ] 5. Create `<Entity>Repository.java` (in-memory with ConcurrentHashMap)
- [ ] 6. Create `<Module>Manager.java` with `@Component(service = <Service>.class)`
- [ ] 7. Add `@Reference` for any services you depend on
- [ ] 8. Implement service methods
- [ ] 9. Add `loadSeedData()` in `@Activate` using **IDs from this document**
- [ ] 10. Build: `mvn clean package -pl <your-bundle>`
- [ ] 11. Verify MANIFEST.MF is generated
- [ ] 12. Push and create PR

---

## Questions?

Ask Elvis Sawing (Stay Management) or check the DEVELOPMENT_GUIDE.md for detailed OSGi explanations!
