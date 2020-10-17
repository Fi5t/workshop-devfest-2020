from rest_auth.models import TokenModel
from rest_framework import serializers

from todos.models import User, Todo


class TodoSerializer(serializers.ModelSerializer):
    owner = serializers.ReadOnlyField(source='owner.username')

    class Meta:
        model = Todo
        fields = '__all__'


class UserSerializer(serializers.ModelSerializer):
    todos = TodoSerializer(many=True, required=False)

    class Meta:
        model = User
        fields = '__all__'


class TokenSerializer(serializers.ModelSerializer):
    access_token = serializers.CharField(source='key')
    user = UserSerializer()

    class Meta:
        model = TokenModel
        fields = ['access_token', 'user']