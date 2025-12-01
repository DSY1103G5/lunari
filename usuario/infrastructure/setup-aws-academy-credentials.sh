#!/bin/bash

# Setup AWS Academy Credentials on EC2
# Usage: ./setup-aws-academy-credentials.sh <ec2-host>
# Example: ./setup-aws-academy-credentials.sh ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com

set -e

EC2_HOST=$1

if [ -z "$EC2_HOST" ]; then
    echo "ERROR: Please provide EC2 host"
    echo "Usage: $0 <ec2-user@ec2-host>"
    echo "Example: $0 ec2-user@ec2-XX-XX-XX-XX.compute-1.amazonaws.com"
    exit 1
fi

echo "=========================================="
echo "  AWS Academy Credentials Setup"
echo "=========================================="
echo ""
echo "Step 1: Get your AWS Academy credentials"
echo "  1. Go to AWS Academy Learner Lab"
echo "  2. Click 'AWS Details'"
echo "  3. Click 'Show' next to AWS CLI credentials"
echo "  4. Copy the credentials"
echo ""
echo "Step 2: Paste them here (they should look like):"
echo "  [default]"
echo "  aws_access_key_id=ASIA..."
echo "  aws_secret_access_key=..."
echo "  aws_session_token=..."
echo ""
echo "Paste the credentials and press Ctrl+D when done:"
echo "---"

# Read credentials from stdin
CREDENTIALS=$(cat)

echo ""
echo "✓ Credentials received"
echo ""
echo "Uploading credentials to EC2..."

# Create credentials file on EC2
ssh -o StrictHostKeyChecking=no "$EC2_HOST" "mkdir -p ~/.aws"

echo "$CREDENTIALS" | ssh "$EC2_HOST" "cat > ~/.aws/credentials"

echo "✓ Credentials uploaded"
echo ""
echo "Setting AWS region..."

ssh "$EC2_HOST" "cat > ~/.aws/config << 'EOF'
[default]
region = us-east-1
output = json
EOF"

echo "✓ Region configured"
echo ""
echo "=========================================="
echo "  ✅ Setup Complete!"
echo "=========================================="
echo ""
echo "Now restart your application on EC2:"
echo ""
echo "  ssh $EC2_HOST"
echo "  pkill -f 'java -jar'"
echo "  nohup java -jar app.jar > app.log 2>&1 &"
echo "  tail -f app.log"
echo ""
echo "⚠️  IMPORTANT: AWS Academy credentials expire after a few hours!"
echo "    When they expire, re-run this script to update them."
echo ""
