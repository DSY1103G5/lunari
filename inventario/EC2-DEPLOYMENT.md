# EC2 Deployment Guide - LUNARi Inventory API

## Overview

This guide covers deploying the LUNARi Inventory API (gaming products catalog) to AWS EC2 with NeonDB PostgreSQL.

**Key Changes from Local Development:**
- No `.env` file required on EC2 (environment variables passed as command-line arguments)
- Database: NeonDB PostgreSQL (already configured)
- Port: 8082 (different from usuario service on 8081)

---

## Prerequisites

- AWS EC2 instance (t2.micro or larger)
- NeonDB PostgreSQL database
- SSH access to EC2 instance (.pem key file)
- Security group allowing port 8082

---

## Deployment Steps

### Step 1: Build the Application

**On your local machine:**

```bash
cd inventario
./mvnw clean package -DskipTests
```

This creates: `target/lunari-inventory-api-0.0.1-SNAPSHOT.jar`

---

### Step 2: Upload JAR to EC2

**Option A: Use the deployment script (recommended)**
```bash
cd inventario
./deploy-to-ec2.sh
# Follow the prompts
```

**Option B: Manual upload**
```bash
scp -i /path/to/your-key.pem \
    target/lunari-inventory-api-0.0.1-SNAPSHOT.jar \
    ec2-user@<YOUR-EC2-IP>:~/
```

---

### Step 3: Setup EC2 Instance

**Connect to EC2:**
```bash
ssh -i /path/to/your-key.pem ec2-user@<YOUR-EC2-IP>
```

**Install Java 21:**
```bash
sudo yum install java-21-amazon-corretto-headless -y
java -version  # Verify installation
```

---

### Step 4: Configure NeonDB Database

Before running the app, ensure your NeonDB database is set up:

**1. Create the schema:**
```bash
# Connect to your NeonDB database
psql postgresql://your_user:your_password@your-db.neon.tech/lunari_inventory?sslmode=require

# Drop old tables (if migrating from service catalog)
DROP TABLE IF EXISTS servicio_servicio_adicional CASCADE;
DROP TABLE IF EXISTS paquete_recurso_servicio CASCADE;
DROP TABLE IF EXISTS catalogo CASCADE;
DROP TABLE IF EXISTS tipo_recurso CASCADE;
DROP TABLE IF EXISTS servicio_adicional CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;

# Create new tables (Hibernate will auto-create on first run with ddl-auto=update)
# OR manually run these CREATE statements:

CREATE TABLE categoria (
    id_categoria VARCHAR(10) PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    categoria_id VARCHAR(10) NOT NULL,
    precio_clp INTEGER NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    marca VARCHAR(100),
    rating DECIMAL(2,1),
    specs JSONB,
    descripcion TEXT,
    tags JSONB,
    imagen VARCHAR(500),
    is_activo BOOLEAN DEFAULT TRUE,
    creado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    actualizado_el TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id_categoria)
);

CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_code ON producto(code);
CREATE INDEX idx_producto_activo ON producto(is_activo);
```

**2. Load seed data:**

Upload the seed files to EC2:
```bash
# On your local machine
scp -i /path/to/your-key.pem \
    seeds/inventory-categorias.sql \
    seeds/inventory-productos.sql \
    ec2-user@<YOUR-EC2-IP>:~/
```

Execute on NeonDB:
```bash
# On EC2
psql postgresql://your_user:your_password@your-db.neon.tech/lunari_inventory?sslmode=require < inventory-categorias.sql
psql postgresql://your_user:your_password@your-db.neon.tech/lunari_inventory?sslmode=require < inventory-productos.sql
```

---

### Step 5: Run the Application

**On your EC2 instance:**

**Option A: Run in foreground (for testing)**
```bash
java -jar lunari-inventory-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_HOST=ep-xxx-xxx.us-east-2.aws.neon.tech \
  --DB_PORT=5432 \
  --DB_NAME=lunari_inventory \
  --DB_USER=your_neon_user \
  --DB_PASSWORD=your_neon_password
```

Press `Ctrl+C` to stop.

**Option B: Run in background (recommended)**
```bash
nohup java -jar lunari-inventory-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_HOST=ep-xxx-xxx.us-east-2.aws.neon.tech \
  --DB_PORT=5432 \
  --DB_NAME=lunari_inventory \
  --DB_USER=your_neon_user \
  --DB_PASSWORD=your_neon_password \
  > app.log 2>&1 &

# Get the process ID
echo $!
```

---

### Step 6: Test Your Deployment

**From your local machine:**
```bash
# Health check
curl http://<YOUR-EC2-IP>:8082/actuator/health

# Should return: {"status":"UP"}

# Get all products
curl http://<YOUR-EC2-IP>:8082/api/v1/productos

# Get all categories
curl http://<YOUR-EC2-IP>:8082/api/v1/categorias

# View Swagger UI in browser
http://<YOUR-EC2-IP>:8082/swagger-ui/index.html
```

---

## Managing Your Application

### View Logs
```bash
# Real-time logs
tail -f app.log

# Last 100 lines
tail -n 100 app.log

# Search for errors
grep -i error app.log

# Search for specific product code
grep -i "JM001" app.log
```

### Stop the Application
```bash
# Find the process
ps aux | grep lunari-inventory-api

# Kill it (replace PID with actual process ID)
kill <PID>

# Force kill if needed
kill -9 <PID>
```

### Restart the Application
```bash
# Stop it
kill $(ps aux | grep lunari-inventory-api | grep -v grep | awk '{print $2}')

# Start it again
nohup java -jar lunari-inventory-api-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_HOST=your-db.neon.tech \
  --DB_PORT=5432 \
  --DB_NAME=lunari_inventory \
  --DB_USER=your_user \
  --DB_PASSWORD=your_password > app.log 2>&1 &
```

