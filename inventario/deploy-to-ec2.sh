#!/bin/bash

# EC2 Deployment Script for LUNARi Inventario Service
# Usage: ./deploy-to-ec2.sh <EC2_IP> <PEM_KEY_PATH>

set -e  # Exit on error

echo "========================================"
echo "LUNARi Inventario Service - EC2 Deployment"
echo "========================================"
echo ""

# Check parameters
if [ $# -ne 2 ]; then
    echo "ERROR: Invalid number of parameters!"
    echo ""
    echo "Usage: $0 <EC2_IP> <PEM_KEY_PATH>"
    echo ""
    echo "Example:"
    echo "  $0 54.123.45.67 ~/.ssh/my-key.pem"
    echo ""
    exit 1
fi

EC2_IP="$1"
PEM_KEY="$2"

# Validate PEM key exists
if [ ! -f "$PEM_KEY" ]; then
    echo "ERROR: PEM key file not found: $PEM_KEY"
    exit 1
fi

# Check if JAR exists, build if not found
JAR_FILE="target/lunari-inventory-app.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "⚠ JAR file not found. Building application..."
    echo ""
    ./mvnw clean package -DskipTests

    if [ $? -ne 0 ]; then
        echo ""
        echo "ERROR: Build failed!"
        exit 1
    fi

    echo ""
    echo "✓ Build completed successfully"
    echo ""
fi

echo "✓ JAR file found: $(ls -lh $JAR_FILE | awk '{print $5}')"
echo ""

# Upload JAR to EC2
echo "Uploading JAR to EC2 instance ($EC2_IP)..."
scp -i "$PEM_KEY" "$JAR_FILE" ec2-user@$EC2_IP:~/app.jar

echo ""
echo "✓ JAR uploaded successfully as app.jar"
echo ""

# Deploy and start application on EC2
echo "Deploying and starting application on EC2..."
echo ""

ssh -i "$PEM_KEY" ec2-user@$EC2_IP << 'ENDSSH'
set -e

echo "→ Stopping existing application..."
# Find and kill any running java -jar process
pkill -f "java -jar" || echo "  No existing process found"

echo ""
echo "→ Starting application..."
# Start the application (DB credentials from environment variables)
nohup java -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &

# Wait a bit for startup
sleep 3

# Check if process started
if pgrep -f "java -jar" > /dev/null; then
    echo ""
    echo "✓ Application started successfully!"
    echo ""
    echo "Process ID: $(pgrep -f 'java -jar')"
else
    echo ""
    echo "ERROR: Application failed to start. Check logs with: tail -f app.log"
    exit 1
fi
ENDSSH

echo ""
echo "========================================"
echo "Deployment Complete!"
echo "========================================"
echo ""
echo "Your API should be available at: http://$EC2_IP:8082"
echo ""
echo "Useful commands:"
echo "  - Check health: curl http://$EC2_IP:8082/actuator/health"
echo "  - View logs:    ssh -i \"$PEM_KEY\" ec2-user@$EC2_IP 'tail -f app.log'"
echo "  - Stop app:     ssh -i \"$PEM_KEY\" ec2-user@$EC2_IP 'pkill -f \"java -jar\"'"
echo ""
echo "Note: Make sure DB credentials are set as environment variables on EC2"
echo ""
