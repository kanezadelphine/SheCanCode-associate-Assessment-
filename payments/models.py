from django.db import models


class PaymentRequest(models.Model):

    STATUS_CHOICES = [
        ("processing", "Processing"),
        ("completed", "Completed"),
    ]

    idempotency_key = models.CharField(
        max_length=255,
        unique=True
    )

    request_hash = models.TextField()

    response_body = models.JSONField()

    status_code = models.IntegerField()

    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default="processing"
    )

    client_ip = models.GenericIPAddressField(
        null=True,
        blank=True
    )

    processing_time = models.FloatField(
        null=True,
        blank=True
    )

    created_at = models.DateTimeField(
        auto_now_add=True
    )

    def __str__(self):
        return self.idempotency_key