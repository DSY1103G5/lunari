# NeonDB vs Cloud SQL: Why We Chose NeonDB

This document explains why LUNARi microservices use NeonDB instead of Google Cloud SQL.

## Quick Comparison

| Feature | NeonDB | Cloud SQL |
|---------|--------|-----------|
| **Pricing (Free Tier)** | 3GB storage, unlimited compute | No free tier |
| **Minimum Cost** | $0/month (free tier) | ~$7.50/month (db-f1-micro) |
| **Production Cost** | $19-69/month | $60-200/month |
| **Provisioning Time** | Instant (< 10 seconds) | 5-10 minutes |
| **Scales to Zero** | ✅ Yes, automatically | ❌ No, always running |
| **Cold Starts** | ❌ None | ✅ Yes (when stopped) |
| **Setup Complexity** | Simple (connection string) | Complex (Cloud SQL Proxy, IAM) |
| **SSL/TLS** | Always enabled | Requires configuration |
| **Backups** | Automatic, point-in-time | Manual configuration |
| **Branching** | ✅ Yes (database branches) | ❌ No |

## Cost Analysis

### Development/Testing Environment

**NeonDB (Free Tier):**
```
Database: $0/month
Cloud Run: $0-5/month (within free tier)
Total: $0-5/month
```

**Cloud SQL:**
```
Database (db-f1-micro): $7.50/month
Cloud Run: $0-5/month
Total: $7.50-12.50/month
```

**Savings: 60-100%**

### Production Environment (Moderate Traffic)

**NeonDB Pro:**
```
Database (10GB storage): $19/month
Cloud Run (2-3 instances): $30/month
Total: ~$49/month
```

**Cloud SQL:**
```
Database (db-g1-small): $60/month
Cloud Run (2-3 instances): $30/month
Total: ~$90/month
```

**Savings: 45%**

### Production Environment (High Traffic)

**NeonDB Pro:**
```
Database (50GB storage): $69/month
Cloud Run (5-10 instances): $80/month
Load Balancer: $18/month
Total: ~$167/month
```

**Cloud SQL:**
```
Database (db-n1-standard-1): $120/month
Cloud Run (5-10 instances): $80/month
Load Balancer: $18/month
Total: ~$218/month
```

**Savings: 23%**

## Why NeonDB is Better for Microservices

### 1. Serverless Architecture Alignment

**NeonDB:**
- Scales to zero automatically when idle
- Pay only for actual compute time
- Perfect match for Cloud Run's serverless model
- No wasted compute on idle databases

**Cloud SQL:**
- Always running, even when no traffic
- Fixed hourly cost regardless of usage
- Mismatch with serverless compute model
- Paying for idle time

### 2. Developer Experience

**NeonDB:**
```bash
# Simple setup - just use connection string
export DB_URL="postgres://user:pass@host/db?sslmode=require"
psql "$DB_URL"

# Deploy to Cloud Run
gcloud run deploy service \
  --set-secrets="DB_HOST=db-host:latest,DB_PASSWORD=db-password:latest"
```

**Cloud SQL:**
```bash
# Complex setup - need Cloud SQL Proxy, IAM, etc.
gcloud sql instances create db --tier=db-f1-micro --region=us-central1
gcloud sql users create user --instance=db
gcloud iam service-accounts create cloudsql-proxy
# ... many more steps

# Deploy requires Cloud SQL proxy sidecar
gcloud run deploy service \
  --add-cloudsql-instances=project:region:instance \
  --set-env-vars="DB_HOST=/cloudsql/connection-name"
```

### 3. Database Branching (Unique to NeonDB)

NeonDB supports **database branching** - create instant copies of your database for testing:

```bash
# Create branch for testing
neon branches create --name=feature-test

# Test migrations on branch
psql "postgres://user:pass@branch-host/db" < migration.sql

# Merge to main or delete if issues
neon branches delete feature-test
```

**Use Cases:**
- Test migrations without affecting production
- Create staging environments instantly
- Review database changes in pull requests
- Zero cost for short-lived branches

### 4. Point-in-Time Recovery

**NeonDB:**
- Automatic continuous backups
- Restore to any point in the last 7 days (free tier) or 30 days (Pro)
- No configuration needed
- Instant restoration

**Cloud SQL:**
- Manual backup configuration required
- Limited restore points
- Additional storage costs
- Slower restoration process

### 5. Connection Management

**NeonDB:**
- Direct PostgreSQL connections over SSL
- Connection pooling built-in
- No proxy required
- Standard PostgreSQL drivers work

**Cloud SQL:**
- Requires Cloud SQL Proxy for secure connections
- Adds complexity to deployment
- Extra latency from proxy
- Additional configuration for GKE

## When to Consider Cloud SQL

Cloud SQL might be better if:

1. **GCP Ecosystem Lock-in**: You want everything in Google Cloud
2. **VPC Peering**: Need private network connections within GCP
3. **Enterprise Support**: Require Google enterprise SLAs
4. **Legacy Systems**: Existing Cloud SQL integration
5. **Regulatory Requirements**: Data must stay in specific GCP regions
6. **Very High Traffic**: > 1TB database with constant high load

## Performance Comparison

### Latency

**From Cloud Run to NeonDB:**
- Average: 10-20ms (public internet)
- 99th percentile: 30-50ms

**From Cloud Run to Cloud SQL:**
- Average: 5-15ms (private network with Cloud SQL Proxy)
- 99th percentile: 20-40ms

**Verdict**: Cloud SQL has slightly lower latency, but NeonDB is close enough for most applications.

### Throughput

Both can handle similar throughput for typical microservices:
- NeonDB: Up to 10,000 queries/second per database
- Cloud SQL: Up to 15,000 queries/second (depends on tier)

For LUNARi's microservices (< 1000 QPS expected), both are more than sufficient.

## Security Comparison

### NeonDB
- ✅ SSL/TLS always enforced
- ✅ Automatic certificate rotation
- ✅ IP allowlist support
- ✅ Role-based access control
- ⚠️ Public endpoint (over encrypted connection)

### Cloud SQL
- ✅ SSL/TLS configurable
- ✅ Private IP support (VPC)
- ✅ Cloud IAM integration
- ✅ Audit logging
- ✅ Private network option

**Verdict**: Cloud SQL has more security options, but NeonDB's security is sufficient for most applications.

## Migration Path

If you need to migrate to Cloud SQL later:

```bash
# Export from NeonDB
pg_dump "$NEONDB_URL" > backup.sql

# Import to Cloud SQL
gcloud sql connect instance --user=postgres < backup.sql
```

Migration is straightforward since both use standard PostgreSQL.

## Recommendation

**Use NeonDB** for LUNARi microservices because:
1. ✅ **40-60% cost savings**
2. ✅ **Simpler deployment** (no proxy, no IAM complexity)
3. ✅ **Better for serverless** (scales to zero)
4. ✅ **Faster provisioning** (instant vs 5-10 minutes)
5. ✅ **Database branching** for testing
6. ✅ **Free tier** for development

**Switch to Cloud SQL only if**:
- You need VPC private networking
- You require Google enterprise SLAs
- You have regulatory requirements for data location
- You're already heavily invested in GCP ecosystem

## Resources

- **NeonDB Pricing**: https://neon.tech/pricing
- **Cloud SQL Pricing**: https://cloud.google.com/sql/pricing
- **NeonDB Docs**: https://neon.tech/docs
- **Performance Comparison**: https://neon.tech/blog/neon-vs-cloud-sql

---

**Decision Date**: December 2025
**Reviewed By**: LUNARi Development Team
**Next Review**: Q2 2026
