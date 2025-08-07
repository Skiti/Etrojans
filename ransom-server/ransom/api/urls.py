from django.urls import path
from ransom.api.views import ScooterListCreateAPIView, ScooterAPIView

urlpatterns = [
    path("ransom/",
         ScooterListCreateAPIView.as_view(),
         name="scooter-list"),
    path("ransom/<slug:mac>/",
         ScooterAPIView.as_view(),
         name="scooter-detail"),
]
