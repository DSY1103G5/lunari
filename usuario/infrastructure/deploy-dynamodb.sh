#!/bin/bash

# DynamoDB Table Deployment Script for LUNARi Users Service
# This script deploys the DynamoDB table using AWS CloudFormation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT=${1:-dev}
REGION=${AWS_REGION:-us-east-1}
STACK_NAME="lunari-users-dynamodb-${ENVIRONMENT}"
TEMPLATE_FILE="dynamodb-table.yaml"
BILLING_MODE=${BILLING_MODE:-PAY_PER_REQUEST}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}DynamoDB Table Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Environment: ${YELLOW}${ENVIRONMENT}${NC}"
echo -e "Region: ${YELLOW}${REGION}${NC}"
echo -e "Stack Name: ${YELLOW}${STACK_NAME}${NC}"
echo -e "Billing Mode: ${YELLOW}${BILLING_MODE}${NC}"
echo ""

# Validate AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    exit 1
fi

# Validate AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}Error: AWS credentials not configured${NC}"
    echo "Please run: aws configure"
    exit 1
fi

# Validate template
echo -e "${YELLOW}Validating CloudFormation template...${NC}"
aws cloudformation validate-template \
    --template-body file://${TEMPLATE_FILE} \
    --region ${REGION} > /dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Template is valid${NC}"
else
    echo -e "${RED}✗ Template validation failed${NC}"
    exit 1
fi

# Check if stack exists
echo -e "${YELLOW}Checking if stack exists...${NC}"
if aws cloudformation describe-stacks \
    --stack-name ${STACK_NAME} \
    --region ${REGION} &> /dev/null; then

    echo -e "${YELLOW}Stack exists. Updating...${NC}"

    aws cloudformation update-stack \
        --stack-name ${STACK_NAME} \
        --template-body file://${TEMPLATE_FILE} \
        --parameters \
            ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
            ParameterKey=BillingMode,ParameterValue=${BILLING_MODE} \
        --region ${REGION} \
        --capabilities CAPABILITY_IAM

    echo -e "${YELLOW}Waiting for stack update to complete...${NC}"
    aws cloudformation wait stack-update-complete \
        --stack-name ${STACK_NAME} \
        --region ${REGION}

    echo -e "${GREEN}✓ Stack updated successfully${NC}"
else
    echo -e "${YELLOW}Stack does not exist. Creating...${NC}"

    aws cloudformation create-stack \
        --stack-name ${STACK_NAME} \
        --template-body file://${TEMPLATE_FILE} \
        --parameters \
            ParameterKey=Environment,ParameterValue=${ENVIRONMENT} \
            ParameterKey=BillingMode,ParameterValue=${BILLING_MODE} \
        --region ${REGION} \
        --capabilities CAPABILITY_IAM

    echo -e "${YELLOW}Waiting for stack creation to complete...${NC}"
    aws cloudformation wait stack-create-complete \
        --stack-name ${STACK_NAME} \
        --region ${REGION}

    echo -e "${GREEN}✓ Stack created successfully${NC}"
fi

# Get outputs
echo ""
echo -e "${GREEN}Stack Outputs:${NC}"
aws cloudformation describe-stacks \
    --stack-name ${STACK_NAME} \
    --region ${REGION} \
    --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue]' \
    --output table

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "Update your application.properties with:"
echo -e "${YELLOW}aws.dynamodb.tableName=lunari-users-${ENVIRONMENT}${NC}"
echo -e "${YELLOW}aws.region=${REGION}${NC}"
