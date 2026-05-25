package com.igire.gateway.service;

import com.igire.gateway.model.CachedPayment;
import com.igire.gateway.model.PaymentRequest;
import com.igire.gateway.model.PaymentResponse;
import com.igire.gateway.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public ResponseEntity<?> processPayment(
            String idempotencyKey,
            PaymentRequest request
    ) throws Exception {

        String requestHash = hashRequest(request);

        Optional<CachedPayment> existingPaymentOptional =
                paymentRepository.findById(idempotencyKey);

        if (existingPaymentOptional.isPresent()) {

            CachedPayment existingPayment =
                    existingPaymentOptional.get();

            if (!existingPayment.getRequestHash().equals(requestHash)) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new PaymentResponse(
                                "Idempotency key already used for a different request body."
                        ));
            }

            while (existingPayment.isProcessing()) {
                Thread.sleep(200);

                existingPayment =
                        paymentRepository.findById(idempotencyKey).get();
            }

            return ResponseEntity
                    .status(existingPayment.getStatusCode())
                    .header("X-Cache-Hit", "true")
                    .body(new PaymentResponse(
                            existingPayment.getResponseMessage()
                    ));
        }

        CachedPayment cachedPayment =
                new CachedPayment(idempotencyKey, requestHash);

        paymentRepository.save(cachedPayment);

        Thread.sleep(2000);

        String message =
                "Charged "
                        + request.getAmount()
                        + " "
                        + request.getCurrency();

        cachedPayment.setResponseMessage(message);
        cachedPayment.setStatusCode(201);
        cachedPayment.setProcessing(false);

        paymentRepository.save(cachedPayment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new PaymentResponse(message));
    }

    private String hashRequest(PaymentRequest request)
            throws Exception {

        String rawData =
                request.getAmount() + request.getCurrency();

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hashBytes =
                digest.digest(
                        rawData.getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder hexString = new StringBuilder();

        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}