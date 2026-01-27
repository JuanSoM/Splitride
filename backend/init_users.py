#!/usr/bin/env python
"""
Script para inicializar usuarios por defecto en la base de datos
"""
import os
import sys
import django

# Configurar Django
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'splitride_backend.settings')
django.setup()

from api.models import Usuario

def crear_usuarios_defecto():
    """Crea los usuarios por defecto si no existen"""
    
    usuarios_defecto = [
        {
            'usuario': 'daniel123',
            'contrasena': '12345',
            'nombre': 'Daniel',
            'apellidos': 'ApellidoDaniel'
        },
        {
            'usuario': 'marta123',
            'contrasena': '12345',
            'nombre': 'Marta',
            'apellidos': 'ApellidoMarta'
        },
        {
            'usuario': 'juan123',
            'contrasena': '12345',
            'nombre': 'Juan',
            'apellidos': 'ApellidoJuan'
        }
    ]
    
    for user_data in usuarios_defecto:
        usuario, created = Usuario.objects.get_or_create(
            usuario=user_data['usuario'],
            defaults={
                'contrasena': user_data['contrasena'],
                'nombre': user_data['nombre'],
                'apellidos': user_data['apellidos']
            }
        )
        
        if created:
            print(f"✓ Usuario creado: {usuario.usuario} ({usuario.nombre} {usuario.apellidos})")
        else:
            print(f"• Usuario ya existe: {usuario.usuario}")
    
    print(f"\n✓ Total de usuarios en la base de datos: {Usuario.objects.count()}")

if __name__ == '__main__':
    print("=" * 50)
    print("Inicializando usuarios por defecto...")
    print("=" * 50)
    crear_usuarios_defecto()
    print("=" * 50)
    print("¡Completado!")
