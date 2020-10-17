from rest_framework import status, generics
from rest_framework.exceptions import PermissionDenied
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView

from todos.models import User, Todo
from todos.serializers import UserSerializer, TodoSerializer


class UserList(generics.ListAPIView):
    permission_classes = (AllowAny,)
    queryset = User.objects.all()
    serializer_class = UserSerializer

    def get(self, request, *args, **kwargs):
        api_key = request.headers.get('X-API-KEY')

        if api_key == 'b3b4e12487f9a41a613ad2d05237752acfba1047d8556551bd103bd96a98a057':
            return self.list(request, *args, **kwargs)
        else:
            raise PermissionDenied("X-API-KEY header isn't provided")


class TodoList(APIView):
    def get(self, request):
        todos = Todo.objects.filter(owner_id=self.request.user.id)
        serializer = TodoSerializer(todos, many=True)

        return Response(serializer.data)

    def post(self, request):
        serializer = TodoSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save(owner=self.request.user)
            return Response(serializer.data, status=status.HTTP_201_CREATED)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
