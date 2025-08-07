from rest_framework import serializers
from secrets import token_bytes

from ransom.models import Scooter


class ScooterSerializer(serializers.ModelSerializer):
    class Meta:
        model = Scooter
        fields = '__all__'

class ScooterCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Scooter
        fields = ['mac']

    def create(self, validated_data):
        mac_value = validated_data['mac']
        key_value = token_bytes(8).hex()
        payed_value = False

        scooter = Scooter(mac=mac_value, key=key_value, payed=payed_value)
        scooter.save()

        return scooter

class ScooterUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Scooter
        fields = ['payed']

    def update(self, instance, validated_data):
        instance.mac = validated_data.get('mac', instance.mac)
        instance.key = validated_data.get('key', instance.key)
        instance.payed = validated_data.get('payed', instance.payed)
        instance.save()
        return instance



class ScooterGetKeySerializer(serializers.ModelSerializer):
    key = serializers.ReadOnlyField()

    class Meta:
        model = Scooter
        fields = ['key']

class ScooterGetBMSSerializer(serializers.ModelSerializer):
    bms = serializers.SerializerMethodField(method_name='bms_constructor')

    class Meta:
        model = Scooter
        fields = ['bms']

    def bms_constructor(self, obj):
        # Modify bin to add key
        with open("126.bin", 'rb') as file:
            file_data = file.read()
        hex_data = file_data.hex()
        return hex_data


