# Idempotency Gateway API — The "Pay-Once" Protocol

A backend payment-processing API built with Django REST Framework that prevents duplicate payment processing using **idempotency keys**.

This project simulates a fintech payment gateway where clients may retry requests due to network failures or timeouts. The API guarantees that the same payment request is processed **exactly once**, even if the client retries multiple times.

---

## Features

- Prevents duplicate payment processing
- Supports `Idempotency-Key` request headers
- Returns cached responses for duplicate requests
- Detects fraudulent reuse of keys with different payloads
- Handles concurrent/in-flight requests safely
- Includes request audit logging
- Stores processing duration and client IP
- Built with Django REST Framework
- SQLite database integration

---

## System Architecture

<p align="center">
  <img src="docs/architecture.png" width="900">
</p>

<p align="center">
  Architecture diagram for the Idempotency Gateway Payment API.
</p>

---

## Problem Statement

In real-world fintech systems, payment requests can be retried automatically when a client experiences:

- Network failures
- Connection timeouts
- Slow responses

Without idempotency protection:

- Customers may be charged multiple times
- Payment records become inconsistent
- Financial trust is damaged
- Regulatory and compliance issues may arise

This project solves that problem by implementing an **Idempotency Gateway** that safely handles duplicate payment requests.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Python 3 | Backend language |
| Django | Web framework |
| Django REST Framework | REST API development |
| SQLite | Database |
| JSON | API communication |

---

## Project Structure

```bash
shecancode-associate-assessment/
│
├── gateway/
│   ├── __init__.py
│   ├── asgi.py
│   ├── settings.py
│   ├── urls.py
│   └── wsgi.py
│
├── payments/
│   ├── migrations/
│   ├── __init__.py
│   ├── models.py
│   ├── serializers.py
│   ├── urls.py
│   └── views.py
│
├── docs/
│   └── architecture.png
│
├── .gitignore
├── db.sqlite3
├── manage.py
├── README.md
└── requirements.txt
```

---

# API Endpoint

## Process Payment

### Request

```http
POST /api/process-payment
```

### Headers

```http
Idempotency-Key: payment-001
```

### Request Body

```json
{
    "amount": 100,
    "currency": "GHS"
}
```

---

## Successful Response

```json
{
    "message": "Charged 100 GHS"
}
```

### Status

```http
201 Created
```

---

## Duplicate Request Response

If the same request is sent again using the same `Idempotency-Key` and same body:

```json
{
    "message": "Charged 100 GHS"
}
```

### Response Header

```http
X-Cache-Hit: true
```

This indicates the request was replayed safely from cache without reprocessing payment.

---

## Fraud Protection

If a client reuses the same `Idempotency-Key` with a different request body:

```json
{
    "error": "Idempotency key already used for a different request body."
}
```

### Status

```http
409 Conflict
```

---

# In-Flight Request Handling

This project safely handles concurrent requests.

If two identical requests arrive simultaneously:

1. The first request processes normally
2. The second request waits
3. The payment is processed only once
4. Both requests receive the same response

This prevents race conditions and duplicate charges.

---

# Audit Logging & Request Tracking

An additional fintech-focused safety feature was implemented to improve traceability and monitoring.

The system stores:

- Client IP address
- Request timestamp
- Processing duration
- Request status

### Why This Matters

Audit logging helps with:

- Fraud investigations
- Debugging production issues
- Compliance and regulatory requirements
- Security monitoring
- Transaction traceability

---

# Setup Instructions

## Clone Repository

```bash
git clone https://github.com/kanezadelphine/SheCanCode-associate-Assessment-.git

cd idempotency-gateway
```

---

## Create Virtual Environment

### Windows

```powershell
python -m venv venv

venv\Scripts\Activate.ps1
```

---

## Install Dependencies

```bash
pip install -r requirements.txt
```

---

## Run Migrations

```bash
python manage.py makemigrations

python manage.py migrate
```

---

## Start Development Server

```bash
python manage.py runserver
```

Server runs at:

```text
http://127.0.0.1:8000/
```

---

# Testing

The API was tested using:

- Thunder Client
- Django REST Framework browser
- Duplicate request simulations
- Fraud protection scenarios

---

# Example Test Cases

## First Payment

- New key
- Payment processed successfully

---

## Duplicate Payment

- Same key
- Same body
- Cached response returned instantly

---

## Fraud Attempt

- Same key
- Different body
- Request rejected with `409 Conflict`

---

# Future Improvements

- Redis caching for production-scale performance
- PostgreSQL integration
- Authentication and API keys
- Docker containerization
- Rate limiting
- Asynchronous task queues

---

# Author

## KANEZA Delphine

Software Engineering Student & Backend Developer

### GitHub

https://github.com/kanezadelphine

### Email

delphinekaneza888@gmail.com