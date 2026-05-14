import mongoengine as me


class Scooter(me.Document):
    mac = me.StringField(max_length=17, unique=True, required=True)
    key = me.StringField(max_length=16, required=True)
    payed = me.BooleanField(default=False)

    meta = {
        'collection': 'scooter',
        'strict': False
    }


