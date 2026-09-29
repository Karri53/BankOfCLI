package com.bankofcli.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyConverter {

    /*
     * Get the current exchange rate from
     * the Frankfurter public API.
     */
    public double getExchangeRate(
            String fromCurrency,
            String toCurrency) {

        try {

            /*
             * Build the API URL.
             *
             * Example:
             * USD -> EUR
             */
            String url =
                    "https://api.frankfurter.dev/v2/rates.csv?base="
                            + fromCurrency.toLowerCase()
                            + "&quotes="
                            + toCurrency.toLowerCase();

            // Create the HTTP client.
            HttpClient client =
                    HttpClient.newHttpClient();

            // Create the GET request.
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            // Send the request and receive text.
            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            /*
             * A successful HTTP request should
             * return a status code of 200.
             */
            if (response.statusCode() != 200) {

                throw new IllegalStateException(
                        "Currency service returned an error.");
            }

            /*
             * CSV response:
             *
             * date,base,quote,rate
             * 2026-09-28,USD,EUR,0.85
             */
            String[] lines =
                    response.body().trim().split("\n");

            if (lines.length < 2) {

                throw new IllegalStateException(
                        "Currency service returned invalid data.");
            }

            // Split the actual data row by commas.
            String[] values =
                    lines[1].split(",");

            if (values.length < 4) {

                throw new IllegalStateException(
                        "Currency service returned invalid data.");
            }

            // The exchange rate is the fourth value.
            return Double.parseDouble(
                    values[3]);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not connect to currency service.");

        } catch (InterruptedException e) {

            /*
             * Restore the thread interruption status.
             */
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Currency request was interrupted.");

        } catch (NumberFormatException e) {

            throw new IllegalStateException(
                    "Currency service returned an invalid rate.");
        }
    }

    /*
     * Convert an amount using the current rate.
     */
    public double convert(
            double amount,
            String fromCurrency,
            String toCurrency) {

        double rate =
                getExchangeRate(
                        fromCurrency,
                        toCurrency);

        return amount * rate;
    }
}