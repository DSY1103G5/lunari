#!/bin/bash

echo "==================================="
echo "Environment Variables Verification"
echo "==================================="
echo ""

# Load .env file
if [ -f .env ]; then
    echo "✓ .env file found"
    echo ""

    # Export variables from .env
    export $(cat .env | grep -v '^#' | xargs)

    echo "Loaded environment variables:"
    echo "  DB_URL: ${DB_URL:0:50}..." # Show first 50 chars
    echo "  DB_USERNAME: $DB_USERNAME"
    echo "  DB_PASSWORD: ${DB_PASSWORD:0:5}..." # Show only first 5 chars for security
    echo "  JWT_SECRET: ${JWT_SECRET:0:20}..."
    echo "  JWT_EXPIRATION: $JWT_EXPIRATION"
    echo "  SERVER_PORT: $SERVER_PORT"
    echo ""

    # Test database connection
    echo "Testing NeonDB connection..."
    if command -v psql &> /dev/null; then
        # Extract host, port, database from JDBC URL
        DB_HOST=$(echo $DB_URL | sed -n 's|.*://\([^:]*\):.*|\1|p')
        DB_PORT=$(echo $DB_URL | sed -n 's|.*://[^:]*:\([0-9]*\)/.*|\1|p')
        DB_NAME=$(echo $DB_URL | sed -n 's|.*/\([^?]*\).*|\1|p')

        echo "  Host: $DB_HOST"
        echo "  Port: $DB_PORT"
        echo "  Database: $DB_NAME"
        echo "  Username: $DB_USERNAME"
        echo ""

        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME -c "SELECT version();" 2>&1 | head -3
    else
        echo "  psql not installed - skipping database connection test"
    fi

    echo ""
    echo "==================================="
    echo "Ready to run Spring Boot app!"
    echo "Run: ./mvnw spring-boot:run"
    echo "==================================="
else
    echo "❌ .env file not found!"
    echo "Please create .env file from .env.example"
    exit 1
fi
