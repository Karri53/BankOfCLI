package com.bankofcli.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    // Store logs inside a dedicated logs folder.
    private static final String LOG_DIRECTORY = "logs";

    private static final String LOG_FILE =
            LOG_DIRECTORY + "/application.log";

    // Keep log timestamps easy to read.
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss");

    /*
     * Record successful application activity.
     */
    public static void info(
            String message) {

        writeLog(
                "INFO",
                message);
    }

    /*
     * Record failures and security-related activity.
     */
    public static void error(
            String message) {

        writeLog(
                "ERROR",
                message);
    }

    /*
     * Write one formatted log entry to application.log.
     */
    private static void writeLog(
            String level,
            String message) {

        /*
         * Create the logs folder if it
         * does not already exist.
         */
        File logDirectory =
                new File(
                        LOG_DIRECTORY);

        if (!logDirectory.exists()) {

            logDirectory.mkdirs();
        }

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(
                                        LOG_FILE,
                                        true))
        ) {

            String timestamp =
                    LocalDateTime.now()
                            .format(FORMATTER);

            writer.write(
                    timestamp
                            + " "
                            + level
                            + " "
                            + message);

            writer.newLine();

        } catch (IOException e) {

            System.out.println(
                    "Unable to write application log.");
        }
    }
}