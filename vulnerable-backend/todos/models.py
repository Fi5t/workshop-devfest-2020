from django.contrib.auth.models import AbstractUser
from django.db import models


from vulnerable_backend import settings


class User(AbstractUser):
    avatar = models.FileField(upload_to='data/avatars', null=True)


class Todo(models.Model):
    text = models.CharField(max_length=200)
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, related_name='todos', on_delete=models.CASCADE)

