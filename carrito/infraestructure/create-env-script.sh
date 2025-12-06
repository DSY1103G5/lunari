#!/usr/bin/env bash

# Creates environment variables script for EC2 deployment
# This script will be placed in /etc/profile.d/ on the EC2 instance

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

OUTPUT_FILE="./lunari-cart-env.sh"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Environment Variables Script Creator${NC}"
echo -e "${GREEN}========================================${NC}\n"

# Prompt for values
echo -e "${YELLOW}Database Configuration${NC}"
read -p "DB_HOST (e.g., your-db.neon.tech): " DB_HOST
read -p "DB_PORT [5432]: " DB_PORT
DB_PORT=${DB_PORT:-5432}
read -p "DB_NAME [lunari_cart_db]: " DB_NAME
DB_NAME=${DB_NAME:-lunari_cart_db}
read -p "DB_USER: " DB_USER
read -sp "DB_PASSWORD: " DB_PASSWORD
echo ""

echo -e "\n${YELLOW}Microservices URLs${NC}"
echo -e "${YELLOW}Example: http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8081${NC}"
read -p "USUARIO_SERVICE_URL: " USUARIO_SERVICE_URL
read -p "INVENTARIO_SERVICE_URL: " INVENTARIO_SERVICE_URL

echo -e "\n${YELLOW}Transbank Configuration${NC}"
read -p "Environment (TEST/PROD) [TEST]: " TRANSBANK_ENVIRONMENT
TRANSBANK_ENVIRONMENT=${TRANSBANK_ENVIRONMENT:-TEST}

if [ "$TRANSBANK_ENVIRONMENT" = "TEST" ]; then
    TRANSBANK_API_KEY="597055555532"
    TRANSBANK_COMMERCE_CODE="597055555532"
    echo -e "${YELLOW}Using test credentials${NC}"
else
    read -p "TRANSBANK_API_KEY: " TRANSBANK_API_KEY
    read -p "TRANSBANK_COMMERCE_CODE: " TRANSBANK_COMMERCE_CODE
fi

# Create the environment script
cat > "$OUTPUT_FILE" << EOF
#!/bin/bash
# LUNARi Carrito Service - Environment Variables
# Generated: $(date)

# Database Configuration
export DB_HOST="$DB_HOST"
export DB_PORT="$DB_PORT"
export DB_NAME="$DB_NAME"
export DB_USER="$DB_USER"
export DB_PASSWORD="$DB_PASSWORD"

# Microservices URLs
export USUARIO_SERVICE_URL="$USUARIO_SERVICE_URL"
export INVENTARIO_SERVICE_URL="$INVENTARIO_SERVICE_URL"

# Transbank Configuration
export TRANSBANK_API_KEY="$TRANSBANK_API_KEY"
export TRANSBANK_COMMERCE_CODE="$TRANSBANK_COMMERCE_CODE"
export TRANSBANK_ENVIRONMENT="$TRANSBANK_ENVIRONMENT"
EOF

chmod 644 "$OUTPUT_FILE"

echo -e "\n${GREEN}✓ Environment script created: $OUTPUT_FILE${NC}\n"

echo -e "${YELLOW}Review the file:${NC}"
echo "  cat $OUTPUT_FILE"
echo ""

echo -e "${YELLOW}To deploy to EC2:${NC}"
echo "  ./deploy-to-ec2.sh <ec2-host> [ssh-key]"
echo ""

echo -e "${YELLOW}Manual installation on EC2:${NC}"
echo "  scp -i <key> $OUTPUT_FILE ec2-user@<host>:~/"
echo "  ssh -i <key> ec2-user@<host>"
echo "  sudo mv lunari-cart-env.sh /etc/profile.d/"
echo "  sudo chmod 644 /etc/profile.d/lunari-cart-env.sh"
echo "  source /etc/profile.d/lunari-cart-env.sh"
echo ""
