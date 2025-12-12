# LUNARi Microservices - Complete Google Cloud Platform Deployment Guide

This guide covers the complete deployment of all three LUNARi microservices to Google Cloud Platform with custom domains.

## Overview

- **Usuario Service** → `user.aframuz.dev` (Port 8081)
- **Inventario Service** → `inventory.aframuz.dev` (Port 8082)
- **Carrito Service** → `cart.aframuz.dev` (Port 8083)

## Architecture

```
                          ┌─────────────────────┐
                          │   DNS (aframuz.dev) │
                          └──────────┬──────────┘
                                     │
                ┌────────────────────┼────────────────────┐
                │                    │                    │
        user.aframuz.dev    inventory.aframuz.dev   cart.aframuz.dev
                │                    │                    │
         ┌──────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
         │   Load      │      │   Load      │     │   Load      │
         │  Balancer   │      │  Balancer   │     │  Balancer   │
         └──────┬──────┘      └──────┬──────┘     └──────┬──────┘
                │                    │                    │
         ┌──────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
         │  Cloud Run  │      │  Cloud Run  │     │  Cloud Run  │
         │   Usuario   │◄─────┤ Inventario  │◄────┤   Carrito   │
         │  Service    │      │  Service    │     │  Service    │
         └──────┬──────┘      └──────┬──────┘     └──────┬──────┘
                │                    │                    │
         ┌──────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
         │   NeonDB    │      │   NeonDB    │     │   NeonDB    │
         │ lunari_users│      │lunari_invent│     │ lunari_cart │
         └─────────────┘      └─────────────┘     └─────────────┘
```

## Prerequisites

1. **Google Cloud Account** with billing enabled
2. **NeonDB Account** at https://neon.tech (3 separate databases)
3. **Domain** (aframuz.dev) with DNS management access
4. **Local Tools**:
   - Google Cloud SDK (`gcloud`)
   - Docker
   - Git

## Step-by-Step Deployment

### Phase 1: Prepare NeonDB Databases

Create three separate databases in NeonDB:

1. **lunari_users** - For Usuario service
2. **lunari_inventory** - For Inventario service
3. **lunari_cart** - For Carrito service

Run the SQL scripts:
```bash
# Execute on respective databases
psql [CONNECTION_STRING] -f script_creacion_tablas.sql
```

### Phase 2: Configure Google Cloud Project

```bash
# Set your project ID
export PROJECT_ID="your-gcp-project-id"
export REGION="us-central1"

# Authenticate
gcloud auth login

# Set project
gcloud config set project $PROJECT_ID

# Enable APIs
gcloud services enable \
  cloudbuild.googleapis.com \
  run.googleapis.com \
  secretmanager.googleapis.com \
  artifactregistry.googleapis.com \
  compute.googleapis.com \
  certificatemanager.googleapis.com
```

### Phase 3: Store Database Secrets

You can use the automated script or manual commands.

#### Option A: Automated (Recommended)

```bash
# From usuario directory
cd usuario/infrastructure
./setup-neondb-secrets.sh
```

#### Option B: Manual

```bash
# Usuario Service Secrets
echo -n 'ep-xxx.us-east-1.aws.neon.tech' | gcloud secrets create db-host-usuario --data-file=-
echo -n 'neondb_user' | gcloud secrets create db-user-usuario --data-file=-
echo -n 'your_password' | gcloud secrets create db-password-usuario --data-file=-
echo -n 'lunari_users' | gcloud secrets create db-name-usuario --data-file=-

# Inventario Service Secrets
echo -n 'ep-xxx.us-east-1.aws.neon.tech' | gcloud secrets create db-host-inventario --data-file=-
echo -n 'neondb_user' | gcloud secrets create db-user-inventario --data-file=-
echo -n 'your_password' | gcloud secrets create db-password-inventario --data-file=-
echo -n 'lunari_inventory' | gcloud secrets create db-name-inventario --data-file=-

# Carrito Service Secrets
echo -n 'ep-xxx.us-east-1.aws.neon.tech' | gcloud secrets create db-host-carrito --data-file=-
echo -n 'neondb_user' | gcloud secrets create db-user-carrito --data-file=-
echo -n 'your_password' | gcloud secrets create db-password-carrito --data-file=-
echo -n 'lunari_cart' | gcloud secrets create db-name-carrito --data-file=-
```

