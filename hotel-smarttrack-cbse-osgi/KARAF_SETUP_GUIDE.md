# Karaf Setup Guide (For Teammates)

This guide explains how to set up and run the Hotel SmartTrack OSGi project using the provided Karaf folder.

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

## Step 2: Build the Project

Open PowerShell/Terminal and run:

```powershell
cd hotel-smarttrack-cbse-osgi
mvn clean install -DskipTests
```

Wait for `BUILD SUCCESS`.

---

## Step 3: Start Karaf

```powershell
cd karaf
bin\karaf.bat
```

You should see the Karaf welcome screen.

---

## Step 4: Install SCR Feature

In the Karaf console, run:

```
feature:install scr
```

---

## Step 5: Install Bundles

Copy and paste this entire block:

```bash
bundle:install -s file:../common-bundle/target/common-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../guest-management-bundle/target/guest-management-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../room-management-bundle/target/room-management-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../reservation-management-bundle/target/reservation-management-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../stay-management-bundle/target/stay-management-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../billing-payment-bundle/target/billing-payment-bundle-1.0-SNAPSHOT.jar
bundle:install -s file:../console-bundle/target/console-bundle-1.0-SNAPSHOT.jar
```

---

## Step 6: Verify Installation

```
bundle:list
```

All Hotel SmartTrack bundles should show `Active` status.

---

## Step 7: Run the Console

```
hotel:console
```

You should see the Hotel SmartTrack main menu!

---

## Quick Reference

| Command           | Description         |
| ----------------- | ------------------- |
| `bundle:list`     | List all bundles    |
| `scr:list`        | List SCR components |
| `log:tail`        | View logs           |
| `system:shutdown` | Stop Karaf          |

---

## Troubleshooting

### Reset Karaf (Start Fresh)

```powershell
# Stop Karaf first (Ctrl+C or 'logout')
Remove-Item -Recurse -Force karaf\data
bin\karaf.bat
```

### Bundle Not Starting

```
bundle:diag <bundle-id>
```

### After Code Changes

Rebuild and update the bundle:

```powershell
# In another terminal:
mvn install -DskipTests -pl console-bundle
```

Then in Karaf:

```
bundle:update <bundle-id> file:../console-bundle/target/console-bundle-1.0-SNAPSHOT.jar
```
