from secrets import token_bytes

from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from ransom.models import Scooter
from ransom.api.serializers import ScooterSerializer
from ransom.api.serializers import ScooterCreateSerializer
from ransom.api.serializers import ScooterGetBMSSerializer
from ransom.api.serializers import ScooterGetKeySerializer
from ransom.api.serializers import ScooterUpdateSerializer
from django.http import JsonResponse
from django.http import HttpResponse
from mongoengine.errors import DoesNotExist, ValidationError


class ScooterListCreateAPIView(APIView):

    def get(self, request):
        try:
            scooters = Scooter.objects.all()
            serializer = ScooterSerializer(scooters, many=True)
            return Response(serializer.data)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    def post(self, request):
        serializer = ScooterCreateSerializer(data=request.data)
        if serializer.is_valid():
            try:
                scooter = serializer.save()
                response_serializer = ScooterSerializer(scooter)
                return Response(response_serializer.data, status=status.HTTP_201_CREATED)
            except ValidationError as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ScooterAPIView(APIView):

    def get_object(self, mac):
        try:
            scooter = Scooter.objects.get(mac=mac)
            return scooter
        except DoesNotExist:
            return None

    def get(self, request, mac):
        scooter = self.get_object(mac)
        if scooter is None:
            return Response({'error': 'Scooter not found'}, status=status.HTTP_404_NOT_FOUND)
        
        if scooter.payed == False:
            serializer = ScooterGetBMSSerializer(instance=scooter, data=request.data)
        else:
            serializer = ScooterGetKeySerializer(instance=scooter, data=request.data)
        
        if serializer.is_valid():
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def put(self, request, mac):
        scooter = self.get_object(mac)
        if scooter is None:
            return Response({'error': 'Scooter not found'}, status=status.HTTP_404_NOT_FOUND)
        
        serializer = ScooterUpdateSerializer(instance=scooter, data=request.data)
        if serializer.is_valid():
            try:
                scooter = serializer.save()
                response_serializer = ScooterSerializer(scooter)
                return Response(response_serializer.data)
            except ValidationError as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
