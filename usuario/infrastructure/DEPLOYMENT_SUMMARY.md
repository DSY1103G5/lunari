# LUNARi GCP Deployment - Summary of Changes

This document summarizes the infrastructure updates made to support deployment to Google Cloud Platform with NeonDB.

## What Changed

### Database: Cloud SQL → NeonDB

**Previous**: Used Google Cloud SQL for PostgreSQL
**Now**: Uses NeonDB (serverless PostgreSQL)

**Why?**
- 40-60% cost savings
- Scales to zero automatically
- Simpler deployment (no Cloud SQL Proxy)
- Free tier for development
- Instant provisioning

See [NEONDB_VS_CLOUDSQL.md](./NEONDB_VS_CLOUDSQL.md) for detailed comparison.

## Files Created

### Documentation
1. **GCP_DEPLOYMENT_GUIDE.md** (26KB)
   - Comprehensive deployment guide
   - Cloud Run and GKE options
   - NeonDB setup instructions
   - Monitoring and troubleshooting

2. **GCP_QUICK_START.md** (13KB)
   - 30-minute quick start guide
   - Step-by-step automated deployment
   - Common issues and solutions
   - Cost estimates

3. **NEONDB_VS_CLOUDSQL.md** (7KB)
   - Detailed comparison of NeonDB vs Cloud SQL
   - Cost analysis
   - Performance benchmarks
   - Migration guide

4. **README.md** (11KB - updated)
   - Infrastructure overview
   - Database setup options
   - Deployment workflows
   - Troubleshooting guide

### Scripts
5. **deploy-to-gcp.sh** (6.3KB)
   - Automated GCP deployment script
   - Builds and pushes Docker images
   - Deploys to Cloud Run with NeonDB
   - Validates secrets before deployment

6. **setup-neondb-secrets.sh** (4.9KB)
   - Interactive NeonDB secrets setup
   - Parses connection strings
   - Stores credentials in Secret Manager
   - Validates input format

### Container Configuration
7. **Dockerfile** (in usuario/)
   - Multi-stage Docker build
   - Maven build stage + runtime stage
   - Security hardening (non-root user)
   - Health checks
   - JVM optimization

8. **.dockerignore**
   - Optimizes Docker build
   - Excludes unnecessary files
   - Reduces image size

### Kubernetes
9. **k8s-usuario-deployment.yaml** (5KB)
   - Kubernetes deployment manifest
   - Uses NeonDB (no Cloud SQL Proxy sidecar)
   - Horizontal Pod Autoscaler
   - Pod Disruption Budget
   - Network policies

## Architecture Changes

### Before (Cloud SQL)
```
Cloud Run → Cloud SQL Proxy → Cloud SQL PostgreSQL
```
- Required Cloud SQL Proxy sidecar/connector
- Complex IAM setup
- Always-running database ($7.50/month minimum)
- VPC connector for private networking

### After (NeonDB)
```
Cloud Run → (Internet/SSL) → NeonDB PostgreSQL
```
- Direct connection over SSL
- Simple secrets management
- Scales to zero when idle ($0/month possible)
- No proxy needed

## Deployment Process Changes

### Old Process (Cloud SQL)
```bash
# 1. Create Cloud SQL instance (5-10 minutes)
gcloud sql instances create lunari-db --tier=db-f1-micro

# 2. Configure networking
gcloud compute networks vpc-access connectors create connector

# 3. Set up Cloud SQL Proxy
gcloud iam service-accounts create cloudsql-proxy

# 4. Deploy with complex flags
gcloud run deploy service \
  --add-cloudsql-instances=project:region:instance \
  --set-env-vars="DB_HOST=/cloudsql/connection-name"
```

### New Process (NeonDB)
```bash
# 1. Create NeonDB database (< 10 seconds, via web UI)
# Visit https://neon.tech

# 2. Store secrets
./infrastructure/setup-neondb-secrets.sh

# 3. Deploy with simple command
./infrastructure/deploy-to-gcp.sh project-id region
```

## Environment Variables Changes

### Before (Cloud SQL)
```bash
DB_HOST=/cloudsql/project:region:instance  # Unix socket path
DB_PORT=5432
DB_NAME=lunari_users
DB_USER=lunari-app
DB_PASSWORD=secret
```

### After (NeonDB)
```bash
DB_HOST=ep-xyz.region.aws.neon.tech  # Direct hostname
DB_PORT=5432  # Standard port
DB_NAME=lunari_users
DB_USER=neondb_owner
DB_PASSWORD=secret
# SSL is automatically enabled (required by NeonDB)
```

