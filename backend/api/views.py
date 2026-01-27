from rest_framework import viewsets, status
from rest_framework.decorators import action, api_view
from rest_framework.response import Response
from django.shortcuts import get_object_or_404
from .models import Usuario, Car, Viaje
from .serializers import UsuarioSerializer, CarSerializer, ViajeSerializer


class UsuarioViewSet(viewsets.ModelViewSet):
    queryset = Usuario.objects.all()
    serializer_class = UsuarioSerializer
    lookup_field = 'id_usuario'
    
    @action(detail=False, methods=['post'], url_path='login')
    def login(self, request):
        """Login endpoint - equivalente a obtenerUsuario"""
        usuario = request.data.get('usuario')
        contrasena = request.data.get('contrasena')
        
        try:
            user = Usuario.objects.get(usuario=usuario, contrasena=contrasena)
            serializer = self.get_serializer(user)
            return Response(serializer.data)
        except Usuario.DoesNotExist:
            return Response({'error': 'Usuario no encontrado'}, status=status.HTTP_404_NOT_FOUND)
    
    @action(detail=False, methods=['get'], url_path='all')
    def list_all(self, request):
        """Cargar todos los usuarios - equivalente a cargarUsuarios"""
        usuarios = Usuario.objects.all()
        serializer = self.get_serializer(usuarios, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['put'], url_path='update')
    def update_user(self, request, id_usuario=None):
        """Actualizar usuario - equivalente a actualizarUsuario"""
        usuario = self.get_object()
        serializer = self.get_serializer(usuario, data=request.data, partial=True)
        
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=True, methods=['post'], url_path='coches')
    def add_car(self, request, id_usuario=None):
        """Agregar coche a usuario"""
        usuario = self.get_object()
        car_data = request.data.copy()
        
        car = Car.objects.create(
            usuario=usuario,
            brand=car_data.get('brand'),
            model=car_data.get('model'),
            year=car_data.get('year'),
            city_kmpl=car_data.get('cityKmpl', '0.0'),
            highway_kmpl=car_data.get('highwayKmpl', '0.0'),
            avg_kmpl=car_data.get('avgKmpl', '0.0'),
            capacidad_deposito=car_data.get('capacidaddeposito', -1),
            capacidad_actual=car_data.get('capacidadactual', -1),
            veces_usado=car_data.get('vecesusado', 0)
        )
        
        serializer = CarSerializer(car)
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    
    @action(detail=True, methods=['post'], url_path='viajes')
    def add_viaje(self, request, id_usuario=None):
        """Agregar viaje a usuario"""
        usuario = self.get_object()
        viaje_data = request.data
        
        viaje = Viaje.objects.create(
            usuario=usuario,
            origen=viaje_data.get('origen'),
            destino=viaje_data.get('destino'),
            distancia_km=viaje_data.get('distanciaKm'),
            litros_consumidos=viaje_data.get('litrosConsumidos'),
            coche_usado=viaje_data.get('cocheUsado'),
            timestamp=viaje_data.get('timestamp')
        )
        
        serializer = ViajeSerializer(viaje)
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    
    @action(detail=True, methods=['put'], url_path='coches/(?P<car_id>[^/.]+)')
    def update_car(self, request, id_usuario=None, car_id=None):
        """Actualizar coche específico"""
        usuario = self.get_object()
        car = get_object_or_404(Car, id=car_id, usuario=usuario)
        
        serializer = CarSerializer(car, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['get'], url_path='buscar')
    def buscar_usuarios(self, request):
        """Buscar usuarios por nombre de usuario"""
        query = request.query_params.get('q', '').strip()
        usuario_actual_id = request.query_params.get('exclude', None)
        
        if not query:
            return Response({'results': []})
        
        # Buscar usuarios que contengan el texto en el campo 'usuario'
        usuarios = Usuario.objects.filter(usuario__icontains=query)
        
        # Excluir al usuario actual de los resultados
        if usuario_actual_id:
            usuarios = usuarios.exclude(id_usuario=usuario_actual_id)
        
        # Limitar a 20 resultados
        usuarios = usuarios[:20]
        
        serializer = self.get_serializer(usuarios, many=True)
        return Response({'results': serializer.data})
    
    @action(detail=True, methods=['post'], url_path='amigos')
    def add_friend(self, request, id_usuario=None):
        """Agregar amigo de forma bidireccional"""
        usuario = self.get_object()
        amigo_id = request.data.get('amigo_id')
        
        if not amigo_id:
            return Response({'error': 'amigo_id es requerido'}, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            amigo = Usuario.objects.get(id_usuario=amigo_id)
        except Usuario.DoesNotExist:
            return Response({'error': 'Usuario amigo no encontrado'}, status=status.HTTP_404_NOT_FOUND)
        
        # Verificar que no sean el mismo usuario
        if usuario.id_usuario == amigo_id:
            return Response({'error': 'No puedes agregarte a ti mismo'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Agregar amigo bidireccionalmente
        if amigo_id not in usuario.lista_amigos_ids:
            usuario.lista_amigos_ids.append(amigo_id)
            usuario.save()
        
        if usuario.id_usuario not in amigo.lista_amigos_ids:
            amigo.lista_amigos_ids.append(usuario.id_usuario)
            amigo.save()
        
        return Response({
            'lista_amigos_ids': usuario.lista_amigos_ids,
            'mensaje': f'Ahora eres amigo de {amigo.usuario}'
        })
