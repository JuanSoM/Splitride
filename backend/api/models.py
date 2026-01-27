from django.db import models
import secrets
import string


def generate_user_id():
    """Genera un ID aleatorio similar al que genera Java"""
    return ''.join(secrets.choice(string.ascii_lowercase + string.digits) for _ in range(26))


class Usuario(models.Model):
    id_usuario = models.CharField(max_length=100, primary_key=True, default=generate_user_id)
    usuario = models.CharField(max_length=100, unique=True)
    contrasena = models.CharField(max_length=100)
    nombre = models.CharField(max_length=100)
    apellidos = models.CharField(max_length=100)
    lista_amigos_ids = models.JSONField(default=list)
    
    class Meta:
        db_table = 'usuarios'
    
    def __str__(self):
        return f"{self.usuario} ({self.nombre} {self.apellidos})"


class Car(models.Model):
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name='coches')
    brand = models.CharField(max_length=100)
    model = models.CharField(max_length=100)
    year = models.CharField(max_length=10)
    city_kmpl = models.CharField(max_length=20, default="0.0")
    highway_kmpl = models.CharField(max_length=20, default="0.0")
    avg_kmpl = models.CharField(max_length=20, default="0.0")
    capacidad_deposito = models.IntegerField(default=-1)
    capacidad_actual = models.IntegerField(default=-1)
    veces_usado = models.IntegerField(default=0)
    
    class Meta:
        db_table = 'coches'
    
    def __str__(self):
        return f"{self.brand} {self.model} {self.year}"


class Viaje(models.Model):
    usuario = models.ForeignKey(Usuario, on_delete=models.CASCADE, related_name='viajes')
    origen = models.CharField(max_length=200)
    destino = models.CharField(max_length=200)
    distancia_km = models.FloatField()
    litros_consumidos = models.FloatField()
    coche_usado = models.CharField(max_length=200)
    timestamp = models.BigIntegerField()
    
    class Meta:
        db_table = 'viajes'
        ordering = ['-timestamp']
    
    def __str__(self):
        return f"{self.origen} -> {self.destino} ({self.distancia_km} km)"
