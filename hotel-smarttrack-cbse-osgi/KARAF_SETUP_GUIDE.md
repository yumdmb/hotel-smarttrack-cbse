# Karaf Setup Guide (For Teammates)

This guide explains how to set up and run the Hotel SmartTrack OSGi project using Apache Karaf.

---

## Prerequisites

- **Java 17+** installed and in PATH
- **Maven 3.8+** installed and in PATH

---

## Step 1: Extract Karaf

1. Get the `karaf.zip` file
2. Extract the `karaf/` folder into the OSGi project directory:

```
hotel-smarttrack-cbse-osgi/
├── karaf/              ← Extract here
│   ├── bin/
│   ├── etc/
│   └── ...
├── common-bundle/
├── console-bundle/
└── ...
```

---

## Steps 2-4: Build, Start & Run

### 2. Build the Project

```powershell
cd hotel-smarttrack-cbse-osgi
mvn clean install -DskipTests
```

### 3. Start Karaf

```powershell
cd karaf
bin\karaf.bat
```

### 4. Install & Run (Single Command!)

In the Karaf console, copy and paste:

```
feature:repo-add file:../src/main/feature/hotel-smarttrack-features.xml
feature:install hotel-smarttrack
hotel:console
```

**That's it!** 🎉 You should see the Hotel SmartTrack main menu.

---

## Quick Reference

| Command           | Description                        |
| ----------------- | ---------------------------------- |
| `hotel:console`   | Start the Hotel SmartTrack console |
| `bundle:list`     | List all bundles                   |
| `scr:list`        | List SCR components                |
| `log:tail`        | View logs                          |
| `feature:list`    | List installed features            |
| `system:shutdown` | Stop Karaf                         |

---

## Troubleshooting

### Reset Everything (Start Fresh)

```powershell
# Stop Karaf first (Ctrl+D or 'logout')
Remove-Item -Recurse -Force karaf\data
bin\karaf.bat
# Then re-run the install commands from Step 3
```

### Bundle Not Starting

```
bundle:diag <bundle-id>
scr:list
```

### After Code Changes

```powershell
# In another terminal:
mvn install -DskipTests

# In Karaf console:
feature:uninstall hotel-smarttrack
feature:install hotel-smarttrack
```

---

## Alternative: Manual Bundle Installation

If you prefer to install bundles one by one instead of using the feature file:

### 1. Start Karaf

```powershell
cd karaf
bin\karaf.bat
```

### 2. Install SCR Feature

```
feature:install scr
```

### 3. Install Bundles (in order)

Copy and paste each command one by one:

```bash
bundle:install -s file:../common-bundle/target/common-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../guest-management-bundle/target/guest-management-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../room-management-bundle/target/room-management-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../reservation-management-bundle/target/reservation-management-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../stay-management-bundle/target/stay-management-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../billing-payment-bundle/target/billing-payment-bundle-1.0-SNAPSHOT.jar
```

```bash
bundle:install -s file:../console-bundle/target/console-bundle-1.0-SNAPSHOT.jar
```

### 4. Verify & Run

```
bundle:list
hotel:console
```
