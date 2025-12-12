#!/usr/bin/env bash

# NeonDB Secrets Setup Script for Google Cloud Platform
# Usage: ./setup-neondb-secrets.sh
#
# This script helps you store NeonDB connection details in Google Secret Manager

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  NeonDB Secrets Setup for GCP${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${BLUE}This script will help you store NeonDB credentials in Google Secret Manager.${NC}"
echo -e "${BLUE}You'll need the connection strings from https://console.neon.tech${NC}\n"

# Function to parse connection string
parse_connection_string() {
    local conn_string="$1"
    local service_name="$2"

    # Extract components
    DB_HOST=$(echo "$conn_string" | sed -n 's/.*@\([^/]*\)\/.*/\1/p')
    DB_USER=$(echo "$conn_string" | sed -n 's/.*\/\/\([^:]*\):.*/\1/p')
    DB_PASSWORD=$(echo "$conn_string" | sed -n 's/.*\/\/[^:]*:\([^@]*\)@.*/\1/p')
    DB_NAME=$(echo "$conn_string" | sed -n 's/.*\/\([^?]*\).*/\1/p')

    if [ -z "$DB_HOST" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ] || [ -z "$DB_NAME" ]; then
        echo -e "${RED}Error: Could not parse connection string${NC}"
        echo -e "${YELLOW}Expected format: postgres://user:password@host/dbname?sslmode=require${NC}"
        return 1
    fi

    echo -e "${YELLOW}Parsed connection details:${NC}"
    echo "  Host: $DB_HOST"
    echo "  Database: $DB_NAME"
    echo "  User: $DB_USER"
    echo "  Password: ${DB_PASSWORD:0:4}****"
    echo ""

    # Create secrets
    echo -e "${BLUE}Creating secrets for $service_name service...${NC}"

    echo -n "$DB_HOST" | gcloud secrets create db-host-$service_name --data-file=- 2>/dev/null || \
        (echo -n "$DB_HOST" | gcloud secrets versions add db-host-$service_name --data-file=-)
    echo -e "${GREEN}✓${NC} db-host-$service_name"

    echo -n "$DB_USER" | gcloud secrets create db-user-$service_name --data-file=- 2>/dev/null || \
        (echo -n "$DB_USER" | gcloud secrets versions add db-user-$service_name --data-file=-)
    echo -e "${GREEN}✓${NC} db-user-$service_name"

    echo -n "$DB_PASSWORD" | gcloud secrets create db-password-$service_name --data-file=- 2>/dev/null || \
        (echo -n "$DB_PASSWORD" | gcloud secrets versions add db-password-$service_name --data-file=-)
    echo -e "${GREEN}✓${NC} db-password-$service_name"

    echo -n "$DB_NAME" | gcloud secrets create db-name-$service_name --data-file=- 2>/dev/null || \
        (echo -n "$DB_NAME" | gcloud secrets versions add db-name-$service_name --data-file=-)
    echo -e "${GREEN}✓${NC} db-name-$service_name"

    echo ""
}

# Setup Usuario Service
echo -e "${YELLOW}=== Usuario Service ===${NC}"
echo "Enter the NeonDB connection string for the Usuario service:"
echo "(Format: postgres://user:password@host/lunari_users?sslmode=require)"
read -r USUARIO_CONNECTION

if [ -n "$USUARIO_CONNECTION" ]; then
    parse_connection_string "$USUARIO_CONNECTION" "usuario"
else
    echo -e "${RED}Skipping Usuario service${NC}\n"
fi

# Setup Inventario Service
echo -e "${YELLOW}=== Inventario Service ===${NC}"
echo "Enter the NeonDB connection string for the Inventario service:"
echo "(Format: postgres://user:password@host/lunari_inventory?sslmode=require)"
read -r INVENTARIO_CONNECTION

if [ -n "$INVENTARIO_CONNECTION" ]; then
    parse_connection_string "$INVENTARIO_CONNECTION" "inventario"
else
    echo -e "${RED}Skipping Inventario service${NC}\n"
fi

# Setup Carrito Service
echo -e "${YELLOW}=== Carrito Service ===${NC}"
echo "Enter the NeonDB connection string for the Carrito service:"
echo "(Format: postgres://user:password@host/lunari_cart?sslmode=require)"
read -r CARRITO_CONNECTION

if [ -n "$CARRITO_CONNECTION" ]; then
    parse_connection_string "$CARRITO_CONNECTION" "carrito"
else
    echo -e "${RED}Skipping Carrito service${NC}\n"
fi

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Setup Complete!${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}Created secrets:${NC}"
gcloud secrets list --filter="name:db-*-usuario OR name:db-*-inventario OR name:db-*-carrito"

echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Deploy services using: ./deploy-to-gcp.sh"
echo "  2. Or manually deploy with 'gcloud run deploy'"
echo ""

echo -e "${YELLOW}Verify secrets:${NC}"
echo "  gcloud secrets versions access latest --secret=db-host-usuario"
echo "  gcloud secrets versions access latest --secret=db-name-usuario"
echo ""

echo -e "${BLUE}Note: Secrets are stored securely in Google Secret Manager${NC}"
echo -e "${BLUE}and will be injected as environment variables at runtime.${NC}"
