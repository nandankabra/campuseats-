package com.example.campuseatsOrders.client;

import com.example.campuseatsOrders.exception.PaymentRejectedException;
import com.example.campuseatsOrders.exception.PaymentUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentClient {

    private final HttpClient httpClient;
    private final String paymentsBaseUrl;

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 200;
    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(3);

    public PaymentClient(
            @Value("${payments.base-url:http://localhost:8081}")
            String paymentsBaseUrl) {

        this.paymentsBaseUrl =
                paymentsBaseUrl.replaceAll("/+$", "");

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public void createPayment(
            Long orderId,
            String idempotencyKey) {

        String url =
                paymentsBaseUrl +
                        "/payments/" +
                        orderId;

        for (int attempt = 1;
             attempt <= MAX_ATTEMPTS;
             attempt++) {

            try {

                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(REQUEST_TIMEOUT)
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
                                .header(
                                        "Content-Type",
                                        "application/json"
                                )
                                .POST(
                                        HttpRequest.BodyPublishers.noBody()
                                )
                                .build();

                HttpResponse<String> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                        );

                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                    return;
                }

                if (status >= 400 && status < 500) {
                    throw new PaymentRejectedException(
                            "Payments service rejected request with status "
                                    + status
                    );
                }

                if (status >= 500) {

                    if (attempt == MAX_ATTEMPTS) {
                        throw new PaymentUnavailableException(
                                "Payments service returned status "
                                        + status
                                        + " after "
                                        + MAX_ATTEMPTS
                                        + " attempts"
                        );
                    }

                    sleepWithBackoffAndJitter(attempt);
                }

            } catch (PaymentRejectedException ex) {

                throw ex;

            } catch (PaymentUnavailableException ex) {

                throw ex;

            } catch (IOException ex) {

                if (attempt == MAX_ATTEMPTS) {
                    throw new PaymentUnavailableException(
                            "Payments service unreachable after "
                                    + MAX_ATTEMPTS
                                    + " attempts"
                    );
                }

                sleepWithBackoffAndJitter(attempt);

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                throw new PaymentUnavailableException(
                        "Payment request interrupted"
                );
            }
        }
    }

    private void sleepWithBackoffAndJitter(int attempt)
            throws PaymentUnavailableException {

        long exponentialDelay =
                INITIAL_BACKOFF_MS *
                        (1L << (attempt - 1));

        long jitter =
                ThreadLocalRandom.current()
                        .nextLong(0, 100);

        long totalDelay =
                exponentialDelay + jitter;

        try {

            Thread.sleep(totalDelay);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new PaymentUnavailableException(
                    "Retry wait interrupted"
            );
        }
    }
}