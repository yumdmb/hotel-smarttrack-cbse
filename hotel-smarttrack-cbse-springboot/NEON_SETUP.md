# Hotel SmartTrack - Database Setup

## Default Database: Neon PostgreSQL

This project uses **Neon PostgreSQL** as the default and only database.

## Quick Start

### 1. Set Up Credentials

```bash
cp .env.example .env
```

Edit `.env` with your Neon database details:

```
NEON_DB_HOST=ep-twilight-lab-a1akavj4-pooler.ap-southeast-1.aws.neon.tech
NEON_DB_NAME=neondb
NEON_DB_USER=neondb_owner
NEON_DB_PASSWORD=your_password
```

### 2. Run the Application

**VS Code:** Press `F5` → Select "Run Application (Neon)"

**IntelliJ IDEA:**

1. First, create your `.env` file in the `hotel-smarttrack-cbse-springboot` directory:
   - Copy `.env.example` to `.env`
   - Edit `.env` with your actual Neon credentials
2. Go to **Run** → **Edit Configurations...**
3. Select your Spring Boot run configuration (e.g., `HotelSmarttrackApplication`)
4. Click **Modify options** → Check **Environment variables**
5. In the Environment variables field, click the **document icon** (📄)
6. Click **Load from file** (or the folder icon)
7. Browse and select your `.env` file from the project directory
8. Click **OK** → **Apply** → **Run**

**Maven:**

```powershell
# PowerShell - set env vars first
$env:NEON_DB_HOST = "ep-twilight-lab-a1akavj4-pooler.ap-southeast-1.aws.neon.tech"
$env:NEON_DB_NAME = "neondb"
$env:NEON_DB_USER = "neondb_owner"
$env:NEON_DB_PASSWORD = "your_password"
mvn spring-boot:run
```

## Files

| File                     | Purpose                        |
| ------------------------ | ------------------------------ |
| `.env`                   | Your credentials (gitignored)  |
| `.env.example`           | Template for teammates         |
| `application.properties` | Database config using env vars |
