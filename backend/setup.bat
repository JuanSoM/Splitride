@echo off
echo ======================================
echo  Instalando dependencias...
echo ======================================
pip install -r requirements.txt

echo.
echo ======================================
echo  Creando migraciones de base de datos...
echo ======================================
python manage.py makemigrations

echo.
echo ======================================
echo  Aplicando migraciones...
echo ======================================
python manage.py migrate

echo.
echo ======================================
echo  Creando superusuario (opcional)
echo ======================================
echo Si quieres acceder al panel de administración, crea un superusuario ahora.
echo Pulsa Ctrl+C para saltar este paso.
python manage.py createsuperuser

echo.
echo ======================================
echo  Setup completo!
echo ======================================
pause
