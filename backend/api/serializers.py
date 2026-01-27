from rest_framework import serializers
from .models import Usuario, Car, Viaje


class CarSerializer(serializers.ModelSerializer):
    class Meta:
        model = Car
        fields = ['id', 'brand', 'model', 'year', 'city_kmpl', 'highway_kmpl', 'avg_kmpl', 
                  'capacidad_deposito', 'capacidad_actual', 'veces_usado']


class ViajeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Viaje
        fields = ['id', 'origen', 'destino', 'distancia_km', 'litros_consumidos', 'coche_usado', 'timestamp']


class UsuarioSerializer(serializers.ModelSerializer):
    coches = CarSerializer(many=True, read_only=True)
    viajes = ViajeSerializer(many=True, read_only=True)
    
    class Meta:
        model = Usuario
        fields = ['id_usuario', 'usuario', 'contrasena', 'nombre', 'apellidos', 
                  'lista_amigos_ids', 'coches', 'viajes']
        extra_kwargs = {
            'contrasena': {'write_only': True}
        }
