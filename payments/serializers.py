from rest_framework import serializers


class PaymentSerializer(serializers.Serializer):

    amount = serializers.IntegerField(
        min_value=1
    )

    currency = serializers.CharField(
        max_length=10
    )