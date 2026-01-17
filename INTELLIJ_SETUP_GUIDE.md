# IntelliJ IDEA Setup Guide for Hotel SmartTrack CBSE

This guide explains how to create both Spring Boot and OSGi projects in IntelliJ IDEA.

---

## Prerequisites

### Required Software

| Software          | Version         | Download                                                                                                                                  |
| ----------------- | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **JDK**           | Java 17 (LTS)   | [Eclipse Temurin](https://adoptium.net/) or [Amazon Corretto](https://aws.amazon.com/corretto/)                                           |
| **IntelliJ IDEA** | 2023.x or later | [JetBrains](https://www.jetbrains.com/idea/download/)                                                                                     |
| **Maven**         | 3.6.3+          | Usually bundled with IntelliJ                                                                                                             |
| **Apache Felix**  | 7.x (for OSGi)  | [Apache Felix Downloads](https://felix.apache.org/documentation/subprojects/apache-felix-framework/apache-felix-framework-downloads.html) |

---

## Part 1: Spring Boot Project Setup

### Step 1: Create New Project

1. Open IntelliJ IDEA
2. Click **File** → **New** → **Project**
3. In the left panel, select **New Project** (not from Wizard)

### Step 2: Configure Spring Boot

1. Select **Generators** → **Spring Boot** (or **Spring Initializr**)
2. Configure:

   | Field            | Value                              |
   | ---------------- | ---------------------------------- |
   | **Name**         | `hotel-smarttrack-cbse-springboot` |
   | **Location**     | `d:\Dev\hotel-smarttrack-cbse`     |
   | **Language**     | Java                               |
   | **Type**         | Maven                              |
   | **Group**        | `com.hotel.smarttrack`             |
   | **Artifact**     | `hotel-smarttrack-cbse-springboot` |
   | **Package name** | `com.hotel.smarttrack`             |
   | **JDK**          | 17                                 |
   | **Java**         | 17                                 |
   | **Packaging**    | Jar                                |

3. Click **Next**

### Step 3: Select Dependencies

Select these dependencies:

- **Spring Web** (under Web)
- **Spring Data JPA** (under SQL) - optional, for database
- **Lombok** (under Developer Tools) - optional, reduces boilerplate

Click **Create**

### Step 4: Add Sub-Modules

After project is created:

1. Right-click on project root → **New** → **Module**
2. Select **New Module** (Maven based)
3. Configure:

   | Field      | Value                              |
   | ---------- | ---------------------------------- |
   | **Name**   | `common`                           |
   | **Parent** | `hotel-smarttrack-cbse-springboot` |

4. Click **Create**

5. **Repeat for each module:**
   - `guest-management`
   - `room-management`
   - `reservation-management`
   - `stay-management`
   - `billing-payment`

### Step 5: Verify Project Structure

Your project should look like:

```
hotel-smarttrack-cbse-springboot/
├── pom.xml (parent)
├── src/main/java/... (main application)
├── common/
│   └── pom.xml
├── guest-management/
│   └── pom.xml
├── room-management/
│   └── pom.xml
├── reservation-management/
│   └── pom.xml
├── stay-management/
│   └── pom.xml
└── billing-payment/
    └── pom.xml
```

### Step 6: Build & Run

1. Open **Maven** tool window (right sidebar)
2. Click **Reload All Maven Projects** (🔄 icon)
3. Expand project → **Lifecycle** → Double-click **install**
4. Right-click main application class → **Run**

---

## Part 2: OSGi Project Setup

### Step 1: Install OSGi Plugin

1. Go to **File** → **Settings** (or `Ctrl+Alt+S`)
2. Navigate to **Plugins** → **Marketplace**
3. Search for **"OSGi"**
4. Install the OSGi plugin
5. Restart IntelliJ IDEA

### Step 2: Configure Apache Felix Framework

1. Download Apache Felix from [apache.org](https://felix.apache.org/documentation/subprojects/apache-felix-framework/apache-felix-framework-downloads.html)
2. Extract to a folder (e.g., `C:\felix`)
3. In IntelliJ: **File** → **Settings** → **Languages & Frameworks** → **OSGi**
4. Click **+** to add framework
5. Select your Felix installation folder
6. IntelliJ will detect Felix version automatically

### Step 3: Create OSGi Project

1. Click **File** → **New** → **Project**
2. Select **Maven** (not Spring Boot)
3. Configure:

   | Field          | Value                          |
   | -------------- | ------------------------------ |
   | **Name**       | `hotel-smarttrack-cbse-osgi`   |
   | **Location**   | `d:\Dev\hotel-smarttrack-cbse` |
   | **GroupId**    | `com.hotel.smarttrack.osgi`    |
   | **ArtifactId** | `hotel-smarttrack-cbse-osgi`   |

4. Click **Create**

### Step 4: Configure Parent POM for OSGi

Edit the root `pom.xml` to include:

```xml
<packaging>pom</packaging>

<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <osgi.version>8.0.0</osgi.version>
    <felix.version>7.0.5</felix.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>biz.aQute.bnd</groupId>
            <artifactId>bnd-maven-plugin</artifactId>
            <version>7.0.0</version>
        </plugin>
    </plugins>
</build>
```

### Step 5: Add OSGi Bundle Modules

1. Right-click on project → **New** → **Module**
2. Select **Maven**
3. Create modules with these names:
   - `common-bundle`
   - `guest-management-bundle`
   - `room-management-bundle`
   - `reservation-management-bundle`
   - `stay-management-bundle`
   - `billing-payment-bundle`

4. For each module `pom.xml`, set packaging to bundle:

```xml
<packaging>bundle</packaging>
```

### Step 6: Build OSGi Bundles

1. Open **Maven** tool window
2. Click **Reload All Maven Projects**
3. Run **mvn clean install**
4. Check each module's `target/` folder for JAR files with proper OSGi `MANIFEST.MF`

---

## Quick Reference: Key Differences

| Aspect                    | Spring Boot              | OSGi                                               |
| ------------------------- | ------------------------ | -------------------------------------------------- |
| **Module Type**           | Standard Maven module    | Bundle (`<packaging>bundle</packaging>`)           |
| **Dependency Management** | Spring DI (`@Autowired`) | OSGi Service Registry                              |
| **Runtime**               | Embedded Tomcat          | Apache Felix / Karaf                               |
| **Module Communication**  | Direct Java calls        | OSGi Services (`Export-Package`, `Import-Package`) |
| **Hot Deployment**        | Need restart             | Hot swap bundles at runtime                        |

---

## Troubleshooting

### Spring Boot Issues

| Problem                | Solution                                           |
| ---------------------- | -------------------------------------------------- |
| Module not recognized  | Right-click project → Maven → Reload Project       |
| Cannot find main class | Check `@SpringBootApplication` annotation exists   |
| Port 8080 in use       | Add `server.port=8081` in `application.properties` |

### OSGi Issues

| Problem                      | Solution                                                  |
| ---------------------------- | --------------------------------------------------------- |
| Bundle packaging not working | Ensure Bnd Maven Plugin is configured                     |
| Plugin not found             | Check IntelliJ OSGi plugin is installed                   |
| Missing MANIFEST.MF          | Verify `bnd.bnd` file exists in module or use annotations |

---

## Next Steps

After project structure is set up:

1. We'll create the entity classes in `common` / `common-bundle`
2. Define service interfaces
3. Implement manager classes in each module
4. Connect modules together
