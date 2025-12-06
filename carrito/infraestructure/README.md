# Carrito Service - Infrastructure & Deployment

This directory contains scripts and configuration for deploying the Carrito service to AWS EC2.

## Quick Start

### 1. Build the Application

```bash
cd ..
mvn clean package -DskipTests
```

### 2. Create Environment Variables Script

Run the interactive script:

```bash
cd infraestructure
chmod +x create-env-script.sh
./create-env-script.sh
```

This will create `lunari-cart-env.sh` with your configuration.

**Or manually create it:**

```bash
cp lunari-cart-env.sh.example lunari-cart-env.sh
nano lunari-cart-env.sh
# Edit with your values
```

### 3. Deploy to EC2

```bash
chmod +x deploy-to-ec2.sh
./deploy-to-ec2.sh ec2-user@your-ec2-public-dns.amazonaws.com ~/.ssh/your-key.pem
```

## Files

- **`deploy-to-ec2.sh`** - Main deployment script
- **`create-env-script.sh`** - Interactive script to create environment variables
- **`lunari-cart-env.sh.example`** - Example environment variables file
- **`lunari-cart-env.sh`** - Your actual environment variables (created by you, gitignored)

## Environment Variables Approach

Unlike using `.env` files, this approach uses `/etc/profile.d/` on EC2:

### Why `/etc/profile.d/`?

✅ Variables persist across sessions
✅ Automatically loaded on login
✅ More secure (system-level permissions)
✅ Consistent with other services (Usuario, Inventario)
✅ No need to clone repository on EC2

### How It Works

1. **Local**: Create `lunari-cart-env.sh` with your EC2 configuration
2. **Upload**: Script uploads to EC2 via SCP
3. **Install**: Moved to `/etc/profile.d/` with proper permissions
4. **Source**: Application sources it before starting

## Deployment Process

The deployment script performs these steps:

1. **Upload JAR** - Copies built JAR to EC2
2. **Upload Env Script** - Copies environment variables script
3. **Install Env Script** - Moves to `/etc/profile.d/`
4. **Stop Old App** - Kills previous instance
5. **Start App** - Runs with sourced environment variables
6. **Verify** - Checks health endpoint

## Required Environment Variables

### Database
- `DB_HOST` - PostgreSQL host (NeonDB)
- `DB_PORT` - PostgreSQL port (default: 5432)
- `DB_NAME` - Database name
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

### Microservices
- `USUARIO_SERVICE_URL` - Usuario service URL
  - Local: `http://localhost:8081`
  - EC2: `http://ec2-XX-XX-XX-XX.compute-1.amazonaws.com:8081`
- `INVENTARIO_SERVICE_URL` - Inventario service URL
  - Local: `http://localhost:8082`
  - EC2: `http://ec2-YY-YY-YY-YY.compute-1.amazonaws.com:8082`

### Transbank
- `TRANSBANK_API_KEY` - API key (test: 597055555532)
- `TRANSBANK_COMMERCE_CODE` - Commerce code (test: 597055555532)
- `TRANSBANK_ENVIRONMENT` - TEST or PROD

## Security Group Configuration

Your EC2 instance needs these rules:

### Inbound

| Type | Port | Source | Purpose |
|------|------|--------|---------|
| SSH | 22 | Your IP | Deployment |
| HTTP | 8083 | 0.0.0.0/0 | API access |
| HTTPS | 443 | 0.0.0.0/0 | Transbank callbacks |

### Outbound

| Type | Port | Destination | Purpose |
|------|------|-------------|---------|
| PostgreSQL | 5432 | NeonDB | Database |
| HTTP | 8081 | Usuario EC2 | Usuario service |
| HTTP | 8082 | Inventario EC2 | Inventario service |
| HTTPS | 443 | 0.0.0.0/0 | Transbank API |

## Verification

After deployment, verify:

```bash
# Check service health
curl http://your-ec2-public-dns:8083/api/v1/payments/health

# Check Swagger UI
curl http://your-ec2-public-dns:8083/swagger-ui/index.html

# View logs
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns 'tail -f app.log'

# Check environment variables
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns 'source /etc/profile.d/lunari-cart-env.sh && env | grep -E "DB_|USUARIO_|INVENTARIO_|TRANSBANK_"'
```

## Troubleshooting

### Service Not Starting

Check logs on EC2:
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns 'tail -100 app.log'
```

Common issues:
- Missing Java 21: `java -version`
- Environment variables not loaded: check `/etc/profile.d/lunari-cart-env.sh`
- Database connection failed: verify NeonDB credentials
- Port already in use: `sudo netstat -tlnp | grep 8083`

### Cannot Connect to Other Services

Test connectivity:
```bash
# From Carrito EC2, test Usuario service
curl http://usuario-ec2-dns:8081/swagger-ui/index.html

# Test Inventario service
curl http://inventario-ec2-dns:8082/swagger-ui/index.html
```

If fails:
- Check security groups allow traffic between instances
- Verify other services are running
- Check URLs in environment variables

### Transbank Callbacks Not Working

- Ensure security group allows HTTPS inbound (port 443)
- Use public DNS or Elastic IP for stable callback URL
- Verify Transbank environment (TEST/PROD) is correct

## Manual Operations

### Start Application
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns
source /etc/profile.d/lunari-cart-env.sh
nohup java -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### Stop Application
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns 'pkill -f "java -jar"'
```

### Update Environment Variables
```bash
# On EC2
sudo nano /etc/profile.d/lunari-cart-env.sh
# Restart application after changes
```

### View Real-time Logs
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@your-ec2-dns 'tail -f app.log'
```

## Best Practices

1. **Never commit** `lunari-cart-env.sh` to Git (already in .gitignore)
2. **Use Elastic IPs** for stable inter-service URLs
3. **Enable HTTPS** in production with Load Balancer
4. **Set up CloudWatch** for monitoring
5. **Regular backups** of database
6. **Use Secrets Manager** for production credentials

## Related Documentation

- [Main EC2 Deployment Guide](../EC2_DEPLOYMENT_GUIDE.md) - Detailed deployment guide
- [API Documentation](../CARRITO_API_GUIDE.md) - Complete API reference
- [Transbank Docs](https://www.transbankdevelopers.cl/) - Payment integration

## Support

For deployment issues:
1. Check application logs on EC2
2. Verify security group configuration
3. Test inter-service connectivity
4. Review environment variables
