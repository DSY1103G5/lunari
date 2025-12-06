#!/usr/bin/env bash

# Upload Product Images to S3 Bucket
# Usage: ./upload-images-to-s3.sh <s3-bucket-name> [images-directory]
# Example: ./upload-images-to-s3.sh lunari-product-images ./images

set -e

S3_BUCKET=$1
IMAGES_DIR=${2:-./images}

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

if [ -z "$S3_BUCKET" ]; then
    echo -e "${RED}ERROR: S3 bucket name not provided${NC}"
    echo ""
    echo "Usage: $0 <s3-bucket-name> [images-directory]"
    echo "Example: $0 lunari-product-images ./images"
    echo ""
    echo "This script will:"
    echo "  1. Check if the S3 bucket exists (create if needed)"
    echo "  2. Upload all images from the directory to S3"
    echo "  3. Set public-read ACL for web access"
    echo "  4. Output the public URLs for each image"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Upload Product Images to S3${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${YELLOW}S3 Bucket:${NC} $S3_BUCKET"
echo -e "${YELLOW}Images Directory:${NC} $IMAGES_DIR\n"

# Check if images directory exists
if [ ! -d "$IMAGES_DIR" ]; then
    echo -e "${RED}✗ Images directory not found: $IMAGES_DIR${NC}"
    echo -e "${YELLOW}Create the directory and add images:${NC}"
    echo "  mkdir -p $IMAGES_DIR"
    echo "  # Add your product images (jpg, png, webp, etc.)"
    exit 1
fi

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}✗ AWS CLI not installed${NC}"
    echo -e "${YELLOW}Install AWS CLI:${NC}"
    echo "  # Ubuntu/Debian:"
    echo "  sudo apt install awscli"
    echo ""
    echo "  # macOS:"
    echo "  brew install awscli"
    exit 1
fi

# Check AWS credentials
echo -e "${YELLOW}Step 1/4:${NC} Checking AWS credentials..."
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}✗ AWS credentials not configured or expired${NC}"
    echo -e "${YELLOW}Configure AWS credentials:${NC}"
    echo "  1. Get credentials from AWS Academy"
    echo "  2. Run: aws configure"
    echo "  Or update ~/.aws/credentials"
    exit 1
fi
echo -e "${GREEN}✓ AWS credentials valid${NC}\n"

# Check if bucket exists, create if not
echo -e "${YELLOW}Step 2/4:${NC} Checking S3 bucket..."
if aws s3 ls "s3://$S3_BUCKET" &> /dev/null; then
    echo -e "${GREEN}✓ Bucket exists: $S3_BUCKET${NC}\n"
else
    echo -e "${YELLOW}⚠ Bucket does not exist. Creating...${NC}"
    
    # Get AWS region
    AWS_REGION=$(aws configure get region)
    if [ -z "$AWS_REGION" ]; then
        AWS_REGION="us-east-1"
        echo -e "${YELLOW}  Using default region: $AWS_REGION${NC}"
    fi
    
    # Create bucket
    if [ "$AWS_REGION" = "us-east-1" ]; then
        aws s3 mb "s3://$S3_BUCKET" || {
            echo -e "${RED}✗ Failed to create bucket${NC}"
            echo -e "${YELLOW}The bucket name might be taken. Try a different name.${NC}"
            exit 1
        }
    else
        aws s3 mb "s3://$S3_BUCKET" --region "$AWS_REGION" || {
            echo -e "${RED}✗ Failed to create bucket${NC}"
            exit 1
        }
    fi
    
    # Set bucket policy for public read (optional, for web access)
    echo -e "${YELLOW}  Setting bucket policy for public access...${NC}"
    cat > /tmp/bucket-policy.json << POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::$S3_BUCKET/*"
    }
  ]
}
POLICY
    
    # Disable block public access
    aws s3api put-public-access-block \
        --bucket "$S3_BUCKET" \
        --public-access-block-configuration \
        "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false" 2>/dev/null || true
    
    # Apply bucket policy
    aws s3api put-bucket-policy --bucket "$S3_BUCKET" --policy file:///tmp/bucket-policy.json 2>/dev/null || {
        echo -e "${YELLOW}  ⚠ Could not set public policy (may need manual configuration)${NC}"
    }
    
    rm -f /tmp/bucket-policy.json
    echo -e "${GREEN}✓ Bucket created: $S3_BUCKET${NC}\n"
fi

# Count images
IMAGE_COUNT=$(find "$IMAGES_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.gif" -o -iname "*.webp" \) | wc -l)

if [ "$IMAGE_COUNT" -eq 0 ]; then
    echo -e "${RED}✗ No images found in $IMAGES_DIR${NC}"
    echo -e "${YELLOW}Add images to the directory:${NC}"
    echo "  Supported formats: jpg, jpeg, png, gif, webp"
    exit 1
fi

echo -e "${YELLOW}Step 3/4:${NC} Found $IMAGE_COUNT images to upload...\n"

# Upload images
echo -e "${YELLOW}Step 4/4:${NC} Uploading images to S3..."
UPLOADED=0
FAILED=0

# Create URLs file
URLS_FILE="s3-image-urls.txt"
echo "# Product Image URLs - $(date)" > "$URLS_FILE"
echo "# Bucket: $S3_BUCKET" >> "$URLS_FILE"
echo "" >> "$URLS_FILE"

find "$IMAGES_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" -o -iname "*.gif" -o -iname "*.webp" \) | while read -r image; do
    FILENAME=$(basename "$image")
    echo -e "  Uploading: ${BLUE}$FILENAME${NC}"
    
    # Determine content type
    EXT="${FILENAME##*.}"
    case "${EXT,,}" in
        jpg|jpeg) CONTENT_TYPE="image/jpeg" ;;
        png) CONTENT_TYPE="image/png" ;;
        gif) CONTENT_TYPE="image/gif" ;;
        webp) CONTENT_TYPE="image/webp" ;;
        *) CONTENT_TYPE="application/octet-stream" ;;
    esac
    
    # Upload with public-read ACL and content-type
    if aws s3 cp "$image" "s3://$S3_BUCKET/$FILENAME" \
        --acl public-read \
        --content-type "$CONTENT_TYPE" \
        --metadata-directive REPLACE 2>/dev/null; then
        
        # Generate public URL
        URL="https://$S3_BUCKET.s3.amazonaws.com/$FILENAME"
        echo "  ${GREEN}✓${NC} $URL"
        echo "$FILENAME = $URL" >> "$URLS_FILE"
        ((UPLOADED++))
    else
        echo "  ${RED}✗ Failed${NC}"
        ((FAILED++))
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Upload Complete!${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${GREEN}✓ Uploaded:${NC} $UPLOADED images"
if [ "$FAILED" -gt 0 ]; then
    echo -e "${RED}✗ Failed:${NC} $FAILED images"
fi
echo ""

echo -e "${YELLOW}Image URLs saved to:${NC} $URLS_FILE"
echo -e "${YELLOW}View bucket:${NC} aws s3 ls s3://$S3_BUCKET"
echo ""

echo -e "${YELLOW}Example image URL:${NC}"
FIRST_IMAGE=$(find "$IMAGES_DIR" -type f \( -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.png" \) | head -1)
if [ -n "$FIRST_IMAGE" ]; then
    FIRST_FILENAME=$(basename "$FIRST_IMAGE")
    echo "  https://$S3_BUCKET.s3.amazonaws.com/$FIRST_FILENAME"
fi
echo ""

echo -e "${BLUE}Tip:${NC} Update your database with these URLs"
echo -e "${BLUE}Tip:${NC} To update images, just run this script again"
