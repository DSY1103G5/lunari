#!/bin/bash

# EC2 Deployment Script for LUNARi Usuario Service
# This script helps you deploy the application to an EC2 instance

echo "========================================"
echo "LUNARi Usuario Service - EC2 Deployment"
echo "========================================"
echo ""

# Check if JAR exists
if [ ! -f "target/lunari-user-api-0.0.1-SNAPSHOT.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo "Please run: ./mvnw clean package -DskipTests"
    exit 1
fi

echo "JAR file found: $(ls -lh target/lunari-user-api-0.0.1-SNAPSHOT.jar | awk '{print $5}')"
echo ""

# Get deployment details
read -p "Enter your EC2 instance IP address: " EC2_IP
read -p "Enter path to your .pem key file: " PEM_KEY

# Validate inputs
if [ -z "$EC2_IP" ] || [ -z "$PEM_KEY" ]; then
    echo "ERROR: EC2 IP and PEM key are required!"
    exit 1
fi

if [ ! -f "$PEM_KEY" ]; then
    echo "ERROR: PEM key file not found: $PEM_KEY"
    exit 1
fi

echo ""
echo "Uploading JAR to EC2 instance..."
scp -i "$PEM_KEY" target/lunari-user-api-0.0.1-SNAPSHOT.jar ec2-user@$EC2_IP:~/

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ JAR uploaded successfully!"
    echo ""
    echo "========================================"
    echo "Next Steps:"
    echo "========================================"
    echo ""
    echo "1. Connect to your EC2 instance:"
    echo "   ssh -i \"$PEM_KEY\" ec2-user@$EC2_IP"
    echo ""
    echo "2. Install Java 21 (if not already installed):"
    echo "   sudo yum install java-21-amazon-corretto-headless -y"
    echo ""
    echo "3. Run the application:"
    echo "   nohup java -jar lunari-user-api-0.0.1-SNAPSHOT.jar \\"
    echo "     --spring.profiles.active=dev \\"
    echo "     --aws.region=us-east-1 \\"
    echo "     --aws.dynamodb.tableName=lunari-users-dev > app.log 2>&1 &"
    echo ""
    echo "4. Check if it's running:"
    echo "   curl http://$EC2_IP:8080/actuator/health"
    echo ""
    echo "5. View logs:"
    echo "   tail -f app.log"
    echo ""
    echo "Your API will be available at: http://$EC2_IP:8080"
    echo ""
else
    echo "ERROR: Failed to upload JAR file!"
    exit 1
fi