## Secret Management Changes

### Before (Cloud SQL)
```bash
# Fewer secrets needed (host is connection name)
echo -n "password" | gcloud secrets create db-password --data-file=-
echo -n "lunari-app" | gcloud secrets create db-user --data-file=-
```

### After (NeonDB)
```bash
# More granular secrets (includes host)
echo -n "ep-xyz.aws.neon.tech" | gcloud secrets create db-host-usuario --data-file=-
echo -n "neondb_owner" | gcloud secrets create db-user-usuario --data-file=-
echo -n "password" | gcloud secrets create db-password-usuario --data-file=-
echo -n "lunari_users" | gcloud secrets create db-name-usuario --data-file=-
```

**Benefit**: Secrets are service-specific (usuario, inventario, carrito) making multi-database setup clearer.

## Cost Impact

### Development Environment
- **Before**: Cloud SQL ($7.50/mo) + Cloud Run ($0-5/mo) = **$7.50-12.50/month**
- **After**: NeonDB Free Tier ($0/mo) + Cloud Run ($0-5/mo) = **$0-5/month**
- **Savings**: **$7.50/month (100% on database)**

### Production Environment (Moderate Traffic)
- **Before**: Cloud SQL db-g1-small ($60/mo) + Cloud Run ($30/mo) = **$90/month**
- **After**: NeonDB Pro ($19/mo) + Cloud Run ($30/mo) = **$49/month**
- **Savings**: **$41/month (45%)**

### Annual Savings
- **Development**: **$90/year**
- **Production**: **$492/year**
- **3 Microservices in Production**: **$1,476/year**

## Migration Steps (If Needed)

If you need to switch from NeonDB back to Cloud SQL:

```bash
# 1. Export from NeonDB
pg_dump "postgres://user:pass@host/db?sslmode=require" > backup.sql

# 2. Create Cloud SQL instance
gcloud sql instances create lunari-db --tier=db-g1-small

# 3. Create database
gcloud sql databases create lunari_users --instance=lunari-db

# 4. Import data
gcloud sql connect lunari-db --user=postgres < backup.sql

# 5. Update deployment to use Cloud SQL connection
gcloud run services update usuario-service \
  --add-cloudsql-instances=project:region:lunari-db \
  --set-env-vars="DB_HOST=/cloudsql/project:region:lunari-db"
```

## Testing Checklist

Before deploying to production:

- [ ] Test connection to NeonDB from local machine
- [ ] Verify SSL is enabled (required by NeonDB)
- [ ] Load database schema and seed data
- [ ] Test Cloud Run deployment in staging
- [ ] Verify secrets are correctly injected
- [ ] Test inter-service communication (Carrito → Usuario)
- [ ] Monitor logs for connection errors
- [ ] Test database performance under load
- [ ] Verify backups are working (NeonDB auto-backup)
- [ ] Test disaster recovery procedure

## Rollback Plan

If issues occur with NeonDB:

1. **Keep Cloud SQL scripts** - They're still in the guide as "Option B"
2. **Export data regularly** - Use `pg_dump` for backups
3. **Test migration** - Practice Cloud SQL migration in staging
4. **Monitor costs** - NeonDB has billing alerts
5. **Have fallback ready** - Can switch to Cloud SQL in 15-20 minutes

## Support Resources

- **NeonDB Support**: support@neon.tech
- **NeonDB Docs**: https://neon.tech/docs
- **NeonDB Community**: https://discord.gg/neon
- **GCP Cloud Run**: https://cloud.google.com/run/docs
- **PostgreSQL**: https://www.postgresql.org/docs

## Next Steps

1. **For Developers**:
   - Read [GCP_QUICK_START.md](./GCP_QUICK_START.md)
   - Create NeonDB account and databases
   - Run `./setup-neondb-secrets.sh`
   - Deploy with `./deploy-to-gcp.sh`

2. **For DevOps**:
   - Review [GCP_DEPLOYMENT_GUIDE.md](./GCP_DEPLOYMENT_GUIDE.md)
   - Set up CI/CD with Cloud Build
   - Configure monitoring and alerts
   - Plan disaster recovery

3. **For Managers**:
   - Review [NEONDB_VS_CLOUDSQL.md](./NEONDB_VS_CLOUDSQL.md)
   - Approve cost savings
   - Schedule quarterly review of database performance

---

**Migration Date**: December 2025
**Status**: ✅ Complete
**Team**: LUNARi Development Team
