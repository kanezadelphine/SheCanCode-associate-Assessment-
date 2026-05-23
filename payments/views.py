import hashlib
import json
import time

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status

from .models import PaymentRequest
from .serializers import PaymentSerializer


def generate_request_hash(data):

    """
    Generate consistent hash
    for request body.
    """

    return hashlib.sha256(
        json.dumps(
            data,
            sort_keys=True
        ).encode()
    ).hexdigest()


@api_view(["POST"])
def process_payment(request):

    start_time = time.time()

    # ====================================
    # GET IDEMPOTENCY KEY
    # ====================================

    idempotency_key = request.headers.get(
        "Idempotency-Key"
    )

    if not idempotency_key:

        return Response(
            {
                "error":
                "Idempotency-Key header required."
            },
            status=status.HTTP_400_BAD_REQUEST
        )

    # ====================================
    # VALIDATE REQUEST BODY
    # ====================================

    serializer = PaymentSerializer(
        data=request.data
    )

    if not serializer.is_valid():

        return Response(
            serializer.errors,
            status=status.HTTP_400_BAD_REQUEST
        )

    validated_data = serializer.validated_data

    # ====================================
    # GENERATE REQUEST HASH
    # ====================================

    request_hash = generate_request_hash(
        validated_data
    )

    # ====================================
    # CHECK EXISTING REQUEST
    # ====================================

    existing_payment = PaymentRequest.objects.filter(
        idempotency_key=idempotency_key
    ).first()

    # ====================================
    # HANDLE DUPLICATE REQUEST
    # ====================================

    if existing_payment:

        # --------------------------------
        # SAME KEY + DIFFERENT BODY
        # --------------------------------

        if existing_payment.request_hash != request_hash:

            return Response(
                {
                    "error":
                    "Idempotency key already used for a different request body."
                },
                status=status.HTTP_409_CONFLICT
            )

        # --------------------------------
        # IN-FLIGHT HANDLING
        # --------------------------------

        while existing_payment.status == "processing":

            time.sleep(0.2)

            existing_payment.refresh_from_db()

        # --------------------------------
        # RETURN CACHED RESPONSE
        # --------------------------------

        response = Response(
            existing_payment.response_body,
            status=existing_payment.status_code
        )

        response["X-Cache-Hit"] = "true"

        return response

    # ====================================
    # CREATE NEW PAYMENT RECORD
    # ====================================

    payment = PaymentRequest.objects.create(
        idempotency_key=idempotency_key,
        request_hash=request_hash,
        response_body={},
        status_code=201,
        status="processing",
        client_ip=get_client_ip(request)
    )

    # ====================================
    # SIMULATE PAYMENT PROCESSING
    # ====================================

    time.sleep(2)

    response_body = {
        "message":
        f"Charged {validated_data['amount']} "
        f"{validated_data['currency']}"
    }

    processing_duration = time.time() - start_time

    # ====================================
    # SAVE FINAL RESPONSE
    # ====================================

    payment.response_body = response_body

    payment.status_code = 201

    payment.status = "completed"

    payment.processing_time = round(
        processing_duration,
        2
    )

    payment.save()

    # ====================================
    # RETURN RESPONSE
    # ====================================

    return Response(
        response_body,
        status=status.HTTP_201_CREATED
    )


def get_client_ip(request):

    """
    Get client IP address.
    """

    x_forwarded_for = request.META.get(
        "HTTP_X_FORWARDED_FOR"
    )

    if x_forwarded_for:

        return x_forwarded_for.split(",")[0]

    return request.META.get("REMOTE_ADDR")