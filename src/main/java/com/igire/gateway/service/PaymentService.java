package com.igire.gateway.service;

import com.igire.gateway.model.CachedPayment;
import com.igire.gateway.model.PaymentRequest;
import com.igire.gateway.model.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    // Thread-safe memory storage
    private final ConcurrentHashMap<String, CachedPayment> paymentStore = new ConcurrentHashMap<>();


    public ResponseEntity<?> processPayment(String idempotencyKey, PaymentRequest request)
            throws Exception {

        // Create hash from request body
        String requestHash = hashRequest(request);

        // Check if key already exists
        if (paymentStore.containsKey(idempotencyKey)) {

            CachedPayment existingPayment = paymentStore.get(idempotencyKey);

            // Fraud protection:
            // Same key but different request body
            if (!existingPayment.getRequestHash().equals(requestHash)) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new PaymentResponse(
                                "Idempotency key already used for a different request body."
                        ));
            }

            // In-flight handling
            while (existingPayment.isProcessing()) {
                Thread.sleep(200);
            }

            // Return cached response
            return ResponseEntity
                    .status(existingPayment.getStatusCode())
                    .header("X-Cache-Hit", "true")
                    .body(existingPayment.getResponse());
        }

        // First request
        CachedPayment cachedPayment = new CachedPayment(requestHash);

        paymentStore.put(idempotencyKey, cachedPayment);

        // Simulate payment processing delay
        Thread.sleep(2000);

        // Create response
        PaymentResponse response = new PaymentResponse(
                "Charged " + request.getAmount() + " " + request.getCurrency()
        );

        // Save response
        cachedPayment.setResponse(response);
        cachedPayment.setStatusCode(201);
        cachedPayment.setProcessing(false);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // SHA-256 request hashing
    private String hashRequest(PaymentRequest request) throws Exception {

        String rawData = request.getAmount() + request.getCurrency();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashBytes = digest.digest(
                rawData.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder hexString = new StringBuilder();

        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}