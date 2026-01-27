# 🎉 Migración Completada: JSON → Django Backend

## ✅ Resumen de Cambios

He transformado completamente tu sistema de almacenamiento:

**ANTES:**
- ❌ Datos en JSON local (`usuarios.json`)
- ❌ Sin sincronización entre dispositivos
- ❌ Sin base de datos real

**AHORA:**
- ✅ Base de datos SQLite (migratable a PostgreSQL)
- ✅ API REST completa con Django
- ✅ SaveManager adaptado para HTTP
- ✅ Sin cambios en el resto del código Android

## 📦 Archivos Creados

### Backend Django (`/backend/`)
```
backend/
├── manage.py
├── requirements.txt
├── README.md (documentación completa)
├── setup.bat (instalación automática)
├── start.bat (iniciar servidor)
├── init_users.py (crear usuarios por defecto)
├── test_api.bat (probar que funciona)
├── splitride_backend/
│   ├── settings.py
│   ├── urls.py
│   ├── wsgi.py
│   └── asgi.py
└── api/
    ├── models.py (Usuario, Car, Viaje)
    ├── serializers.py
    ├── views.py (endpoints API)
    ├── urls.py
    └── admin.py
```

### Archivos Modificados
- ✏️ `app/.../SaveManager.java` - Ahora hace llamadas HTTP
- ✏️ `app/.../AndroidManifest.xml` - Agregado `usesCleartextTraffic`

### Documentación
- 📚 `MIGRATION_GUIDE.md` - Guía técnica detallada
- 📚 `backend/README.md` - Manual de uso del backend

## 🚀 Primeros Pasos (Quick Start)

### 1️⃣ Instalar y Configurar Backend

Abre terminal en la carpeta `backend`:

```bash
# Opción A: Todo automático (Windows)
setup.bat

# Opción B: Paso a paso
pip install -r requirements.txt
python manage.py makemigrations
python manage.py migrate
python init_users.py
```

### 2️⃣ Iniciar el Servidor

```bash
start.bat
```

Verás algo como:
```
Starting development server at http://0.0.0.0:8000/
```

✅ **El servidor DEBE estar corriendo** para que la app funcione.

### 3️⃣ Verificar que Funciona

Abre otra terminal y ejecuta:

```bash
test_api.bat
```

O prueba en navegador:
- http://localhost:8000/admin/ (panel de administración)
- http://localhost:8000/api/usuarios/all/ (lista de usuarios)

### 4️⃣ Configurar Android App

**SI USAS EMULADOR:**
No necesitas hacer nada, ya está configurado con `http://10.0.2.2:8000/api/`

**SI USAS DISPOSITIVO FÍSICO:**
1. Averigua tu IP local:
   ```bash
   ipconfig
   ```
   Busca algo como `192.168.1.X`

2. Abre [SaveManager.java](app/src/main/java/com/example/consumocarros/SaveManager.java)

3. Cambia la línea 25:
   ```java
   private static final String BASE_URL = "http://TU_IP:8000/api/";
   ```

4. Asegúrate que tu PC y móvil están en la misma red WiFi

### 5️⃣ Ejecutar la App

1. ✅ Verifica que el servidor Django está corriendo
2. ▶️ Compila y ejecuta la app Android
3. 🔐 Login con: `daniel123` / `12345`
4. 👀 Revisa Logcat filtrando por "SaveManager" para ver logs

## 🎯 Funcionalidades Disponibles

### API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/usuarios/login/` | Login de usuario |
| GET | `/api/usuarios/all/` | Listar todos los usuarios |
| GET | `/api/usuarios/{id}/` | Obtener usuario específico |
| PUT | `/api/usuarios/{id}/update/` | Actualizar usuario completo |
| POST | `/api/usuarios/{id}/coches/` | Agregar coche |
| POST | `/api/usuarios/{id}/viajes/` | Agregar viaje |
| POST | `/api/usuarios/{id}/amigos/` | Agregar amigo |

### Usuarios de Prueba

| Usuario | Contraseña | Nombre |
|---------|------------|--------|
| daniel123 | 12345 | Daniel ApellidoDaniel |
| marta123 | 12345 | Marta ApellidoMarta |
| juan123 | 12345 | Juan ApellidoJuan |

## 🔍 Cómo Saber si Funciona

