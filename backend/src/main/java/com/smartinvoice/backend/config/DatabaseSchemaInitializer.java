package com.smartinvoice.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Drop admins table if it exists
            try {
                statement.execute("DROP TABLE IF EXISTS admins CASCADE");
                System.out.println("✓ Dropped admins table");
            } catch (Exception e) {
                System.err.println("Info: admins table not found or already dropped");
            }
            
            // Add user_type column if it doesn't exist
            try {
                statement.execute("ALTER TABLE users ADD COLUMN user_type INTEGER DEFAULT 0");
                System.out.println("✓ Added user_type column to users table");
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("✓ user_type column already exists");
                } else {
                    System.err.println("Error adding user_type column: " + e.getMessage());
                }
            }
            
            // Add password_set column if it doesn't exist
            try {
                statement.execute("ALTER TABLE users ADD COLUMN password_set BOOLEAN DEFAULT false");
                System.out.println("✓ Added password_set column to users table");
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("✓ password_set column already exists");
                } else {
                    System.err.println("Error adding password_set column: " + e.getMessage());
                }
            }
            
            // Update existing users to have passwordSet = true
            try {
                int updated = statement.executeUpdate("UPDATE users SET password_set = true WHERE password IS NOT NULL AND password_set = false");
                System.out.println("✓ Updated " + updated + " users with password_set = true");
            } catch (Exception e) {
                System.err.println("Error updating password_set: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
