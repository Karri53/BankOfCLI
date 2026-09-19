package com.bankofcli.api;

import com.bankofcli.persistence.BankDAO;
import com.bankofcli.persistence.BankDAOImpl;
import com.bankofcli.service.BankService;
import com.bankofcli.service.BankServiceImpl;
import com.bankofcli.util.AppLogger;

public class Main {

    public static void main(String[] args) {

        /*
         * Handle startup failures here because the database
         * connection may fail before the REPL is created.
         */
        try {

            // Create the DAO that handles JDBC and PostgreSQL.
            BankDAO bankDAO =
                    new BankDAOImpl();

            // Give the DAO to the Service layer.
            BankService bankService =
                    new BankServiceImpl(bankDAO);

            // Give the Service to the API layer and start the application.
            new BankRepl(bankService).run();

        } catch (IllegalStateException e) {

            // Log the technical failure without showing a stack trace.
            AppLogger.error(
                    "Application startup failed: "
                            + e.getMessage());

            // Give the user a simple, friendly message.
            System.out.println(
                    "Service unavailable. Please try again later.");
        }
    }
}