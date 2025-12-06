# Carrito Service - EC2 Deployment Guide

## Overview
This guide explains how to deploy the Carrito service to AWS EC2 and configure it to communicate with Usuario and Inventario services running on separate EC2 instances.

## Architecture

```
┌─────────────────────┐
│   Usuario Service   │
│   EC2 Instance 1    │
│   Port: 8081        │
│   Public DNS: X     │
└─────────────────────┘
           ▲
           │
           │
┌──────────┴──────────┐
│  Carrito Service    │
│  EC2 Instance 2     │◄───────┐
│  Port: 8083         │        │
│  Public DNS: Y      │        │
└─────────────────────┘        │
           │                   │
           │                   │
           ▼                   │
┌─────────────────────┐        │
│ Inventario Service  │        │
│ EC2 Instance 3      │────────┘
│ Port: 8082          │
│ Public DNS: Z       │
└─────────────────────┘
```

## Prerequisites

### 1. EC2 Instance Setup
- **Instance Type**: t2.micro or larger
- **AMI**: Amazon Linux 2023 or Ubuntu 22.04
- **Java**: OpenJDK 21 installed
- **Maven**: 3.8+ installed
- **Security Group**: Configured to allow traffic

### 2. Security Group Configuration

#### Carrito Service Security Group
| Type | Protocol | Port Range | Source | Description |
|------|----------|------------|--------|-------------|
| HTTP | TCP | 8083 | 0.0.0.0/0 | Carrito API access |
| HTTPS | TCP | 443 | 0.0.0.0/0 | Transbank callbacks |
| SSH | TCP | 22 | Your IP | SSH access |
| Custom TCP | TCP | 8081 | Usuario SG | Usuario service communication |
| Custom TCP | TCP | 8082 | Inventario SG | Inventario service communication |

**IMPORTANT**: Allow outbound traffic to Usuario and Inventario EC2 instances.

### 3. Database Setup
- NeonDB PostgreSQL configured and accessible from EC2
- Database credentials available
- Security group allows EC2 to connect to database

## Deployment Steps

### Step 1: Connect to EC2 Instance

```bash
ssh -i your-key.pem ec2-user@your-ec2-public-dns
```

### Step 2: Install Dependencies

```bash
# Update system
sudo yum update -y  # Amazon Linux
# OR
sudo apt update && sudo apt upgrade -y  # Ubuntu

# Install Java 21
sudo yum install java-21-amazon-corretto -y  # Amazon Linux
# OR
sudo apt install openjdk-21-jdk -y  # Ubuntu

# Install Maven
sudo yum install maven -y  # Amazon Linux
# OR
sudo apt install maven -y  # Ubuntu

# Verify installations
java -version
mvn -version
```

### Step 3: Clone Repository

```bash
cd /home/ec2-user
git clone <your-repository-url>
cd lunari/carrito
```

### Step 4: Configure Environment Variables

Create `.env` file in `src/main/resources/`:

```bash
nano src/main/resources/.env
```

**For EC2 Deployment** (update with your actual EC2 DNS names):

```env
# Database Configuration
DB_HOST=your-neondb-host.aws.neon.tech
DB_PORT=5432
DB_NAME=lunari_cart_db
DB_USER=your_db_user
DB_PASSWORD=your_secure_password

# Microservices URLs (EC2 Public DNS or Elastic IPs)
USUARIO_SERVICE_URL=http://ec2-usuario-dns.compute.amazonaws.com:8081
INVENTARIO_SERVICE_URL=http://ec2-inventario-dns.compute.amazonaws.com:8082

# Transbank Configuration
TRANSBANK_API_KEY=597055555532
TRANSBANK_COMMERCE_CODE=597055555532
TRANSBANK_ENVIRONMENT=TEST
```

**Production Configuration** (with real Transbank credentials):

```env
# Use HTTPS in production
USUARIO_SERVICE_URL=https://usuario-api.yourdomain.com
INVENTARIO_SERVICE_URL=https://inventario-api.yourdomain.com

# Production Transbank credentials
TRANSBANK_API_KEY=your_production_api_key
TRANSBANK_COMMERCE_CODE=your_production_commerce_code
TRANSBANK_ENVIRONMENT=PROD
```

### Step 5: Build Application

```bash
mvn clean package -DskipTests
```

The JAR file will be created at: `target/lunari-cart-api-0.0.1-SNAPSHOT.jar`

### Step 6: Run Application

#### Option A: Direct Run (for testing)

```bash
java -jar target/lunari-cart-api-0.0.1-SNAPSHOT.jar
```

#### Option B: Run with Production Profile

```bash
java -jar target/lunari-cart-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Option C: Run in Background

```bash
nohup java -jar target/lunari-cart-api-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
```

### Step 7: Create Systemd Service (Recommended)

Create service file:

```bash
sudo nano /etc/systemd/system/lunari-cart.service
```

Add content:

```ini
[Unit]
Description=LUNARi Carrito Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/lunari/carrito
ExecStart=/usr/bin/java -jar /home/ec2-user/lunari/carrito/target/lunari-cart-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/lunari-cart/app.log
StandardError=append:/var/log/lunari-cart/error.log

[Install]
WantedBy=multi-user.target
```

Create log directory:

```bash
sudo mkdir -p /var/log/lunari-cart
sudo chown ec2-user:ec2-user /var/log/lunari-cart
```

Enable and start service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable lunari-cart
sudo systemctl start lunari-cart
sudo systemctl status lunari-cart
```

