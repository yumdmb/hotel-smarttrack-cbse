package com.hotel.smarttrack.datasource;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.Collectors;

/**
 * Database Initializer Component.
 * 
 * This OSGi component:
 * 1. Creates an H2 in-memory DataSource
 * 2. Registers it as an OSGi service
 * 3. Initializes schema from schema.sql
 * 4. Loads seed data from data.sql
 * 
 * This self-contained approach bypasses pax-jdbc-config.
 */
@Component(immediate = true)
public class DatabaseInitializer {

    private JdbcDataSource dataSource;
    private ServiceRegistration<DataSource> serviceRegistration;

    @Activate
    public void activate(BundleContext context) {
        System.out.println("==============================================");
        System.out.println("[DatabaseInitializer] Bundle ACTIVATING...");
        System.out.println("==============================================");

        try {
            // Step 1: Create H2 DataSource
            dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:hoteldb;DB_CLOSE_DELAY=-1");
            dataSource.setUser("sa");
            dataSource.setPassword("");

            System.out.println("[DatabaseInitializer] H2 DataSource created ✓");

            // Step 2: Register as OSGi service with JNDI name
            Dictionary<String, Object> props = new Hashtable<>();
            props.put("osgi.jndi.service.name", "jdbc/hoteldb");
            props.put("dataSourceName", "hoteldb");

            serviceRegistration = context.registerService(DataSource.class, dataSource, props);
            System.out.println("[DatabaseInitializer] DataSource registered as OSGi service ✓");
            System.out.println("  - Service property: osgi.jndi.service.name=jdbc/hoteldb");

            // Step 3: Initialize database
            try (Connection conn = dataSource.getConnection()) {
                // Create schema
                boolean schemaSuccess = executeScript(conn, "schema.sql");
                if (schemaSuccess) {
                    System.out.println("[DatabaseInitializer] Schema created ✓");
                } else {
                    System.err.println("[DatabaseInitializer] Schema creation FAILED!");
                }

                // Load seed data if tables exist and are empty
                if (schemaSuccess && isDataEmpty(conn)) {
                    boolean dataSuccess = executeScript(conn, "data.sql");
                    if (dataSuccess) {
                        System.out.println("[DatabaseInitializer] Seed data loaded ✓");
                    } else {
                        System.err.println("[DatabaseInitializer] Seed data loading FAILED!");
                    }
                } else if (!schemaSuccess) {
                    System.err.println("[DatabaseInitializer] Skipping seed data due to schema failure");
                } else {
                    System.out.println("[DatabaseInitializer] Data already exists, skipping seed");
                }

                // Log table counts
                logTableCounts(conn);
            }

            System.out.println("==============================================");
            System.out.println("[DatabaseInitializer] Bundle ACTIVATED ✓");
            System.out.println("==============================================");

        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[DatabaseInitializer] Deactivating...");

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            System.out.println("[DatabaseInitializer] DataSource service unregistered");
        }

        System.out.println("[DatabaseInitializer] Deactivated");
    }

    /**
     * Execute a SQL script from resources.
     * Returns true if successful, false if resource not found or error.
     */
    private boolean executeScript(Connection conn, String resourceName) {
        // Try multiple ways to load the resource
        InputStream is = null;

        // Method 1: Class classloader
        is = getClass().getClassLoader().getResourceAsStream(resourceName);
        System.out.println("[DatabaseInitializer] Loading " + resourceName + " via classloader: "
                + (is != null ? "FOUND" : "NOT FOUND"));

        // Method 2: Class getResourceAsStream (relative path)
        if (is == null) {
            is = getClass().getResourceAsStream("/" + resourceName);
            System.out.println("[DatabaseInitializer] Loading " + resourceName + " via class (absolute): "
                    + (is != null ? "FOUND" : "NOT FOUND"));
        }

        // Method 3: Thread context classloader
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            System.out.println("[DatabaseInitializer] Loading " + resourceName + " via thread context: "
                    + (is != null ? "FOUND" : "NOT FOUND"));
        }

        if (is == null) {
            System.err.println("[DatabaseInitializer] CRITICAL: Resource not found: " + resourceName);
            System.err.println("[DatabaseInitializer] ClassLoader: " + getClass().getClassLoader());
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String sql = reader.lines().collect(Collectors.joining("\n"));
            System.out.println("[DatabaseInitializer] Loaded " + resourceName + " (" + sql.length() + " chars)");

            if (sql.trim().isEmpty()) {
                System.err.println("[DatabaseInitializer] WARNING: " + resourceName + " is empty!");
                return false;
            }

            // Normalize line endings (CRLF -> LF)
            sql = sql.replace("\r\n", "\n").replace("\r", "\n");

            // Remove SQL comment lines (lines starting with --)
            // This prevents statements with leading comments from being skipped
            StringBuilder cleanedSql = new StringBuilder();
            for (String line : sql.split("\n")) {
                String trimmedLine = line.trim();
                // Skip pure comment lines, but keep inline content
                if (!trimmedLine.startsWith("--")) {
                    cleanedSql.append(line).append("\n");
                }
            }

            // Split by semicolon and execute each statement
            int successCount = 0;
            int errorCount = 0;

            for (String statement : cleanedSql.toString().split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                        successCount++;
                    } catch (SQLException e) {
                        // Log actual errors (not just "already exists")
                        if (!e.getMessage().contains("already exists")) {
                            System.err.println("[DatabaseInitializer] SQL Error executing: "
                                    + trimmed.substring(0, Math.min(50, trimmed.length())) + "...");
                            System.err.println("[DatabaseInitializer] Error: " + e.getMessage());
                            errorCount++;
                        }
                    }
                }
            }

            System.out.println(
                    "[DatabaseInitializer] Executed " + successCount + " statements, " + errorCount + " errors");
            return errorCount == 0;

        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error reading " + resourceName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the guests table is empty (indicator that seed data is needed).
     */
    private boolean isDataEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM guests")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            // Table might not exist yet
            System.err.println("[DatabaseInitializer] isDataEmpty check failed: " + e.getMessage());
            return true;
        }
        return true;
    }

    /**
     * Log the count of records in each table for verification.
     */
    private void logTableCounts(Connection conn) {
        String[] tables = { "guests", "room_types", "rooms", "reservations", "stays", "incidental_charges",
                "invoices" };

        System.out.println("[DatabaseInitializer] Table counts:");
        for (String table : tables) {
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                if (rs.next()) {
                    System.out.println("  - " + table + ": " + rs.getInt(1));
                }
            } catch (SQLException e) {
                System.out.println("  - " + table + ": ERROR - " + e.getMessage());
            }
        }
    }
}
