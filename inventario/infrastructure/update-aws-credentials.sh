#!/usr/bin/env bash

# Quick script to update AWS credentials on EC2 when they rotate
# Usage: ./update-aws-credentials.sh <ec2-host> [ssh-key]

set -e

EC2_HOST=$1
SSH_KEY=${2:-~/.ssh/id_rsa}

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

if [ -z "$EC2_HOST" ]; then
    echo -e "${RED}ERROR: EC2 host not provided${NC}"
    echo "Usage: $0 <ec2-host> [ssh-key]"
    echo "Example: $0 ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com ~/.ssh/my-key.pem"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Update AWS Credentials on EC2${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Checking local AWS credentials...${NC}"
if [ ! -f "$HOME/.aws/credentials" ]; then
    echo -e "${RED}✗ AWS credentials not found at ~/.aws/credentials${NC}"
    echo -e "${YELLOW}Get new credentials from AWS Academy${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found credentials${NC}\n"

echo -e "${YELLOW}Uploading credentials to EC2...${NC}"
ssh -o ConnectTimeout=10 -i "$SSH_KEY" "$EC2_HOST" "mkdir -p ~/.aws"
scp -o ConnectTimeout=10 -i "$SSH_KEY" "$HOME/.aws/credentials" "$EC2_HOST:~/.aws/credentials"
echo -e "${GREEN}✓ Credentials uploaded${NC}\n"

echo -e "${YELLOW}Restarting application...${NC}"
ssh -o ConnectTimeout=10 -i "$SSH_KEY" "$EC2_HOST" "pkill -f 'inventario-app.jar' || true"
sleep 2
ssh -o ConnectTimeout=10 -i "$SSH_KEY" "$EC2_HOST" 'bash -c "nohup java -jar inventario-app.jar --spring.profiles.active=prod > inventario-app.log 2>&1 < /dev/null &"'
sleep 1
echo -e "${GREEN}✓ Application restarted${NC}\n"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Credentials Updated! ✓${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Check logs:${NC}"
echo "  ssh -i $SSH_KEY $EC2_HOST 'tail -f inventario-app.log'"
