from djongo import models


class Scooter(models.Model):
    mac = models.CharField(max_length=17, unique=True)
    key = models.CharField(max_length=16)
    payed = models.BooleanField(default=False)