### Phase 4: Deploy Services to Cloud Run

Deploy each service in order (dependencies matter!):

#### 4.1 Deploy Usuario Service

```bash
cd usuario/
chmod +x infrastructure/deploy-to-gcp.sh
./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION usuario-service
```

Wait for completion. Note the Cloud Run URL.

#### 4.2 Deploy Inventario Service

```bash
cd ../inventario/
chmod +x infrastructure/deploy-to-gcp.sh
./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION inventario-service
```

#### 4.3 Deploy Carrito Service

```bash
cd ../carrito/
chmod +x infrastructure/deploy-to-gcp.sh
./infrastructure/deploy-to-gcp.sh $PROJECT_ID $REGION carrito-service
```

### Phase 5: Setup Custom Domains with Load Balancers

#### 5.1 Setup Usuario Service Domain (user.aframuz.dev)

```bash
cd usuario/
chmod +x infrastructure/setup-load-balancer.sh
./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION usuario-service user.aframuz.dev
```

Follow the prompts to:
1. Add DNS A record pointing to the static IP
2. Wait for DNS propagation
3. Wait for SSL certificate provisioning (10-60 minutes)

#### 5.2 Setup Inventario Service Domain (inventory.aframuz.dev)

```bash
cd ../inventario/
chmod +x infrastructure/setup-load-balancer.sh
./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION inventario-service inventory.aframuz.dev
```

#### 5.3 Setup Carrito Service Domain (cart.aframuz.dev)

```bash
cd ../carrito/
chmod +x infrastructure/setup-load-balancer.sh
./infrastructure/setup-load-balancer.sh $PROJECT_ID $REGION carrito-service cart.aframuz.dev
```

### Phase 6: Update Inter-Service URLs

Once all services are deployed with custom domains, update the Carrito service to use HTTPS URLs:

```bash
# Update Carrito service with custom domain URLs
gcloud run services update carrito-service \
  --region=$REGION \
  --set-env-vars="USUARIO_SERVICE_URL=https://user.aframuz.dev,INVENTARIO_SERVICE_URL=https://inventory.aframuz.dev"
```

## Verification

### Check Service Health

```bash
# Usuario Service
curl https://user.aframuz.dev/actuator/health

# Inventario Service
curl https://inventory.aframuz.dev/actuator/health

# Carrito Service
curl https://cart.aframuz.dev/actuator/health
```

### Access Swagger Documentation

- Usuario: https://user.aframuz.dev/swagger-ui/index.html
- Inventario: https://inventory.aframuz.dev/swagger-ui/index.html
- Carrito: https://cart.aframuz.dev/swagger-ui/index.html

### Test Inter-Service Communication

```bash
# Create a user via Usuario service
USER_ID=$(curl -X POST https://user.aframuz.dev/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}' \
  | jq -r '.id')

# Get cart for that user (Carrito will call Usuario to validate)
curl https://cart.aframuz.dev/api/v1/carts/$USER_ID
```

## DNS Configuration Summary

Add these A records to your DNS provider:

| Subdomain | Type | Value                    |
|-----------|------|--------------------------|
| user      | A    | [Static IP for usuario]  |
| inventory | A    | [Static IP for inventario] |
| cart      | A    | [Static IP for carrito]   |

To get the static IPs:

```bash
# Usuario
gcloud compute addresses describe lunari-usuario-ip --global --format="value(address)"

# Inventario
gcloud compute addresses describe lunari-inventario-ip --global --format="value(address)"

# Carrito
gcloud compute addresses describe lunari-carrito-ip --global --format="value(address)"
```

## Monitoring and Logging

### View Logs

```bash
# Usuario Service
gcloud run services logs tail usuario-service --region=$REGION

# Inventario Service
gcloud run services logs tail inventario-service --region=$REGION

# Carrito Service
gcloud run services logs tail carrito-service --region=$REGION
```

### View All Services

```bash
gcloud run services list --region=$REGION
```

### Monitor SSL Certificate Status

```bash
# Usuario
gcloud compute ssl-certificates describe usuario-cert --global

# Inventario
gcloud compute ssl-certificates describe inventario-cert --global

# Carrito
gcloud compute ssl-certificates describe carrito-cert --global
```

