# DJANGO SERVER

## Usage

* Install required packages:

    `apt install python3-django python3-pymongo python3-dnspython`

    `pip install djongo`

    `pip install pytz`

* Download server and start:

    `python3 manage.py runserver 0.0.0.0:8000`

## APIs

* GET scooter list and POST a scooter:

    `http://localhost/api/ransom/`

* GET key or bms bytes and PUT single scooter:

    `http://localhost/api/ransom/00-00-00-00-00`

## Server Set-Up

`django-admin startproject ransomserver` to create server project

`python3 manage.py migrate` to migrate apps

`python3 manage.py runserver` to test

`python3 manage.py startapp ransom` to create ransom app

## MongoDB Set-Up

Install pyMongo and DJongo for adding MongoDB support to Django

`apt install python3-pymongo python3-dnspython`

`pip install djongo`

`pip install pytz` to solve errors

Modify database settings inside settings.py

```
DATABASES = {
        'default': {
            'ENGINE': 'djongo',
            'NAME': 'Cluster0',
            'ENFORCE_SCHEMA': False,
            'CLIENT': {
                'host': 'mongodb+srv://<yourusername>:<password>@cluster0.r3mlcub.mongodb.net/?retryWrites=true&w=majority'
            }
        }
}
```


