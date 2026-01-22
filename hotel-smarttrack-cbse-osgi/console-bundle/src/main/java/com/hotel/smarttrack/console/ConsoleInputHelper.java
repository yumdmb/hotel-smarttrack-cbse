package com.hotel.smarttrack.console;

import org.apache.karaf.shell.api.console.Session;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Console Input Helper - Provides Karaf-aware input handling with proper echo.
 * 
 * This utility properly handles input in the Karaf terminal environment,
 * ensuring characters are echoed as they are typed.
 */
public class ConsoleInputHelper {

    private final Session session;
    private final PrintStream out;
    private final BufferedReader reader;

    // Track if we need to skip a \n at the start of next read (for Windows \r\n
    // handling)
    private boolean skipNextLF = false;

    /**
     * Create a ConsoleInputHelper using Karaf's Session.
     * 
     * @param session The Karaf shell Session (can be null for fallback mode)
     */
    public ConsoleInputHelper(Session session) {
        this.session = session;

        if (session != null) {
            this.out = session.getConsole();
            // Use keyboard but we'll handle echo manually
            this.reader = new BufferedReader(new InputStreamReader(session.getKeyboard()));
        } else {
            this.out = System.out;
            this.reader = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    /**
     * Print text without newline.
     */
    public void print(String text) {
        out.print(text);
        out.flush();
    }

    /**
     * Print text with newline.
     */
    public void println(String text) {
        out.println(text);
    }

    /**
     * Print empty newline.
     */
    public void println() {
        out.println();
    }

    /**
     * Read a line of input with a prompt, with character echo.
     * Handles cross-platform line endings (Windows \r\n, macOS/Linux \n, old Mac
     * \r).
     * 
     * @param prompt The prompt to display
     * @return The user's input, or empty string on error/EOF
     */
    public String readLine(String prompt) {
        print(prompt);

        try {
            StringBuilder sb = new StringBuilder();
            int ch;

            while ((ch = reader.read()) != -1) {
                // Handle leftover \n from previous \r\n sequence
                if (skipNextLF) {
                    skipNextLF = false;
                    if (ch == '\n') {
                        continue; // Skip this \n and read next character
                    }
                }

                // Handle line endings - break immediately on \r or \n
                if (ch == '\n') {
                    out.println(); // Move to next line
                    break;
                } else if (ch == '\r') {
                    out.println(); // Move to next line
                    skipNextLF = true; // Mark to skip \n if it comes next
                    break; // Break immediately - don't wait for \n
                } else if (ch == '\b' || ch == 127) { // Backspace or DEL
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                        out.print("\b \b"); // Erase character on screen
                        out.flush();
                    }
                } else if (ch >= 32) { // Printable characters
                    sb.append((char) ch);
                    out.print((char) ch); // Echo the character
                    out.flush();
                }
            }

            return sb.toString().trim();
        } catch (IOException e) {
            println("Input error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Read a Long value with retry on invalid input.
     * 
     * @param prompt The prompt to display
     * @return The parsed Long value
     */
    public Long readLong(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                println("Please enter a valid number.");
            }
        }
    }

    /**
     * Read an Integer value with retry on invalid input.
     * 
     * @param prompt The prompt to display
     * @return The parsed Integer value
     */
    public Integer readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                println("Please enter a valid number.");
            }
        }
    }

    /**
     * Read a Double value with retry on invalid input.
     * 
     * @param prompt The prompt to display
     * @return The parsed Double value
     */
    public Double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                println("Please enter a valid number.");
            }
        }
    }

    /**
     * Check if a string is not blank.
     */
    public static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /**
     * Get the PrintStream for direct output.
     */
    public PrintStream getOut() {
        return out;
    }
}
