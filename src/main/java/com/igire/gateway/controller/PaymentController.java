package com.igire.gateway.controller;

import com.igire.gateway.model.PaymentRequest;
import com.igire.gateway.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(

            @RequestHeader("Idempotency-Key") String idempotencyKey,

            @RequestBody PaymentRequest request

    ) throws Exception {

        return paymentService.processPayment(idempotencyKey, request);
    }
}