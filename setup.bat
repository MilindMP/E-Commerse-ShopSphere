@echo off
REM ShopSphere Setup Script for Windows

echo.
echo ==========================================
echo   ShopSphere - Microservices Setup
echo ==========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Docker is installed
where docker >nul 2>nul
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo Checking prerequisites...
echo [OK] Maven is installed
echo [OK] Docker is installed
echo [OK] Java is installed
echo.

REM Display menu
echo Choose an option:
echo 1. Build all services
echo 2. Start Docker Compose
echo 3. Build and Start
echo 4. Stop Docker Compose
echo 5. Clean all (remove volumes)
echo 6. View logs
echo 7. Check service health
echo 8. Exit
echo.

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto docker_up
if "%choice%"=="3" goto build_and_start
if "%choice%"=="4" goto docker_down
if "%choice%"=="5" goto docker_clean
if "%choice%"=="6" goto logs
if "%choice%"=="7" goto health_check
if "%choice%"=="8" goto end
goto invalid

:build
echo.
echo Building all services...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
echo Build successful!
pause
goto end

:docker_up
echo.
echo Starting Docker Compose...
cd docker-compose
call docker-compose up -d
cd ..
echo Docker Compose started!
echo.
echo Services starting... Please wait 30 seconds for full initialization
echo.
echo Access points:
echo - API Gateway: http://localhost:8080
echo - Eureka: http://localhost:8761
echo - Grafana: http://localhost:3000 (admin/admin)
echo - Kibana: http://localhost:5601
echo - Prometheus: http://localhost:9090
echo - Zipkin: http://localhost:9411
pause
goto end

:build_and_start
echo.
echo Building all services...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
echo Build successful!
echo.
echo Starting Docker Compose...
cd docker-compose
call docker-compose up -d
cd ..
echo Docker Compose started!
pause
goto end

:docker_down
echo.
echo Stopping Docker Compose...
cd docker-compose
call docker-compose down
cd ..
echo Docker Compose stopped!
pause
goto end

:docker_clean
echo.
echo WARNING: This will remove all data in volumes!
set /p confirm="Are you sure? (y/n): "
if /i "%confirm%"=="y" (
    cd docker-compose
    call docker-compose down -v
    cd ..
    echo Docker Compose cleaned!
) else (
    echo Cancelled.
)
pause
goto end

:logs
echo.
cd docker-compose
call docker-compose logs -f
cd ..
goto end

:health_check
echo.
echo Checking service health...
echo.
call curl http://localhost:8761
echo.
pause
goto end

:invalid
echo Invalid choice!
pause
goto end

:end
echo.
echo Goodbye!
pause