---

### Auto-start on Boot (Optional)

Create a systemd service:

```bash
sudo nano /etc/systemd/system/lunari-inventory-api.service
```

Add this content (update DB credentials):
```ini
[Unit]
Description=LUNARi Inventory API Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user
ExecStart=/usr/bin/java -jar /home/ec2-user/lunari-inventory-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --DB_HOST=your-db.neon.tech --DB_PORT=5432 --DB_NAME=lunari_inventory --DB_USER=your_user --DB_PASSWORD=your_password
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable lunari-inventory-api
sudo systemctl start lunari-inventory-api

# Check status
sudo systemctl status lunari-inventory-api

# View logs
sudo journalctl -u lunari-inventory-api -f
```

---

## API Endpoints

### Root
- **GET** `/api/v1` - API root with links

### Products
- **GET** `/api/v1/productos` - All products
- **GET** `/api/v1/productos/activos` - Active products only
- **GET** `/api/v1/productos/{id}` - Product by ID
- **GET** `/api/v1/productos/code/{code}` - Product by code (e.g., JM001)
- **GET** `/api/v1/productos/buscar?nombre=X` - Search by name
- **GET** `/api/v1/productos/categoria/{id}` - Products by category (e.g., JM, AC)
- **GET** `/api/v1/productos/marca/{marca}` - Products by brand
- **GET** `/api/v1/productos/en-stock` - Products in stock
- **GET** `/api/v1/productos/precio?min=X&max=Y` - Price range filter
- **GET** `/api/v1/productos/rating?min=X` - By minimum rating
- **GET** `/api/v1/productos/tag/{tag}` - By tag (uses JSONB query)
- **POST** `/api/v1/productos` - Create product
- **PUT** `/api/v1/productos/{id}` - Update product
- **PATCH** `/api/v1/productos/{id}/activar` - Activate product
- **PATCH** `/api/v1/productos/{id}/desactivar` - Deactivate product
- **PATCH** `/api/v1/productos/{id}/stock?stock=X` - Update stock
- **DELETE** `/api/v1/productos/{id}` - Delete product

### Categories
- **GET** `/api/v1/categorias` - All categories
- **GET** `/api/v1/categorias/{id}` - Category by ID (e.g., JM, AC)
- **GET** `/api/v1/categorias/buscar?nombre=X` - Search categories
- **POST** `/api/v1/categorias` - Create category
- **PUT** `/api/v1/categorias/{id}` - Update category
- **DELETE** `/api/v1/categorias/{id}` - Delete category

---

## Security Checklist

- [ ] EC2 Security Group allows port 8082 from authorized IPs only
- [ ] NeonDB database uses SSL connections (`sslmode=require`)
- [ ] SSH port (22) restricted to your IP only
- [ ] Database credentials not hardcoded (passed as environment variables)
- [ ] Consider using Application Load Balancer for HTTPS
- [ ] Set up CloudWatch alarms for high CPU/memory usage
- [ ] Regular security updates: `sudo yum update -y`
- [ ] Use secrets manager for database credentials in production

---

## Troubleshooting

### "Connection refused"
- Check security group allows port 8082
- Verify app is running: `ps aux | grep java`
- Check logs: `tail -f app.log`

### "Connection to NeonDB failed"
- Verify DB_HOST, DB_PORT, DB_NAME are correct
- Check NeonDB allows connections from EC2 IP
- Verify SSL mode is set: `sslmode=require`
- Test connection: `psql postgresql://user:pass@host:port/db?sslmode=require`

### "Table not found"
- Run schema creation SQL first
- Or let Hibernate auto-create (first run with `ddl-auto=update`)
- Load seed data after tables exist

### "JSONB query errors"
- Ensure PostgreSQL version supports JSONB (9.4+)
- NeonDB uses PostgreSQL 15+ by default
- Verify hypersistence-utils dependency in pom.xml

### High memory usage
- Monitor with: `top` or `htop`
- Consider increasing instance size (t2.small, t2.medium)
- Or add swap space (for t2.micro)

---

## Database Schema Differences

**Old Schema (Service Catalog):**
- 6 tables: catalogo, categoria, tipo_recurso, paquete_recurso_servicio, servicio_adicional, servicio_servicio_adicional
- Complex relationships for service pricing
- Category ID: Integer (auto-increment)

**New Schema (Gaming Products):**
- 2 tables: categoria, producto
- Simple catalog structure
- Category ID: String (codes like 'JM', 'AC')
- JSONB columns for specs and tags arrays
- Direct pricing (no resource calculations)

---

## Cost Optimization

**Estimated Monthly Costs:**
- **EC2 t2.micro**: Free tier eligible (12 months), then ~$8/month
- **NeonDB Free Tier**: 0.5 GB storage, 1 compute unit
- **NeonDB Paid**: Starting at $19/month for Pro tier
- **Data transfer**: Minimal for API usage

**To reduce costs:**
- Use t2.micro instance (free tier eligible)
- Start with NeonDB free tier for development
- Stop EC2 instance when not in use
- Set up billing alerts in AWS Console

---

## Next Steps After Deployment

1. ✅ Application running on EC2
2. ✅ Connected to NeonDB
3. ✅ Seed data loaded (10 categories, 47 products)
4. 🔄 Configure domain name (optional)
5. 🔄 Set up SSL/HTTPS with ALB or nginx
6. 🔄 Configure carrito service to call this API
7. 🔄 Set up monitoring and alerts
8. 🔄 Configure automated backups

Your inventory API is live! 🎮🚀
