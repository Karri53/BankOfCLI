package com.bankofcli.persistence;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    // Create one shared ConnectionFactory instance for the application.
    private static final ConnectionFactory connectionFactory =
            new ConnectionFactory();

    // Stores the database connection values from db.properties.
    private final Properties props = new Properties();

    private ConnectionFactory() {

        try {
            // Load the database URL, username, and password.
            props.load(new FileReader("src/main/resources/db.properties"));

        } catch (IOException e) {

            // Stop the application if database configuration cannot be loaded.
            throw new IllegalStateException(
                    "Could not load database configuration",
                    e);
        }
    }

    public static ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection() {

        try {
            // Create and return a JDBC connection to PostgreSQL.
            return DriverManager.getConnection(
                    props.getProperty("DB_URL"),
                    props.getProperty("DB_USER"),
                    props.getProperty("DB_PASSWORD"));

        } catch (SQLException e) {

            // Keep the technical database error wrapped for later logging.
            throw new IllegalStateException(
                    "Could not connect to the database",
                    e);
        }
    }
}