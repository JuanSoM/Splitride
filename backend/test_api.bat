@echo off
echo ======================================
echo  Test del Backend SplitRide
echo ======================================
echo.

echo 1. Verificando si el servidor esta corriendo...
curl -s http://localhost:8000/api/usuarios/all/ > nul
if %errorlevel% neq 0 (
    echo [ERROR] El servidor NO esta corriendo
    echo Por favor, ejecuta start.bat primero
    pause
    exit /b 1
)
echo [OK] Servidor corriendo

echo.
echo 2. Probando endpoint de usuarios...
curl -X GET http://localhost:8000/api/usuarios/all/
echo.

echo.
echo 3. Probando login con daniel123...
curl -X POST http://localhost:8000/api/usuarios/login/ -H "Content-Type: application/json" -d "{\"usuario\":\"daniel123\",\"contrasena\":\"12345\"}"
echo.

echo.
echo ======================================
echo  Tests completados
echo ======================================
echo.
echo Si ves datos JSON arriba, todo funciona correctamente!
echo Si ves errores, revisa que:
echo   1. El servidor este corriendo (start.bat)
echo   2. Hayas creado los usuarios (python init_users.py)
echo.
pause
