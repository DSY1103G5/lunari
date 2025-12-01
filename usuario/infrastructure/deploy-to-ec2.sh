#!/bin/bash

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
echo -e "${GREEN}  LUNARi User Service - EC2 Deployment${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Target:${NC} $EC2_HOST"
echo -e "${YELLOW}SSH Key:${NC} $SSH_KEY\n"

# Check if JAR exists
JAR_PATH="../target/lunari-user-api-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}ERROR: JAR file not found at $JAR_PATH${NC}"
    echo -e "${YELLOW}Build the JAR first:${NC}"
    echo "  cd ../usuario"
    echo "  ./mvnw clean package -Dmaven.test.skip=true"
    exit 1
fi

echo -e "${YELLOW}Step 1/4:${NC} Uploading JAR to EC2..."
scp -i "$SSH_KEY" "$JAR_PATH" "$EC2_HOST:~/app.jar"
echo -e "${GREEN}✓ JAR uploaded${NC}\n"

echo -e "${YELLOW}Step 2/4:${NC} Stopping old application..."
ssh -i "$SSH_KEY" "$EC2_HOST" "pkill -f 'java -jar' || true"
echo -e "${GREEN}✓ Old app stopped${NC}\n"

echo -e "${YELLOW}Step 3/4:${NC} Starting application with dev profile..."
ssh -i "$SSH_KEY" "$EC2_HOST" "nohup java -jar app.jar --spring.profiles.active=dev > app.log 2>&1 &"
echo -e "${GREEN}✓ Application started${NC}\n"

echo -e "${YELLOW}Step 4/4:${NC} Waiting for startup (10 seconds)..."
sleep 10

echo -e "\n${YELLOW}Checking application status...${NC}"
if ssh -i "$SSH_KEY" "$EC2_HOST" "curl -s -f http://localhost:8080/api/v1/users > /dev/null"; then
    echo -e "${GREEN}✓ Application is running!${NC}\n"

    # Extract hostname from EC2_HOST
    HOSTNAME=$(echo "$EC2_HOST" | cut -d'@' -f2)

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Deployment Complete! 🎉${NC}"
    echo -e "${GREEN}========================================${NC}\n"

    echo -e "${YELLOW}API Endpoints:${NC}"
    echo "  http://$HOSTNAME:8080/api/v1/users"
    echo "  http://$HOSTNAME:8080/swagger-ui/index.html"
    echo ""

    echo -e "${YELLOW}Test Commands:${NC}"
    echo "  curl http://$HOSTNAME:8080/api/v1/users | jq"
    echo "  curl http://$HOSTNAME:8080/api/v1/users/1 | jq"
    echo ""

    echo -e "${YELLOW}View Logs:${NC}"
    echo "  ssh -i $SSH_KEY $EC2_HOST 'tail -f app.log'"
    echo ""

    echo -e "${YELLOW}Sample response preview:${NC}"
    ssh -i "$SSH_KEY" "$EC2_HOST" "curl -s http://localhost:8080/api/v1/users | head -30"
    echo ""
else
    echo -e "${RED}✗ Application may not be running correctly${NC}\n"
    echo -e "${YELLOW}Check logs on EC2:${NC}"
    echo "  ssh -i $SSH_KEY $EC2_HOST 'tail -50 app.log'"
    exit 1
fi
