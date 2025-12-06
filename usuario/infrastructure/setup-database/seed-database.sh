#!/usr/bin/env bash

# DynamoDB Seed Script - Shell version
# Populates the lunari-users DynamoDB table with sample client data

set -e

# Configuration
ENV=${1:-dev}
REGION=${AWS_REGION:-us-east-1}
TABLE_NAME="lunari-users-${ENV}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="${SCRIPT_DIR}/seed-data.json"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================"
echo -e "  DynamoDB Seed Script - LUNARi Users"
echo -e "========================================${NC}\n"

echo -e "${GRAY}Environment: ${ENV}"
echo -e "Table Name: ${TABLE_NAME}"
echo -e "Region: ${REGION}${NC}\n"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}ERROR: AWS CLI is not installed!${NC}"
    echo -e "${YELLOW}Install it with: pip install awscli${NC}\n"
    exit 1
fi

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}WARNING: jq is not installed. Using basic parsing.${NC}"
    echo -e "${GRAY}For better output, install jq: sudo apt-get install jq${NC}\n"
fi

# Check if seed data file exists
if [ ! -f "$SEED_FILE" ]; then
    echo -e "${RED}ERROR: Seed data file not found: ${SEED_FILE}${NC}\n"
    exit 1
fi

# Check if table exists
echo -e "${YELLOW}Checking if table exists...${NC}"
if ! aws dynamodb describe-table --table-name "$TABLE_NAME" --region "$REGION" &> /dev/null; then
    echo -e "${RED}ERROR: Table '${TABLE_NAME}' does not exist!${NC}\n"
    echo -e "${YELLOW}Please create the table first:${NC}"
    echo -e "${GRAY}  cd infrastructure"
    echo -e "  ./deploy-dynamodb.sh ${ENV}${NC}\n"
    exit 1
fi
echo -e "${GREEN}✓ Table exists${NC}\n"

# Parse and insert users
echo -e "${YELLOW}Inserting users into DynamoDB...${NC}\n"

SUCCESS_COUNT=0
ERROR_COUNT=0
TOTAL_COUNT=0

# Read seed data and process each user
if command -v jq &> /dev/null; then
    # Use jq for better JSON parsing
    TOTAL_COUNT=$(jq 'length' "$SEED_FILE")

    for i in $(seq 0 $((TOTAL_COUNT - 1))); do
        USER=$(jq -c ".[$i]" "$SEED_FILE")
        USERNAME=$(echo "$USER" | jq -r '.username')
        EMAIL=$(echo "$USER" | jq -r '.email')

        # Convert JSON to DynamoDB format
        DYNAMO_ITEM=$(echo "$USER" | jq -c '{
            id: {S: .id},
            username: {S: .username},
            email: {S: .email},
            password: {S: .password},
            isActive: {BOOL: .isActive},
            isVerified: {BOOL: .isVerified},
            createdAt: {S: .createdAt},
            updatedAt: {S: .updatedAt},
            personal: {M: {
                firstName: {S: .personal.firstName},
                lastName: {S: .personal.lastName},
                phone: {S: .personal.phone},
                birthdate: {S: .personal.birthdate},
                bio: {S: .personal.bio},
                memberSince: {S: .personal.memberSince}
            }},
            address: {M: {
                addressLine1: {S: .address.addressLine1},
                addressLine2: {S: .address.addressLine2},
                city: {S: .address.city},
                region: {S: .address.region},
                postalCode: {S: .address.postalCode},
                country: {S: .address.country},
                deliveryNotes: {S: .address.deliveryNotes}
            }},
            preferences: {M: {
                favoriteCategories: {L: [.preferences.favoriteCategories[] | {S: .}]},
                preferredPlatform: {S: .preferences.preferredPlatform},
                gamingHours: {S: .preferences.gamingHours},
                notifyOffers: {BOOL: .preferences.notifyOffers},
                notifyNewProducts: {BOOL: .preferences.notifyNewProducts},
                notifyRestocks: {BOOL: .preferences.notifyRestocks},
                notifyNewsletter: {BOOL: .preferences.notifyNewsletter}
            }},
            gaming: {M: {
                gamerTag: {S: .gaming.gamerTag},
                favoriteGenre: {S: .gaming.favoriteGenre},
                skillLevel: {S: .gaming.skillLevel},
                streamingPlatforms: {L: [.gaming.streamingPlatforms[] | {S: .}]},
                favoriteGames: {S: .gaming.favoriteGames}
            }},
            stats: {M: {
                level: {S: .stats.level},
                points: {N: (.stats.points | tostring)},
                purchases: {N: (.stats.purchases | tostring)},
                reviews: {N: (.stats.reviews | tostring)},
                favorites: {N: (.stats.favorites | tostring)}
            }},
            coupons: {L: [.coupons[] | {M: {
                id: {S: .id},
                code: {S: .code},
                description: {S: .description},
                type: {S: .type},
                value: {N: (.value | tostring)},
                minPurchase: {N: (.minPurchase | tostring)},
                expiresAt: {S: .expiresAt},
                isUsed: {BOOL: .isUsed}
            }}]}
        }')

        if aws dynamodb put-item \
            --table-name "$TABLE_NAME" \
            --region "$REGION" \
            --item "$DYNAMO_ITEM" &> /dev/null; then
            echo -e "  ${GREEN}✓ Inserted user: ${USERNAME} (${EMAIL})${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        else
            echo -e "  ${RED}✗ Failed to insert ${USERNAME}${NC}"
            ERROR_COUNT=$((ERROR_COUNT + 1))
        fi
    done
else
    # Fallback: Use AWS CLI batch-write-item (simpler but less feedback)
    echo -e "${YELLOW}Using AWS CLI batch-write (no jq available)${NC}\n"

    # For simplicity, just use put-item with the JSON directly via AWS DynamoDB JSON format
    # This is a simplified approach - for production, install jq

    echo -e "${YELLOW}Please install jq for full functionality:${NC}"
    echo -e "${GRAY}  sudo apt-get install jq${NC}\n"
    echo -e "${YELLOW}Or use the Node.js script:${NC}"
    echo -e "${GRAY}  node seed-database.js ${ENV}${NC}\n"
    exit 1
fi

# Summary
echo -e "\n${CYAN}========================================"
echo -e "  Summary"
echo -e "========================================${NC}"
echo -e "${GRAY}Total users: ${TOTAL_COUNT}${NC}"
echo -e "${GREEN}Successfully inserted: ${SUCCESS_COUNT}${NC}"
if [ $ERROR_COUNT -gt 0 ]; then
    echo -e "${RED}Failed: ${ERROR_COUNT}${NC}"
else
    echo -e "${GRAY}Failed: ${ERROR_COUNT}${NC}"
fi
echo -e "${CYAN}========================================${NC}\n"

if [ $SUCCESS_COUNT -gt 0 ]; then
    echo -e "${GREEN}✓ Database seeded successfully!${NC}\n"
    echo -e "${GRAY}You can now query the data:${NC}"
    echo -e "${GRAY}  aws dynamodb scan --table-name ${TABLE_NAME} --region ${REGION}${NC}\n"
    echo -e "${GRAY}Or via the API:${NC}"
    echo -e "${GRAY}  curl http://your-api:8080/api/v1/users${NC}\n"
fi

echo -e "${GREEN}Done!${NC}"
