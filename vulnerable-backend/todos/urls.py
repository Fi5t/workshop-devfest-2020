from django.urls import path

from todos.views import TodoList, UserList

urlpatterns = [
    path('users/', UserList.as_view(), name='users'),
    path('todos/', TodoList.as_view(), name='todos')
]
