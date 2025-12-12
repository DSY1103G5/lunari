# LUNARi - Quick Deployment Reference

Quick reference for deploying LUNARi microservices to Google Cloud Platform.

## 🚀 Quick Start (3 Steps)

### 1. Setup Database Secrets

```bash
cd usuario/infrastructure
./setup-neondb-secrets.sh
```

This will prompt you for NeonDB connection strings for all three services.

### 2. Deploy All Services

```bash
# From project root
chmod +x deploy-all-services.sh
./deploy-all-services.sh [PROJECT_ID] [REGION]
```

### 3. Setup Custom Domains

```bash
# Usuario Service
cd usuario/
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] usuario-service user.aframuz.dev

# Inventario Service
cd ../inventario/
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] inventario-service inventory.aframuz.dev

# Carrito Service
cd ../carrito/
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] carrito-service cart.aframuz.dev
```

## 📋 Individual Service Deployment

### Usuario Service

```bash
cd usuario/

# Deploy to Cloud Run
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]

# Setup custom domain
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] usuario-service user.aframuz.dev
```

### Inventario Service

```bash
cd inventario/

# Deploy to Cloud Run
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]

# Setup custom domain
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] inventario-service inventory.aframuz.dev
```

### Carrito Service

```bash
cd carrito/

# Deploy to Cloud Run
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]

# Setup custom domain
./infrastructure/setup-load-balancer.sh [PROJECT_ID] [REGION] carrito-service cart.aframuz.dev
```

## 🔍 Verification Commands

```bash
# Check service health
curl https://user.aframuz.dev/actuator/health
curl https://inventory.aframuz.dev/actuator/health
curl https://cart.aframuz.dev/actuator/health

# View all services
gcloud run services list --region=[REGION]

# View logs
gcloud run services logs tail usuario-service --region=[REGION]
gcloud run services logs tail inventario-service --region=[REGION]
gcloud run services logs tail carrito-service --region=[REGION]

# Check SSL certificates
gcloud compute ssl-certificates describe usuario-cert --global
gcloud compute ssl-certificates describe inventario-cert --global
gcloud compute ssl-certificates describe carrito-cert --global
```

## 🌐 Service URLs

| Service    | Cloud Run URL                        | Custom Domain              |
|------------|--------------------------------------|----------------------------|
| Usuario    | [AUTO-GENERATED].run.app             | https://user.aframuz.dev   |
| Inventario | [AUTO-GENERATED].run.app             | https://inventory.aframuz.dev |
| Carrito    | [AUTO-GENERATED].run.app             | https://cart.aframuz.dev   |

## 📊 Service Architecture

```
usuario/
├── Dockerfile                      # Multi-stage build
├── .dockerignore                   # Build optimization
├── .env.example                    # Environment template
└── infrastructure/
    ├── deploy-to-gcp.sh           # Cloud Run deployment
    ├── setup-load-balancer.sh     # Custom domain setup
    ├── setup-neondb-secrets.sh    # Database secrets
    └── k8s-usuario-deployment.yaml # Kubernetes config

inventario/
├── Dockerfile
├── .dockerignore
├── .env.example
└── infrastructure/
    ├── deploy-to-gcp.sh
    ├── setup-load-balancer.sh
    └── k8s-inventario-deployment.yaml

carrito/
├── Dockerfile
├── .dockerignore
├── .env.example
└── infrastructure/
    ├── deploy-to-gcp.sh
    ├── setup-load-balancer.sh
    └── k8s-carrito-deployment.yaml
```

## 🔐 Required Secrets

### Usuario Service
- `db-host-usuario`
- `db-user-usuario`
- `db-password-usuario`
- `db-name-usuario`

### Inventario Service
- `db-host-inventario`
- `db-user-inventario`
- `db-password-inventario`
- `db-name-inventario`

### Carrito Service
- `db-host-carrito`
- `db-user-carrito`
- `db-password-carrito`
- `db-name-carrito`

## 🛠️ Common Operations

### Update a Service

```bash
# Rebuild and redeploy
cd [SERVICE_NAME]/
./infrastructure/deploy-to-gcp.sh [PROJECT_ID] [REGION]
```

### View Real-time Logs

```bash
gcloud run services logs tail [SERVICE_NAME] --region=[REGION]
```

### Update Environment Variables

```bash
gcloud run services update [SERVICE_NAME] \
  --region=[REGION] \
  --set-env-vars="KEY=VALUE"
```

### Enable Cloud CDN

```bash
gcloud compute backend-services update usuario-backend --global --enable-cdn
gcloud compute backend-services update inventario-backend --global --enable-cdn
gcloud compute backend-services update carrito-backend --global --enable-cdn
```

## 🔄 Update Inter-Service URLs

After custom domains are active:

```bash
gcloud run services update carrito-service \
  --region=[REGION] \
  --set-env-vars="USUARIO_SERVICE_URL=https://user.aframuz.dev,INVENTARIO_SERVICE_URL=https://inventory.aframuz.dev"
```

## 📱 DNS Configuration

Add these A records to your DNS provider (aframuz.dev):

| Subdomain | Type | Value                          |
|-----------|------|--------------------------------|
| user      | A    | [Get from GCP static IP]       |
| inventory | A    | [Get from GCP static IP]       |
| cart      | A    | [Get from GCP static IP]       |

Get static IPs:
```bash
gcloud compute addresses describe lunari-usuario-ip --global --format="value(address)"
gcloud compute addresses describe lunari-inventario-ip --global --format="value(address)"
gcloud compute addresses describe lunari-carrito-ip --global --format="value(address)"
```

## ⚠️ Troubleshooting

### SSL Certificate Stuck in PROVISIONING
- Verify DNS with: `dig [subdomain].aframuz.dev A +short`
- Wait up to 60 minutes
- Check status: `gcloud compute ssl-certificates describe [CERT_NAME] --global`

### Service Returns 502
- Check logs: `gcloud run services logs tail [SERVICE_NAME]`
- Verify database connectivity
- Check secret values: `gcloud secrets versions access latest --secret=[SECRET_NAME]`

### Build Fails
- Check Dockerfile syntax
- Verify pom.xml is correct
- Check Docker authentication: `gcloud auth configure-docker`

## 💰 Cost Estimate

- Cloud Run: Pay per use (scales to 0)
- Load Balancer: ~$18/month each (3 total)
- Static IP: ~$7/month each (3 total)
- SSL Certificates: Free (Google-managed)
- **Total**: ~$75-100/month

## 📚 Full Documentation

For complete details, see: `GCP_DEPLOYMENT_COMPLETE_GUIDE.md`

## 🆘 Support

- View service status: GCP Console → Cloud Run
- Check NeonDB: https://console.neon.tech
- View logs: Cloud Logging in GCP Console
- DNS propagation: https://dnschecker.org
