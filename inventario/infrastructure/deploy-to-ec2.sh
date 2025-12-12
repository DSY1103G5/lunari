#!/usr/bin/env bash

# Quick Deploy Script for EC2
# Usage: ./deploy-to-ec2.sh <ec2-host> [ssh-key]
# Example: ./deploy-to-ec2.sh ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com ~/.ssh/my-key.pem

set -e

EC2_HOST=$1
SSH_KEY=${2:-~/.ssh/id_rsa}

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

if [ -z "$EC2_HOST" ]; then
    echo -e "${RED}ERROR: EC2 host not provided${NC}"
    echo "Usage: $0 <ec2-host> [ssh-key]"
    echo "Example: $0 ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com ~/.ssh/my-key.pem"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  LUNARi Inventory Service - EC2 Deployment${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Target:${NC} $EC2_HOST"
echo -e "${YELLOW}SSH Key:${NC} $SSH_KEY\n"

# Check if JAR exists
JAR_PATH="../target/lunari-inventory-api-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}ERROR: JAR file not found at $JAR_PATH${NC}"
    echo -e "${YELLOW}Build the JAR first:${NC}"
    echo "  cd ../inventario"
    echo "  ./mvnw clean package -Dmaven.test.skip=true"
    exit 1
fi

echo -e "${YELLOW}Step 1/5:${NC} Uploading JAR to EC2..."
scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -i "$SSH_KEY" "$JAR_PATH" "$EC2_HOST:~/app.jar" || {
    echo -e "${RED}✗ Failed to upload JAR to EC2${NC}"
    echo -e "${YELLOW}Check:${NC}"
    echo "  1. EC2 instance is running"
    echo "  2. Security group allows SSH (port 22)"
    echo "  3. SSH key has correct permissions: chmod 400 $SSH_KEY"
    echo "  4. EC2 host format is correct: ec2-user@<public-ip-or-dns>"
    exit 1
}
echo -e "${GREEN}✓ JAR uploaded${NC}\n"

echo -e "${YELLOW}Step 2/5:${NC} Uploading AWS credentials to EC2..."
# Copy AWS credentials from local machine to EC2
if [ -f "$HOME/.aws/credentials" ]; then
    ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -i "$SSH_KEY" "$EC2_HOST" "mkdir -p ~/.aws" || true
    scp -o ConnectTimeout=10 -o StrictHostKeyChecking=no -i "$SSH_KEY" "$HOME/.aws/credentials" "$EC2_HOST:~/.aws/credentials" || {
        echo -e "${YELLOW}⚠ Could not upload AWS credentials${NC}"
    }
    echo -e "${GREEN}✓ AWS credentials uploaded${NC}\n"
else
    echo -e "${RED}✗ AWS credentials not found at ~/.aws/credentials${NC}"
    echo -e "${YELLOW}Run 'aws configure' first or get credentials from AWS Academy${NC}\n"
    exit 1
fi

echo -e "${YELLOW}Step 3/5:${NC} Stopping old application..."
ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -i "$SSH_KEY" "$EC2_HOST" "pkill -f 'java -jar' || true" || {
    echo -e "${YELLOW}⚠ Could not connect or no old app was running${NC}"
}
echo -e "${GREEN}✓ Old app stopped (if it was running)${NC}\n"

echo -e "${YELLOW}Step 4/5:${NC} Starting application with prod profile..."
ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no -i "$SSH_KEY" "$EC2_HOST" 'bash -c "nohup java -jar app.jar --spring.profiles.active=prod > app.log 2>&1 < /dev/null &"' || {
    echo -e "${RED}✗ Failed to start application${NC}"
    exit 1
}
sleep 1
echo -e "${GREEN}✓ Application started${NC}\n"

echo -e "${YELLOW}Step 5/5:${NC} Waiting for startup (10 seconds)..."
sleep 10

echo -e "\n${YELLOW}Checking application status...${NC}"
if ssh -o ConnectTimeout=10 -i "$SSH_KEY" "$EC2_HOST" "curl -s -f http://localhost:8082/swagger-ui/index.html > /dev/null 2>&1"; then
    echo -e "${GREEN}✓ Application is running!${NC}\n"

    # Extract hostname from EC2_HOST
    HOSTNAME=$(echo "$EC2_HOST" | cut -d'@' -f2)

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Deployment Complete! 🎉${NC}"
    echo -e "${GREEN}========================================${NC}\n"

    echo -e "${YELLOW}API Endpoints:${NC}"
    echo "  http://$HOSTNAME:8082/api/v1/productos"
    echo "  http://$HOSTNAME:8082/api/v1/categorias"
    echo "  http://$HOSTNAME:8082/swagger-ui/index.html"
    echo ""

    echo -e "${YELLOW}View Logs:${NC}"
    echo "  ssh -i $SSH_KEY $EC2_HOST 'tail -f app.log'"
    echo ""
else
    echo -e "${YELLOW}⚠ Application may still be starting up...${NC}\n"
    echo -e "${YELLOW}Check logs on EC2:${NC}"
    echo "  ssh -i $SSH_KEY $EC2_HOST 'tail -50 app.log'"
    echo ""
    echo -e "${YELLOW}If it's not running, check:${NC}"
    echo "  1. Java 21 is installed: ssh -i $SSH_KEY $EC2_HOST 'java -version'"
    echo "  2. AWS credentials are set in .env.prod on EC2"
    echo "  3. Security group allows port 8080"
fi