## Cost Optimization

### Enable Cloud CDN (Optional)

```bash
gcloud compute backend-services update usuario-backend --global --enable-cdn
gcloud compute backend-services update inventario-backend --global --enable-cdn
gcloud compute backend-services update carrito-backend --global --enable-cdn
```

### Scale to Zero When Idle

All services are configured with `--min-instances=0` to scale down when not in use.

## Troubleshooting

### SSL Certificate Stuck in PROVISIONING

1. Verify DNS is correctly configured:
   ```bash
   dig user.aframuz.dev A +short
   dig inventory.aframuz.dev A +short
   dig cart.aframuz.dev A +short
   ```

2. Check certificate status:
   ```bash
   gcloud compute ssl-certificates describe [CERT_NAME] --global
   ```

3. Wait up to 60 minutes for provisioning

### Service Returns 502 Bad Gateway

1. Check service logs:
   ```bash
   gcloud run services logs tail [SERVICE_NAME] --region=$REGION
   ```

2. Verify secrets are accessible:
   ```bash
   gcloud secrets versions access latest --secret=db-host-usuario
   ```

3. Check database connectivity from Cloud Run

### Inter-Service Communication Failing

1. Verify service URLs are correct:
   ```bash
   gcloud run services describe carrito-service --region=$REGION --format="value(spec.template.spec.containers[0].env)"
   ```

2. Check if Usuario and Inventario services are accessible:
   ```bash
   curl https://user.aframuz.dev/actuator/health
   curl https://inventory.aframuz.dev/actuator/health
   ```

## Cleanup (If Needed)

To delete all resources:

```bash
# Delete Cloud Run services
gcloud run services delete usuario-service --region=$REGION
gcloud run services delete inventario-service --region=$REGION
gcloud run services delete carrito-service --region=$REGION

# Delete load balancers and related resources
# Usuario
gcloud compute forwarding-rules delete usuario-https-forwarding-rule --global
gcloud compute forwarding-rules delete usuario-http-forwarding-rule --global
gcloud compute target-https-proxies delete usuario-https-proxy --global
gcloud compute target-http-proxies delete usuario-http-proxy --global
gcloud compute ssl-certificates delete usuario-cert --global
gcloud compute url-maps delete usuario-lb --global
gcloud compute url-maps delete usuario-lb-http --global
gcloud compute backend-services delete usuario-backend --global
gcloud compute network-endpoint-groups delete usuario-neg --region=$REGION
gcloud compute addresses delete lunari-usuario-ip --global

# Repeat for inventario and carrito with appropriate names
```

## Security Best Practices

1. **API Keys**: Store Inventario API keys in Secret Manager
2. **Transbank Credentials**: Store in Secret Manager for Carrito
3. **Network Policies**: Review and adjust K8s network policies if using GKE
4. **Cloud Armor**: Consider adding DDoS protection
5. **IAM**: Use least-privilege service accounts
6. **VPC**: Consider VPC Service Controls for enhanced security

## Next Steps

1. Set up monitoring and alerting with Cloud Monitoring
2. Configure Cloud Armor for DDoS protection
3. Set up Cloud CDN for better performance
4. Implement CI/CD with Cloud Build
5. Set up backup and disaster recovery
6. Configure rate limiting
7. Add custom error pages

## Support

For issues or questions:
- Check service logs: `gcloud run services logs tail [SERVICE_NAME]`
- Review GCP Console for service health
- Check NeonDB console for database status
- Verify DNS propagation with `dig` command

## Estimated Costs

With the current configuration (all services scaled to 0 when idle):

- **Cloud Run**: Pay only for requests and compute time
- **Load Balancer**: ~$18/month per load balancer
- **Static IP**: ~$7/month per IP
- **SSL Certificates**: Free (Google-managed)
- **NeonDB**: Free tier available (up to 3 databases)

**Total estimated monthly cost**: ~$75-100 (3 load balancers + 3 static IPs + minimal compute)

To reduce costs:
- Use a single load balancer with path-based routing
- Use Cloud Run URLs instead of custom domains (development only)
- Enable scale-to-zero (already configured)
