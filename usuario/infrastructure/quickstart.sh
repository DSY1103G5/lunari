#!/bin/bash

# Quick Start Script for LUNARi User Service with DynamoDB
# This script sets up local development environment with LocalStack

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}LUNARi User Service - Quick Start${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi
echo "✅ Docker installed"

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
echo "✅ Docker Compose installed"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi
echo "✅ Maven installed"

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 first."
    exit 1
fi
echo "✅ Java installed"

echo ""
echo -e "${YELLOW}Starting LocalStack...${NC}"
docker-compose -f docker-compose.localstack.yml up -d

echo ""
echo -e "${YELLOW}Waiting for LocalStack to be ready...${NC}"
sleep 10

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}LocalStack is ready!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "📊 DynamoDB Admin UI: http://localhost:8001"
echo "🔌 LocalStack Endpoint: http://localhost:4566"
echo ""
echo -e "${YELLOW}Building application...${NC}"
cd ..
mvn clean package -DskipTests

echo ""
echo -e "${YELLOW}Starting application...${NC}"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Application Started!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "🚀 API: http://localhost:8080"
echo "📚 Swagger UI: http://localhost:8080/swagger-ui.html"
echo "❤️  Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Test User:"
echo "  Email: admin@lunari.com"
echo "  User ID: 00000000-0000-0000-0000-000000000001"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop the application${NC}"
echo ""

java -jar target/usuario-*.jar --spring.profiles.active=local