### Step 8: Verify Deployment

Check service is running:

```bash
# Check process
ps aux | grep lunari-cart

# Check port is listening
sudo netstat -tlnp | grep 8083

# Test health endpoint
curl http://localhost:8083/api/v1/payments/health

# Check logs
tail -f /var/log/lunari-cart/app.log
```

Test from outside:

```bash
curl http://your-ec2-public-dns:8083/api/v1/payments/health
```

### Step 9: Test Inter-Service Communication

Verify Carrito can reach other services:

```bash
# From Carrito EC2 instance, test connectivity
curl http://usuario-ec2-dns:8081/swagger-ui
curl http://inventario-ec2-dns:8082/swagger-ui
```

## Environment Variables Reference

| Variable | Description | Example (Local) | Example (EC2) |
|----------|-------------|-----------------|---------------|
| `DB_HOST` | PostgreSQL host | localhost | neondb-host.aws.neon.tech |
| `DB_PORT` | PostgreSQL port | 5432 | 5432 |
| `DB_NAME` | Database name | lunari_cart_db | lunari_cart_db |
| `DB_USER` | Database username | postgres | db_user |
| `DB_PASSWORD` | Database password | password | secure_password |
| `USUARIO_SERVICE_URL` | Usuario service URL | http://localhost:8081 | http://ec2-x.amazonaws.com:8081 |
| `INVENTARIO_SERVICE_URL` | Inventario service URL | http://localhost:8082 | http://ec2-y.amazonaws.com:8082 |
| `TRANSBANK_API_KEY` | Transbank API key | 597055555532 | production_key |
| `TRANSBANK_COMMERCE_CODE` | Transbank commerce code | 597055555532 | production_code |
| `TRANSBANK_ENVIRONMENT` | TEST or PROD | TEST | PROD |

## Troubleshooting

### Issue 1: Cannot Connect to Usuario/Inventario Services

**Check**:
1. Security groups allow traffic between instances
2. Services are running on target EC2 instances
3. Firewall rules allow outbound connections
4. URLs are correct in .env file

**Test**:
```bash
# From Carrito EC2, test connectivity
telnet usuario-ec2-dns 8081
curl -v http://usuario-ec2-dns:8081/api/v1/health
```

### Issue 2: Database Connection Failed

**Check**:
1. NeonDB allows connections from EC2 IP
2. Database credentials are correct
3. SSL mode is properly configured

**Test**:
```bash
# Test database connection
psql "postgresql://$DB_USER:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME?sslmode=require"
```

### Issue 3: Transbank Callbacks Not Working

**Check**:
1. EC2 security group allows HTTPS inbound (port 443)
2. Return URL is accessible from internet
3. Transbank environment (TEST/PROD) is correct

**Solution**: Use Elastic IP or domain name for stable callback URL

### Issue 4: Application Crashes on Startup

**Check logs**:
```bash
# Systemd logs
sudo journalctl -u lunari-cart -n 50

# Application logs
tail -f /var/log/lunari-cart/error.log
```

**Common causes**:
- Missing environment variables
- Port already in use
- Database connection failed
- Out of memory

## Production Best Practices

### 1. Use HTTPS
- Set up SSL/TLS certificates (Let's Encrypt)
- Configure HTTPS for all inter-service communication
- Use AWS Certificate Manager with Load Balancer

### 2. Use Load Balancer
- Create Application Load Balancer (ALB)
- Configure health checks
- Enable auto-scaling

### 3. Secure Secrets
- Use AWS Systems Manager Parameter Store
- Or use AWS Secrets Manager
- Never commit .env to Git

### 4. Monitoring
- Enable CloudWatch logs
- Set up alarms for errors
- Monitor service health

### 5. Backup
- Regular database backups
- Store JAR files in S3
- Version control for configurations

## Updating Deployment

### Update Code

```bash
cd /home/ec2-user/lunari/carrito
git pull origin main
mvn clean package -DskipTests
sudo systemctl restart lunari-cart
sudo systemctl status lunari-cart
```

### Update Environment Variables

```bash
nano src/main/resources/.env
# Make changes
sudo systemctl restart lunari-cart
```

## Service Management Commands

```bash
# Start service
sudo systemctl start lunari-cart

# Stop service
sudo systemctl stop lunari-cart

# Restart service
sudo systemctl restart lunari-cart

# Check status
sudo systemctl status lunari-cart

# View logs
sudo journalctl -u lunari-cart -f

# Disable service
sudo systemctl disable lunari-cart
```

## Performance Tuning

### JVM Options

For better performance, add JVM options:

```bash
java -Xms512m -Xmx1024m -XX:+UseG1GC \
     -jar target/lunari-cart-api-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=prod
```

### Database Connection Pool

Already configured in `application-prod.properties`:
- Maximum pool size: 10
- Minimum idle: 5
- Connection timeout: 30s

## Additional Resources

- [Transbank Developer Portal](https://www.transbankdevelopers.cl/)
- [Spring Boot Deployment](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [AWS EC2 User Guide](https://docs.aws.amazon.com/ec2/)
- [NeonDB Documentation](https://neon.tech/docs)

## Support

For issues or questions:
1. Check application logs
2. Verify security group configurations
3. Test inter-service connectivity
4. Review Transbank integration documentation
