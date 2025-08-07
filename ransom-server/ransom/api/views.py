from secrets import token_bytes

from rest_framework import status
from rest_framework.generics import get_object_or_404
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


class ScooterListCreateAPIView(APIView):

    def get(self, request):
        scooters = Scooter.objects.filter()
        serializer = ScooterSerializer(scooters, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = ScooterCreateSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)



class ScooterAPIView(APIView):

    def get_object(self, mac):
        scooter = get_object_or_404(Scooter, mac=mac)
        return scooter

    def get(self, request, mac):
        scooter = self.get_object(mac)
        if scooter.payed == False:
            serializer = ScooterGetBMSSerializer(instance=scooter, data=request.data)
        else:
            serializer = ScooterGetKeySerializer(instance=scooter, data=request.data)
        if serializer.is_valid():
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


    def put(self, request, mac):
        scooter = self.get_object(mac)
        serializer = ScooterUpdateSerializer(instance=scooter, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
