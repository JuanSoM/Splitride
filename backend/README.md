# SplitRide Backend API

API REST con Django para SplitRide. Sustituye el almacenamiento JSON local por una base de datos real.

## 🚀 Instalación Rápida

### Opción 1: Script Automático (Windows)
```bash
# 1. Ejecutar el script de setup
setup.bat

# 2. Crear usuarios por defecto
python init_users.py

# 3. Iniciar el servidor
start.bat
```

### Opción 2: Instalación Manual

1. **Instalar dependencias:**
```bash
pip install -r requirements.txt
```

2. **Crear la base de datos:**
```bash
python manage.py makemigrations
python manage.py migrate
```

3. **Crear usuarios por defecto:**
```bash
python init_users.py
```

4. **[Opcional] Crear superusuario para el panel admin:**
```bash
python manage.py createsuperuser
```

5. **Ejecutar el servidor:**
```bash
python manage.py runserver 0.0.0.0:8000
```

## 🔧 Configuración en Android

### 1. Actualizar la URL en SaveManager.java

Abre [SaveManager.java](../app/src/main/java/com/example/consumocarros/SaveManager.java) y modifica la constante `BASE_URL`:

**Para emulador Android:**
```java
private static final String BASE_URL = "http://10.0.2.2:8000/api/";
```

**Para dispositivo físico en red local:**
```java
private static final String BASE_URL = "http://TU_IP_LOCAL:8000/api/";
```

Para encontrar tu IP local:
- Windows: `ipconfig` (busca IPv4)
- Mac/Linux: `ifconfig` o `ip addr`

### 2. Verificar Permisos

El AndroidManifest.xml ya incluye:
- `INTERNET` - Para hacer llamadas HTTP
- `usesCleartextTraffic="true"` - Para permitir HTTP en desarrollo

## 📡 API Endpoints

### Autenticación
- `POST /api/usuarios/login/` - Login de usuario
  ```json
  {
    "usuario": "daniel123",
    "contrasena": "12345"
  }
  ```

### Usuarios
- `GET /api/usuarios/all/` - Listar todos los usuarios
- `GET /api/usuarios/{id_usuario}/` - Obtener usuario específico
- `PUT /api/usuarios/{id_usuario}/update/` - Actualizar usuario completo
- `POST /api/usuarios/{id_usuario}/coches/` - Agregar coche a usuario
- `POST /api/usuarios/{id_usuario}/viajes/` - Agregar viaje a usuario
- `POST /api/usuarios/{id_usuario}/amigos/` - Agregar amigo
- `PUT /api/usuarios/{id_usuario}/coches/{car_id}/` - Actualizar coche específico

### Panel de Administración
Accede a `http://localhost:8000/admin/` para gestionar usuarios, coches y viajes desde el navegador.

## 🗄️ Estructura de Base de Datos

### Usuario
- `id_usuario` (PK) - ID único generado automáticamente
- `usuario` - Nombre de usuario (único)
- `contrasena` - Contraseña (en producción usar hash)
- `nombre` - Nombre real
- `apellidos` - Apellidos
- `lista_amigos_ids` - JSON array con IDs de amigos

### Car (Coche)
- `id` (PK)
- `usuario` (FK) - Relación con Usuario
- `brand` - Marca
- `model` - Modelo
- `year` - Año
- `city_kmpl` - Consumo ciudad
- `highway_kmpl` - Consumo carretera
- `avg_kmpl` - Consumo promedio
- `capacidad_deposito` - Litros del depósito
- `capacidad_actual` - Nivel actual de combustible
- `veces_usado` - Contador de uso

### Viaje
- `id` (PK)
- `usuario` (FK) - Relación con Usuario
- `origen` - Punto de partida
- `destino` - Destino
- `distancia_km` - Distancia en kilómetros
- `litros_consumidos` - Combustible usado
- `coche_usado` - Nombre del coche
- `timestamp` - Momento del viaje

## 🎯 Usuarios Por Defecto

El sistema incluye 3 usuarios de prueba:

| Usuario | Contraseña | Nombre |
|---------|------------|--------|
| daniel123 | 12345 | Daniel ApellidoDaniel |
| marta123 | 12345 | Marta ApellidoMarta |
| juan123 | 12345 | Juan ApellidoJuan |

## 🔍 Testing

### Probar la API con cURL:

**Login:**
```bash
curl -X POST http://localhost:8000/api/usuarios/login/ \
  -H "Content-Type: application/json" \
  -d "{\"usuario\":\"daniel123\",\"contrasena\":\"12345\"}"
```

**Listar usuarios:**
```bash
curl http://localhost:8000/api/usuarios/all/
```

## 📝 Notas Importantes

1. **SaveManager ahora es asíncrono** - Las operaciones de actualización se ejecutan en segundo plano
2. **La sincronización es automática** - Cada vez que actualizas un usuario, se sincronizan coches y viajes
3. **Compatibilidad total** - La interfaz de SaveManager se mantiene igual, el resto del código Android no necesita cambios
4. **Logs útiles** - Revisa Logcat con el tag "SaveManager" para debugging

## 🚧 Producción

Para producción, considera:
- Usar PostgreSQL o MySQL en lugar de SQLite
- Implementar autenticación JWT o Token
- Hashear contraseñas con bcrypt
- Configurar HTTPS
- Usar variables de entorno para configuraciones sensibles
- Implementar rate limiting

## 📚 Stack Tecnológico

- **Backend:** Django 4.2.8
- **API:** Django REST Framework 3.14.0
- **CORS:** django-cors-headers 4.3.1
- **Base de Datos:** SQLite (desarrollo) / PostgreSQL (producción recomendada)
- **Cliente:** Android con HttpURLConnection + Gson
