#!/bin/bash

# LocalStack Initialization Script
# Automatically creates DynamoDB table when LocalStack starts

echo "=========================================="
echo "Initializing LocalStack DynamoDB"
echo "=========================================="

# Wait for LocalStack to be ready
sleep 5

# Create DynamoDB table
echo "Creating lunari-users-dev table..."

awslocal dynamodb create-table \
    --table-name lunari-users-dev \
    --billing-mode PAY_PER_REQUEST \
    --attribute-definitions \
        AttributeName=userId,AttributeType=S \
        AttributeName=email,AttributeType=S \
        AttributeName=roleName,AttributeType=S \
        AttributeName=isActiveUserId,AttributeType=S \
    --key-schema \
        AttributeName=userId,KeyType=HASH \
    --global-secondary-indexes \
        "[
            {
                \"IndexName\": \"EmailIndex\",
                \"KeySchema\": [{\"AttributeName\":\"email\",\"KeyType\":\"HASH\"}],
                \"Projection\": {\"ProjectionType\":\"ALL\"}
            },
            {
                \"IndexName\": \"RoleActiveIndex\",
                \"KeySchema\": [
                    {\"AttributeName\":\"roleName\",\"KeyType\":\"HASH\"},
                    {\"AttributeName\":\"isActiveUserId\",\"KeyType\":\"RANGE\"}
                ],
                \"Projection\": {\"ProjectionType\":\"ALL\"}
            }
        ]"

if [ $? -eq 0 ]; then
    echo "✓ Table created successfully"
else
    echo "✗ Failed to create table"
    exit 1
fi

# Enable DynamoDB streams (optional - for auditing)
echo "Enabling DynamoDB streams..."
awslocal dynamodb update-table \
    --table-name lunari-users-dev \
    --stream-specification \
        StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES

# Create a test user
echo "Creating test user..."
awslocal dynamodb put-item \
    --table-name lunari-users-dev \
    --item '{
        "userId": {"S": "00000000-0000-0000-0000-000000000001"},
        "email": {"S": "admin@lunari.com"},
        "firstName": {"S": "Admin"},
        "lastName": {"S": "User"},
        "password": {"S": "$2a$10$example_hashed_password"},
        "phone": {"S": "+56912345678"},
        "profileImg": {"S": ""},
        "role": {"M": {
            "roleId": {"N": "1"},
            "roleName": {"S": "ADMIN"},
            "roleDescription": {"S": "Administrator with full system access"}
        }},
        "roleName": {"S": "ADMIN"},
        "isActive": {"BOOL": true},
        "isVerified": {"BOOL": true},
        "isActiveUserId": {"S": "true#00000000-0000-0000-0000-000000000001"},
        "level": {"N": "1"},
        "points": {"N": "0"},
        "createdAt": {"S": "2025-01-01T00:00:00Z"},
        "updatedAt": {"S": "2025-01-01T00:00:00Z"},
        "memberSince": {"S": "2025-01-01T00:00:00Z"},
        "purchases": {"M": {
            "totalCount": {"N": "0"},
            "totalSpent": {"S": "0.00"}
        }},
        "reviews": {"M": {
            "totalCount": {"N": "0"}
        }},
        "favorites": {"M": {
            "serviceIds": {"L": []},
            "count": {"N": "0"}
        }},
        "preferences": {"M": {
            "language": {"S": "es"},
            "currency": {"S": "CLP"},
            "notifications": {"M": {
                "email": {"BOOL": true},
                "sms": {"BOOL": false},
                "push": {"BOOL": true}
            }}
        }},
        "metadata": {"M": {}}
    }'

echo ""
echo "=========================================="
echo "LocalStack DynamoDB Ready!"
echo "=========================================="
echo "Table: lunari-users-dev"
echo "Endpoint: http://localhost:4566"
echo "Admin UI: http://localhost:8001"
echo ""
echo "Test user created:"
echo "  Email: admin@lunari.com"
echo "  User ID: 00000000-0000-0000-0000-000000000001"
echo "=========================================="
