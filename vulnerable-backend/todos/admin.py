from django.contrib import admin

# Register your models here.
from todos.models import User

admin.site.register(User)