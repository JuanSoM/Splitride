@echo off
echo ======================================
echo  Iniciando servidor Django...
echo ======================================
echo El servidor estará disponible en:
echo   - Localhost: http://127.0.0.1:8000
echo   - Red local: http://0.0.0.0:8000
echo.
echo Para emulador Android, usa: http://10.0.2.2:8000
echo Para dispositivo físico, usa la IP de tu PC en la red local
echo.
echo ======================================
python manage.py runserver 0.0.0.0:8000
