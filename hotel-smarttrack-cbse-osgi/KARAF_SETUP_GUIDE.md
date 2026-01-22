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
├── datasource-bundle/
├── common-bundle/
├── console-bundle/
└── ...
```

---

## Step 2: Copy H2 DataSource Configuration

Copy the DataSource config file from the project's `config/` folder to Karaf's `etc/` folder:

```powershell
Copy-Item "config\org.ops4j.datasource-hoteldb.cfg" -Destination "karaf\etc\" -Force
```

> **Important**: The config file is stored in the Git-tracked `config/` folder. You must copy it to your local Karaf installation.

---

## Steps 3-5: Build, Start & Run

### 3. Build the Project

```powershell
cd hotel-smarttrack-cbse-osgi
mvn clean install -DskipTests
```

### 4. Start Karaf

```powershell
cd karaf
bin\karaf.bat
```

### 5. Install & Run (Single Command!)

In the Karaf console, copy and paste:

```
feature:repo-add file:../src/main/feature/hotel-smarttrack-features.xml
feature:install hotel-smarttrack
hotel:console
```

**That's it!** 🎉 You should see the Hotel SmartTrack main menu.

---

## Verify H2 Database

After installation, verify the H2 DataSource is available:

```
jdbc:ds-list
```

You should see `hoteldb` listed as an available DataSource.

To verify tables are created:

```
jdbc:tables jdbc/hoteldb
```

---

## Quick Reference

| Command           | Description                        |
| ----------------- | ---------------------------------- |
| `hotel:console`   | Start the Hotel SmartTrack console |
| `bundle:list`     | List all bundles                   |
| `scr:list`        | List SCR components                |
| `jdbc:ds-list`    | List available DataSources         |
| `jdbc:tables`     | Show database tables               |
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
# Then re-run the install commands from Step 5
```

### DataSource Not Available

If `jdbc:ds-list` doesn't show `hoteldb`:

1. Check config file exists: `karaf\etc\org.ops4j.datasource-hoteldb.cfg`
2. Verify features are installed:
   ```
   feature:list | grep jdbc
   feature:list | grep pax-jdbc
   ```
3. Check logs for errors:
   ```
   log:display | grep -i datasource
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

### 2. Install Required Features

```
feature:install scr
feature:install jdbc
feature:install pax-jdbc-h2
feature:install pax-jdbc-config
```

### 3. Install Bundles (in order)

Copy and paste each command one by one:

```bash
bundle:install -s file:../datasource-bundle/target/datasource-bundle-1.0-SNAPSHOT.jar
```

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
jdbc:ds-list
hotel:console
```

---

## H2 Database Details

The project uses **H2 in-memory database**. Key behavior:

- **Schema**: Created automatically on startup by `DatabaseInitializer`
- **Seed Data**: Loaded from `data.sql` if database is empty
- **Data Persistence**: Data is **NOT** persisted across Karaf restarts (in-memory mode)
- **Fresh Start**: Each restart gets fresh seed data

### DataSource Configuration

Source file (Git-tracked): `config/org.ops4j.datasource-hoteldb.cfg`

Must be copied to: `karaf/etc/org.ops4j.datasource-hoteldb.cfg`

```properties
dataSourceName=hoteldb
osgi.jndi.service.name=jdbc/hoteldb
databaseName=hoteldb
url=jdbc:h2:mem:hoteldb;DB_CLOSE_DELAY=-1
osgi.jdbc.driver.name=H2
```
