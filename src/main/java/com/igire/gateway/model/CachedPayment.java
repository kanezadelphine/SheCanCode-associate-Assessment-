package com.igire.gateway.model;

public class CachedPayment {

    private String requestHash;
    private PaymentResponse response;
    private int statusCode;
    private boolean processing;

    public CachedPayment(String requestHash) {
        this.requestHash = requestHash;
        this.processing = true;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public PaymentResponse getResponse() {
        return response;
    }

    public void setResponse(PaymentResponse response) {
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}