# Inventario Service - Infrastructure Scripts

This directory contains deployment and management scripts for the Inventario (Inventory) microservice.

## Available Scripts

### 1. `deploy-to-ec2.sh` - Deploy Application to EC2

Deploys the Inventario service JAR to an EC2 instance.

**Usage:**
```bash
./deploy-to-ec2.sh <ec2-host> [ssh-key]
```

**Example:**
```bash
./deploy-to-ec2.sh ec2-user@ec2-54-123-45-67.compute-1.amazonaws.com ~/.ssh/my-key.pem
```

**What it does:**
1. Uploads the JAR file to EC2
2. Copies AWS credentials from `~/.aws/credentials`
3. Stops any running instance
4. Starts the application with `--spring.profiles.active=prod`
5. Verifies the application is running on port 8082

**Prerequisites:**
- JAR file built: `cd .. && ./mvnw clean package -DskipTests`
- AWS credentials in `~/.aws/credentials`
- EC2 security group allows port 8082 (HTTP)

---

### 2. `upload-images-to-s3.sh` - Upload Product Images to S3

Uploads product images from a local directory to an S3 bucket.

**Usage:**
```bash
./upload-images-to-s3.sh <s3-bucket-name> [images-directory]
```

**Example:**
```bash
# Create images directory and add your product images
mkdir -p images
cp ~/Downloads/product-*.jpg images/

# Upload to S3
./upload-images-to-s3.sh lunari-product-images ./images
```

**What it does:**
1. Checks if S3 bucket exists (creates if needed)
2. Sets bucket policy for public read access
3. Uploads all images (jpg, png, gif, webp)
4. Sets correct content-type headers
5. Generates public URLs for each image
6. Saves URLs to `s3-image-urls.txt`

**Supported image formats:**
- JPG/JPEG
- PNG
- GIF
- WebP

**Generated URLs format:**
```
https://your-bucket-name.s3.amazonaws.com/image-name.jpg
```

**Prerequisites:**
- AWS CLI installed: `aws --version`
- AWS credentials configured (get from AWS Academy)
- Images in a local directory

---

### 3. `update-aws-credentials.sh` - Update Expired AWS Credentials

Quickly updates AWS credentials on EC2 when they expire (AWS Academy credentials rotate every ~4 hours).

**Usage:**
```bash
./update-aws-credentials.sh <ec2-host> [ssh-key]
```

**Example:**
```bash
./update-aws-credentials.sh ec2-user@ec2-54-123-45-67.compute-1.amazonaws.com ~/.ssh/my-key.pem
```

**What it does:**
1. Uploads new credentials from `~/.aws/credentials`
2. Restarts the application
3. Verifies the application is running

**When to use:**
- When you see "expired token" errors in logs
- Every ~3-4 hours with AWS Academy
- After getting new credentials from AWS Academy

---

## Complete Deployment Workflow

### Initial Deployment

**Step 1: Build the JAR**
```bash
cd ../inventario
./mvnw clean package -DskipTests
```

**Step 2: Prepare product images** (optional)
```bash
cd infrastructure
mkdir -p images
# Add your product images to the images/ directory
```

**Step 3: Upload images to S3** (optional)
```bash
./upload-images-to-s3.sh lunari-product-images ./images
# Save the generated URLs from s3-image-urls.txt
```

**Step 4: Deploy to EC2**
```bash
./deploy-to-ec2.sh ec2-user@your-ec2-ip ~/.ssh/your-key.pem
```

**Step 5: Verify deployment**
```bash
# Check application status
curl http://your-ec2-ip:8082/actuator/health

# Get products
curl http://your-ec2-ip:8082/api/v1/products

# View logs
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-ip "tail -f inventario-app.log"
```

---

### Update Expired Credentials

**Step 1: Get new credentials from AWS Academy**
1. Go to AWS Academy Learner Lab
2. Click "AWS Details"
3. Click "Show" next to AWS CLI credentials
4. Copy all three values

**Step 2: Update local credentials**
```bash
cat > ~/.aws/credentials << 'EOF'
[default]
aws_access_key_id = YOUR_NEW_ACCESS_KEY
aws_secret_access_key = YOUR_NEW_SECRET_KEY
aws_session_token = YOUR_NEW_SESSION_TOKEN