### Backend OK ✅
```bash
# Terminal donde ejecutaste start.bat muestra:
[27/Jan/2026 10:30:00] "GET /api/usuarios/all/ HTTP/1.1" 200
[27/Jan/2026 10:30:05] "POST /api/usuarios/login/ HTTP/1.1" 200
```

### Android OK ✅
```
Logcat (filtro: SaveManager):
D/SaveManager: Usuario actualizado correctamente: daniel123
```

### Error ❌
```
E/SaveManager: Error en obtenerUsuario: Connection refused
```
→ El servidor no está corriendo o la URL es incorrecta

## 📱 Compatibilidad

### Sin Cambios en:
- ✅ LoginActivity
- ✅ MainActivity
- ✅ DepositoActivity
- ✅ HistorialActivity
- ✅ SocialActivity
- ✅ MisCochesActivity
- ✅ Clase Usuario
- ✅ Clases Car y Viaje

**Todo sigue funcionando exactamente igual desde el punto de vista del código Android.**

## 🐛 Solución de Problemas

### "Connection refused" o "Network error"
```
PROBLEMA: La app no puede conectarse al servidor
SOLUCIÓN:
1. Verifica que start.bat está corriendo
2. Comprueba la URL en SaveManager.java:
   - Emulador: http://10.0.2.2:8000/api/
   - Físico: http://TU_IP_LOCAL:8000/api/
3. Verifica que PC y móvil están en la misma WiFi (físico)
```

### "Usuario no encontrado"
```
PROBLEMA: Login falla
SOLUCIÓN:
1. Ejecuta: python init_users.py
2. Verifica en http://localhost:8000/admin/
3. Usa las credenciales correctas (daniel123 / 12345)
```

### Datos no se guardan
```
PROBLEMA: Cambios en la app no persisten
SOLUCIÓN:
1. actualizarUsuario() es asíncrono, espera unos segundos
2. Revisa Logcat para errores
3. Verifica en Django Admin si llegaron los datos
```

### "Cleartext HTTP traffic not permitted"
```
PROBLEMA: Android bloquea HTTP
SOLUCIÓN:
Ya está resuelto con android:usesCleartextTraffic="true"
Si persiste, limpia cache: Build > Clean Project
```

## 🎓 Próximos Pasos Recomendados

### Corto Plazo
1. ✅ Probar login y funcionalidades básicas
2. ✅ Verificar que coches y viajes se guardan
3. ✅ Probar sistema de amigos

### Mediano Plazo
- 🔐 Hashear contraseñas (usar bcrypt)
- 🎨 Personalizar panel Django Admin
- 📊 Agregar estadísticas de uso
- 🧪 Tests automatizados

### Largo Plazo
- 🗄️ Migrar a PostgreSQL
- 🌐 Deploy en servidor real (Heroku, AWS, etc.)
- 🔒 HTTPS y autenticación JWT
- 📱 Versión web del frontend
- 🔄 WebSockets para sincronización real-time

## 📚 Documentación Extra

- **Guía técnica completa:** [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md)
- **Manual del backend:** [backend/README.md](backend/README.md)
- **Django Docs:** https://docs.djangoproject.com/
- **DRF Docs:** https://www.django-rest-framework.org/

## 💡 Tips Útiles

### Reiniciar Base de Datos
Si necesitas empezar de cero:
```bash
cd backend
del db.sqlite3
python manage.py migrate
python init_users.py
```

### Ver Logs del Servidor
La terminal donde ejecutaste `start.bat` muestra todas las peticiones HTTP.

### Debug Android
```bash
adb logcat | findstr SaveManager
```

### Acceder desde otros dispositivos
El servidor en `0.0.0.0:8000` es accesible desde cualquier dispositivo en tu red local.

## ✨ Características del Nuevo Sistema

1. **API REST estándar** - Puedes consumirla desde cualquier cliente
2. **Base de datos real** - SQLite (desarrollo) o PostgreSQL (producción)
3. **Panel admin** - Gestiona datos desde el navegador
4. **Escalable** - Soporta múltiples usuarios simultáneos
5. **Mantenible** - Código limpio y bien documentado
6. **Sin breaking changes** - Tu app Android funciona sin modificaciones

---

## 🎊 ¡Todo Listo!

Tu aplicación ahora tiene un backend profesional. Los pasos son:

1. `cd backend`
2. `setup.bat` (solo una vez)
3. `python init_users.py` (solo una vez)
4. `start.bat` (cada vez que desarrolles)
5. Ejecutar app Android

**¡Disfruta tu nueva arquitectura cliente-servidor!** 🚀