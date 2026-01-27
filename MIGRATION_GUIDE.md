# 🎯 Guía de Migración: JSON → Django API

## ✅ Cambios Realizados

### Backend (Nuevo)
Se ha creado un servidor Django completo en `/backend/`:

1. **Modelos de datos** (`api/models.py`):
   - `Usuario` - Almacena usuarios con sus credenciales
   - `Car` - Coches de cada usuario (relación ForeignKey)
   - `Viaje` - Historial de viajes (relación ForeignKey)

2. **API REST** (`api/views.py`):
   - Login de usuarios
   - CRUD completo de usuarios
   - Gestión de coches y viajes
   - Sistema de amigos

3. **Scripts de ayuda**:
   - `setup.bat` - Instalación automática
   - `start.bat` - Iniciar servidor
   - `init_users.py` - Crear usuarios por defecto

### Android (Modificado)

#### SaveManager.java
**ANTES:** Guardaba datos en `usuarios.json` localmente
**AHORA:** Hace llamadas HTTP al servidor Django

**Cambios principales:**
- Usa `HttpURLConnection` para comunicarse con la API
- Ejecuta operaciones en segundo plano con `ExecutorService`
- Mantiene la misma interfaz pública → NO ROMPE código existente
- Logs detallados con el tag "SaveManager"

**Métodos actualizados:**
- ✅ `cargarUsuarios()` - GET a `/api/usuarios/all/`
- ✅ `obtenerUsuario()` - POST a `/api/usuarios/login/`
- ✅ `actualizarUsuario()` - PUT a `/api/usuarios/{id}/update/`
- ✅ `guardarUsuario()` - Alias de actualizarUsuario()
- ⚠️ `guardarUsuarios()` - Deprecado (se usa actualización individual)
- ⚠️ `inicializarUsuariosPorDefecto()` - Ahora se hace desde Django

#### AndroidManifest.xml
- ✅ Permiso `INTERNET` ya existía
- ✅ Agregado `android:usesCleartextTraffic="true"` para permitir HTTP en desarrollo

## 🚀 Cómo Empezar

### 1. Configurar el Backend

```bash
cd backend
setup.bat        # Instala dependencias y crea BD
python init_users.py    # Crea usuarios por defecto
start.bat        # Inicia el servidor en http://localhost:8000
```

### 2. Configurar la App Android

Abre `SaveManager.java` y verifica/actualiza la URL:

**Si usas emulador:**
```java
private static final String BASE_URL = "http://10.0.2.2:8000/api/";
```

**Si usas dispositivo físico:**
```java
private static final String BASE_URL = "http://192.168.X.X:8000/api/";  // Tu IP local
```

### 3. Ejecutar la App

1. Asegúrate que el servidor Django está corriendo
2. Compila y ejecuta la app Android
3. Revisa Logcat filtrando por "SaveManager" para ver logs de red

## 🔍 Verificación

### Probar el Backend
Abre en navegador: http://localhost:8000/admin/

### Probar Login desde Android
En Logcat deberías ver:
```
D/SaveManager: Usuario actualizado correctamente: daniel123
```

Si ves errores:
```
E/SaveManager: Error al actualizar usuario: 500
```
Revisa que el servidor Django esté corriendo y la URL sea correcta.

## 📊 Flujo de Datos

### ANTES (JSON local)
```
App → SaveManager → usuarios.json → Disco local
```

### AHORA (API REST)
```
App → SaveManager → HTTP Request → Django API → SQLite Database
```

## ⚡ Ventajas de la Migración

1. **Base de datos real** - SQLite con capacidad de migrar a PostgreSQL
2. **API REST estándar** - Fácil de integrar con web u otras apps
3. **Panel de administración** - Django Admin para gestionar datos
4. **Escalable** - Puede manejar múltiples usuarios simultáneos
5. **Sincronización** - Datos accesibles desde múltiples dispositivos
6. **Sin cambios en Activities** - LoginActivity, DepositoActivity, etc. funcionan igual

## 🐛 Troubleshooting

### Error: "Connection refused"
- ✅ Verifica que el servidor Django esté corriendo
- ✅ Comprueba que la URL en SaveManager sea correcta
- ✅ Si usas emulador, usa `10.0.2.2` no `localhost`

### Error: "Cleartext HTTP traffic not permitted"
- ✅ Verifica que AndroidManifest.xml tenga `android:usesCleartextTraffic="true"`

### Login no funciona
- ✅ Asegúrate de haber ejecutado `python init_users.py`
- ✅ Revisa el panel admin: http://localhost:8000/admin/
- ✅ Comprueba Logcat para ver el error exacto

### Datos no se guardan
- ✅ `actualizarUsuario()` es asíncrono, puede tardar unos segundos
- ✅ Revisa Logcat para ver si hay errores de red
- ✅ Verifica en Django Admin si los datos llegaron al servidor

## 🔐 Seguridad (Producción)

Para un entorno real:
1. **Hashear contraseñas** - Usar bcrypt o similares
2. **HTTPS obligatorio** - Quitar `usesCleartextTraffic`
3. **Autenticación con tokens** - JWT o Token de DRF
4. **Rate limiting** - Prevenir ataques
5. **Validación estricta** - En serializadores

## 📁 Estructura de Archivos

```
Splitride/
├── backend/                      # ✨ NUEVO - Servidor Django
│   ├── manage.py
│   ├── requirements.txt
│   ├── setup.bat                # Script de instalación
│   ├── start.bat                # Script para iniciar servidor
│   ├── init_users.py            # Crear usuarios por defecto
│   ├── splitride_backend/
│   │   ├── settings.py
│   │   └── urls.py
│   └── api/
│       ├── models.py            # Usuario, Car, Viaje
│       ├── serializers.py
│       ├── views.py             # API endpoints
│       └── urls.py
│
└── app/
    └── src/main/
        ├── AndroidManifest.xml  # ✏️ MODIFICADO - cleartext traffic
        └── java/com/example/consumocarros/
            └── SaveManager.java # ✏️ MODIFICADO - HTTP en lugar de JSON
```

## 🎓 Próximos Pasos Opcionales

1. **Migrar a PostgreSQL** - Mejor para producción
2. **Implementar caché** - Redis para mejorar rendimiento
3. **WebSockets** - Para sincronización en tiempo real
4. **Autenticación social** - Google, Facebook login
5. **Tests automatizados** - Unit tests para API
6. **CI/CD** - Deploy automático
7. **Dockerizar** - Facilitar despliegue

## 💡 Tips

- **Debug en Django**: Revisa `backend/db.sqlite3` con DB Browser
- **Debug en Android**: Usa filtro Logcat → "SaveManager"
- **Test rápido API**: Usa Postman o cURL
- **Panel Admin**: http://localhost:8000/admin/ (después de crear superusuario)

---

**¿Preguntas?** Revisa los logs en ambos lados (Django console y Android Logcat) para diagnosticar problemas.