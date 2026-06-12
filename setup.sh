#!/bin/bash

# ShopSphere Setup Script for Linux/Mac

echo ""
echo "=========================================="
echo "  ShopSphere - Microservices Setup"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH"
    echo "Please install Docker from https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17+ from https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

echo "Checking prerequisites..."
echo "[OK] Maven is installed"
echo "[OK] Docker is installed"
echo "[OK] Java is installed"
echo ""

# Display menu
echo "Choose an option:"
echo "1. Build all services"
echo "2. Start Docker Compose"
echo "3. Build and Start"
echo "4. Stop Docker Compose"
echo "5. Clean all (remove volumes)"
echo "6. View logs"
echo "7. Check service health"
echo "8. Exit"
echo ""

read -p "Enter your choice (1-8): " choice

case $choice in
    1)
        echo ""
        echo "Building all services..."
        mvn clean install -DskipTests
        if [ $? -ne 0 ]; then
            echo "Build failed!"
            exit 1
        fi
        echo "Build successful!"
        ;;
    2)
        echo ""
        echo "Starting Docker Compose..."
        cd docker-compose
        docker-compose up -d
        cd ..
        echo "Docker Compose started!"
        echo ""
        echo "Services starting... Please wait 30 seconds for full initialization"
        echo ""
        echo "Access points:"
        echo "- API Gateway: http://localhost:8080"
        echo "- Eureka: http://localhost:8761"
        echo "- Grafana: http://localhost:3000 (admin/admin)"
        echo "- Kibana: http://localhost:5601"
        echo "- Prometheus: http://localhost:9090"
        echo "- Zipkin: http://localhost:9411"
        ;;
    3)
        echo ""
        echo "Building all services..."
        mvn clean install -DskipTests
        if [ $? -ne 0 ]; then
            echo "Build failed!"
            exit 1
        fi
        echo "Build successful!"
        echo ""
        echo "Starting Docker Compose..."
        cd docker-compose
        docker-compose up -d
        cd ..
        echo "Docker Compose started!"
        ;;
    4)
        echo ""
        echo "Stopping Docker Compose..."
        cd docker-compose
        docker-compose down
        cd ..
        echo "Docker Compose stopped!"
        ;;
    5)
        echo ""
        echo "WARNING: This will remove all data in volumes!"
        read -p "Are you sure? (y/n): " confirm
        if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
            cd docker-compose
            docker-compose down -v
            cd ..
            echo "Docker Compose cleaned!"
        else
            echo "Cancelled."
        fi
        ;;
    6)
        echo ""
        cd docker-compose
        docker-compose logs -f
        cd ..
        ;;
    7)
        echo ""
        echo "Checking service health..."
        echo ""
        curl http://localhost:8761
        echo ""
        ;;
    8)
        echo ""
        echo "Goodbye!"
        exit 0
        ;;
    *)
        echo "Invalid choice!"
        exit 1
        ;;
esac

echo ""
read -p "Press Enter to continue..."
