#!/bin/bash

# Setup IAM Role for EC2 Instance to Access DynamoDB
# Usage: ./setup-ec2-iam-role.sh <instance-id> <environment>
# Example: ./setup-ec2-iam-role.sh i-0123456789abcdef0 dev

set -e

INSTANCE_ID=$1
ENV=${2:-dev}
ROLE_NAME="LunariUserServiceEC2Role-${ENV}"
POLICY_NAME="LunariDynamoDBAccess-${ENV}"
INSTANCE_PROFILE_NAME="LunariUserServiceProfile-${ENV}"

if [ -z "$INSTANCE_ID" ]; then
    echo "ERROR: Please provide EC2 Instance ID"
    echo "Usage: $0 <instance-id> [environment]"
    echo "Example: $0 i-0123456789abcdef0 dev"
    exit 1
fi

echo "=========================================="
echo "  Setting up IAM Role for EC2"
echo "=========================================="
echo "Instance ID: $INSTANCE_ID"
echo "Environment: $ENV"
echo "Role Name: $ROLE_NAME"
echo ""

# 1. Create trust policy for EC2
echo "Creating trust policy..."
cat > /tmp/ec2-trust-policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# 2. Create IAM Role (if it doesn't exist)
echo "Creating IAM role: $ROLE_NAME..."
if aws iam get-role --role-name "$ROLE_NAME" 2>/dev/null; then
    echo "✓ Role already exists"
else
    aws iam create-role \
        --role-name "$ROLE_NAME" \
        --assume-role-policy-document file:///tmp/ec2-trust-policy.json \
        --description "Role for Lunari User Service EC2 to access DynamoDB"
    echo "✓ Role created"
fi

# 3. Create and attach DynamoDB policy
echo "Creating DynamoDB access policy..."
aws iam put-role-policy \
    --role-name "$ROLE_NAME" \
    --policy-name "$POLICY_NAME" \
    --policy-document file://ec2-dynamodb-policy.json

echo "✓ Policy attached to role"

# 4. Create Instance Profile (if it doesn't exist)
echo "Creating instance profile..."
if aws iam get-instance-profile --instance-profile-name "$INSTANCE_PROFILE_NAME" 2>/dev/null; then
    echo "✓ Instance profile already exists"
else
    aws iam create-instance-profile \
        --instance-profile-name "$INSTANCE_PROFILE_NAME"
    echo "✓ Instance profile created"

    # Wait for instance profile to be created
    sleep 5
fi

# 5. Add role to instance profile (if not already added)
echo "Adding role to instance profile..."
if aws iam get-instance-profile --instance-profile-name "$INSTANCE_PROFILE_NAME" | grep -q "$ROLE_NAME"; then
    echo "✓ Role already in instance profile"
else
    aws iam add-role-to-instance-profile \
        --instance-profile-name "$INSTANCE_PROFILE_NAME" \
        --role-name "$ROLE_NAME"
    echo "✓ Role added to instance profile"

    # Wait for propagation
    sleep 10
fi

# 6. Check if instance already has a profile attached
echo "Checking instance profile association..."
CURRENT_PROFILE=$(aws ec2 describe-iam-instance-profile-associations \
    --filters "Name=instance-id,Values=$INSTANCE_ID" \
    --query 'IamInstanceProfileAssociations[0].AssociationId' \
    --output text 2>/dev/null || echo "None")

if [ "$CURRENT_PROFILE" != "None" ] && [ "$CURRENT_PROFILE" != "" ]; then
    echo "Removing existing instance profile..."
    aws ec2 disassociate-iam-instance-profile \
        --association-id "$CURRENT_PROFILE"
    echo "✓ Existing profile removed"
    sleep 5
fi

# 7. Attach instance profile to EC2 instance
echo "Attaching instance profile to EC2 instance..."
aws ec2 associate-iam-instance-profile \
    --instance-id "$INSTANCE_ID" \
    --iam-instance-profile "Name=$INSTANCE_PROFILE_NAME"

echo "✓ Instance profile attached"

echo ""
echo "=========================================="
echo "  ✅ IAM Role Setup Complete!"
echo "=========================================="
echo ""
echo "The EC2 instance now has permissions to access DynamoDB."
echo ""
echo "Next steps:"
echo "  1. SSH into your EC2 instance"
echo "  2. Restart the application:"
echo "     pkill -f 'java -jar'"
echo "     AWS_REGION=us-east-1 AWS_DYNAMODB_TABLE_NAME=lunari-users-${ENV} nohup java -jar app.jar > app.log 2>&1 &"
echo "  3. Verify it works:"
echo "     curl http://localhost:8080/api/v1/users"
echo ""
echo "Note: It may take up to 30 seconds for IAM changes to propagate."
echo ""
