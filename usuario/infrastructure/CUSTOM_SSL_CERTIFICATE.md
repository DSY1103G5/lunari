# Using Custom SSL Certificates with Load Balancer

This guide shows how to use your own SSL certificate (instead of Google-managed certificates) when setting up the Application Load Balancer for your Cloud Run service.

## When to Use Custom Certificates

**Use custom SSL certificates when:**
- ✅ You already have a valid SSL certificate from a Certificate Authority (CA)
- ✅ You have organizational requirements to use specific CA providers
- ✅ You need Extended Validation (EV) or Organization Validation (OV) certificates
- ✅ You want immediate HTTPS availability (no provisioning wait time)
- ✅ Your certificate covers multiple domains/subdomains (wildcard certificate)

**Use Google-managed certificates when:**
- ✅ You don't have an existing certificate (easier, free, automatic renewal)
- ✅ You're okay with 10-60 minute provisioning time
- ✅ You want automatic certificate renewal (zero maintenance)

## Prerequisites

Before starting, you need:

1. **SSL Certificate file** (PEM format) - Usually named `certificate.crt` or `cert.pem`
2. **Private key file** (PEM format) - Usually named `private.key` or `key.pem`
3. **(Optional) CA Bundle/Chain** - Intermediate certificates from your CA

### Certificate File Formats

Your certificate files must be in **PEM format**. PEM files are text files that look like this:

**Certificate (cert.pem):**
```
-----BEGIN CERTIFICATE-----
MIIFXzCCBEegAwIBAgISA+X9v8...
... (base64 encoded data) ...
-----END CERTIFICATE-----
```

**Private Key (key.pem):**
```
-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEF...
... (base64 encoded data) ...
-----END PRIVATE KEY-----
```

### Converting Other Formats to PEM

If your certificate is in another format, convert it first:

#### From PFX/P12 to PEM

```bash
# Extract certificate
openssl pkcs12 -in certificate.pfx -clcerts -nokeys -out cert.pem

# Extract private key (will prompt for password)
openssl pkcs12 -in certificate.pfx -nocerts -nodes -out key.pem
```

#### From DER to PEM

```bash
# Convert certificate
openssl x509 -inform DER -in certificate.der -out cert.pem

# Convert private key
openssl rsa -inform DER -in private.der -out key.pem
```

#### From CRT/CER to PEM

```bash
# Most .crt and .cer files are already in PEM format
# Just verify and rename:
cat certificate.crt  # Should show BEGIN CERTIFICATE
mv certificate.crt cert.pem
```

### Creating a Certificate Bundle (If You Have Intermediate Certificates)

If your CA provided intermediate certificates, combine them with your certificate:

```bash
# Create full certificate chain
cat cert.pem intermediate.pem root.pem > fullchain.pem

# Use fullchain.pem as your certificate file
```

**Order matters**: Your domain certificate first, then intermediate, then root.

## Preparing Your Certificate Files

### 1. Organize Your Files

```bash
# Create a secure directory for certificates
mkdir -p ~/ssl-certs
chmod 700 ~/ssl-certs
cd ~/ssl-certs

# Place your certificate files here
# - cert.pem (or fullchain.pem if you have intermediates)
# - key.pem
```

### 2. Verify Certificate Details

```bash
# Check certificate information
openssl x509 -in cert.pem -text -noout

# Verify domains covered by the certificate
openssl x509 -in cert.pem -noout -text | grep -A1 "Subject Alternative Name"

# Should show something like:
# DNS:user.mydomain.com

# Check expiration date
openssl x509 -in cert.pem -noout -enddate
```

### 3. Verify Certificate and Key Match

```bash
# Get certificate modulus
CERT_MODULUS=$(openssl x509 -noout -modulus -in cert.pem | openssl md5)

# Get key modulus
KEY_MODULUS=$(openssl rsa -noout -modulus -in key.pem | openssl md5)

# Compare (should be identical)
echo "Certificate: $CERT_MODULUS"
echo "Key: $KEY_MODULUS"

# If they don't match, you have the wrong key file!
```

### 4. Remove Passphrase from Private Key (If Present)

GCP Load Balancer requires an unencrypted private key:

```bash
# Check if key is encrypted
head -1 key.pem
# If you see "BEGIN ENCRYPTED PRIVATE KEY", remove passphrase:

# Remove passphrase
openssl rsa -in key.pem -out key-nopass.pem

# Use key-nopass.pem for deployment
mv key-nopass.pem key.pem
```

## Deployment with Custom Certificate

### Option 1: Using the Automated Script (Recommended)

```bash
cd infrastructure/

# Run script with certificate files as additional arguments
./setup-load-balancer.sh \
  lunari-microservices \
  us-central1 \
  usuario-service \
  user.mydomain.com \
  ~/ssl-certs/cert.pem \
  ~/ssl-certs/key.pem

# Script will:
# 1. Validate certificate files exist
# 2. Upload certificate to GCP
# 3. Configure load balancer with your certificate
# 4. Certificate is immediately ACTIVE (no wait time)
```

**Script output:**
```
[INFO] Configuration:
  Project ID: lunari-microservices
  Region: us-central1
  Service: usuario-service
  Domain: user.mydomain.com
  SSL Type: custom
  Certificate: /home/user/ssl-certs/cert.pem
  Private Key: /home/user/ssl-certs/key.pem

[INFO] Custom SSL certificate files validated
[INFO] Uploading custom SSL certificate...
[SUCCESS] Custom SSL certificate uploaded

Certificate Type: Custom (Self-managed)
Certificate Status: ACTIVE
[SUCCESS] Custom SSL certificate is ACTIVE and ready!
```

### Option 2: Manual Setup

If you prefer manual control:

```bash
# 1. Set variables
PROJECT_ID="lunari-microservices"
CERT_FILE="~/ssl-certs/cert.pem"
KEY_FILE="~/ssl-certs/key.pem"

# 2. Upload certificate to GCP
gcloud compute ssl-certificates create usuario-cert \
  --certificate=$CERT_FILE \
  --private-key=$KEY_FILE \
  --global

# 3. Verify upload
gcloud compute ssl-certificates describe usuario-cert --global

# 4. Continue with rest of load balancer setup
# (Follow LOAD_BALANCER_SETUP.md, but skip certificate creation step)
```

## Certificate Management

### Viewing Certificate Information

```bash
# List all certificates
gcloud compute ssl-certificates list

# View certificate details
gcloud compute ssl-certificates describe usuario-cert --global

# Check expiration date
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="value(expireTime)"
```

### Updating/Renewing Certificate

When your certificate expires or needs renewal:

```bash
# 1. Get new certificate from your CA
# 2. Prepare new certificate files (cert.pem, key.pem)

# 3. Create new certificate in GCP
gcloud compute ssl-certificates create usuario-cert-new \
  --certificate=new-cert.pem \
  --private-key=new-key.pem \
  --global

# 4. Update HTTPS proxy to use new certificate
gcloud compute target-https-proxies update usuario-https-proxy \
  --ssl-certificates=usuario-cert-new \
  --global

# 5. Verify new certificate is in use
gcloud compute target-https-proxies describe usuario-https-proxy \
  --global \
  --format="value(sslCertificates)"

# 6. Test HTTPS connection
curl -v https://user.mydomain.com/actuator/health 2>&1 | grep "expire date"

# 7. Delete old certificate (after confirming new one works)
gcloud compute ssl-certificates delete usuario-cert --global --quiet
```

### Certificate Rotation Script

Create a script for easier certificate rotation:

```bash
#!/bin/bash
# rotate-ssl-cert.sh

NEW_CERT=$1
NEW_KEY=$2
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
CERT_NAME="usuario-cert-${TIMESTAMP}"

# Upload new certificate
gcloud compute ssl-certificates create $CERT_NAME \
  --certificate=$NEW_CERT \
  --private-key=$NEW_KEY \
  --global

# Update proxy
gcloud compute target-https-proxies update usuario-https-proxy \
  --ssl-certificates=$CERT_NAME \
  --global

echo "Certificate rotated to: $CERT_NAME"
echo "Test with: curl -v https://user.mydomain.com"
```

Usage:
```bash
chmod +x rotate-ssl-cert.sh
./rotate-ssl-cert.sh new-cert.pem new-key.pem
```

## Wildcard Certificates

If you have a wildcard certificate (e.g., `*.mydomain.com`), you can use it for all subdomains:

```bash
# Single wildcard certificate for all services
./setup-load-balancer.sh \
  lunari-microservices us-central1 usuario-service \
  user.mydomain.com \
  wildcard-cert.pem wildcard-key.pem

# Reuse the same certificate for other services
# For inventario:
./setup-load-balancer.sh \
  lunari-microservices us-central1 inventario-service \
  inventory.mydomain.com \
  wildcard-cert.pem wildcard-key.pem

# For carrito:
./setup-load-balancer.sh \
  lunari-microservices us-central1 carrito-service \
  cart.mydomain.com \
  wildcard-cert.pem wildcard-key.pem
```

## Security Best Practices

### 1. Secure Storage

```bash
# Set proper permissions on certificate files
chmod 600 ~/ssl-certs/key.pem
chmod 644 ~/ssl-certs/cert.pem

# Never commit certificates to git
echo "*.pem" >> .gitignore
echo "*.key" >> .gitignore
echo "*.crt" >> .gitignore
```

### 2. Delete Local Copies After Upload

```bash
# After successful upload to GCP
# Securely delete local copies
shred -vfz -n 10 ~/ssl-certs/key.pem
shred -vfz -n 10 ~/ssl-certs/cert.pem

# Or move to encrypted storage
```

### 3. Set Expiration Reminders

```bash
# Check certificate expiration
EXPIRE_DATE=$(gcloud compute ssl-certificates describe usuario-cert \
  --global --format="value(expireTime)")

echo "Certificate expires: $EXPIRE_DATE"

# Set a reminder 30 days before expiration
# (Use your calendar or monitoring system)
```

### 4. Use Cloud Secrets Manager for Private Keys (Advanced)

```bash
# Store private key in Secret Manager
gcloud secrets create ssl-private-key --data-file=key.pem

# Retrieve when needed
gcloud secrets versions access latest --secret=ssl-private-key > key.pem
```

## Comparing Custom vs Google-Managed Certificates

| Feature | Custom Certificate | Google-Managed |
|---------|-------------------|----------------|
| **Setup Time** | Immediate | 10-60 minutes |
| **Cost** | $0-$100+/year (CA fees) | FREE |
| **Renewal** | Manual (every 1-2 years) | Automatic |
| **Validation Types** | DV, OV, EV | DV only |
| **Wildcard Support** | Yes (if purchased) | Yes |
| **Management Effort** | High (manual rotation) | None |
| **Use Case** | Enterprise, compliance | Most applications |

## Troubleshooting

### Certificate Upload Fails

```bash
# Error: "Invalid certificate"
# Solution: Verify PEM format
openssl x509 -in cert.pem -text -noout

# Error: "Certificate and key do not match"
# Solution: Verify they match
openssl x509 -noout -modulus -in cert.pem | openssl md5
openssl rsa -noout -modulus -in key.pem | openssl md5
# MD5 hashes should match
```

### Certificate Not Working

```bash
# Check certificate is attached to proxy
gcloud compute target-https-proxies describe usuario-https-proxy \
  --global \
  --format="value(sslCertificates)"

# Test SSL connection
openssl s_client -connect user.mydomain.com:443 -servername user.mydomain.com

# Check certificate details served
curl -vI https://user.mydomain.com 2>&1 | grep -A10 "SSL certificate"
```

### Domain Mismatch Error

```bash
# Error: "Certificate does not match domain"
# Check domains in certificate
openssl x509 -in cert.pem -noout -text | grep -A1 "Subject Alternative Name"

# Make sure your domain is listed
# For user.mydomain.com, you need:
#   DNS:user.mydomain.com
# OR wildcard:
#   DNS:*.mydomain.com
```

## Getting SSL Certificates

### Free Options

**Let's Encrypt** (Free, DV, 90-day validity):
```bash
# Install certbot
sudo apt-get install certbot

# Get certificate (DNS challenge for wildcard)
sudo certbot certonly --manual --preferred-challenges dns \
  -d user.mydomain.com

# Certificate will be at:
# /etc/letsencrypt/live/user.mydomain.com/fullchain.pem
# /etc/letsencrypt/live/user.mydomain.com/privkey.pem
```

**ZeroSSL** (Free, DV, 90-day validity):
- Website: https://zerossl.com
- Manual certificate generation with email validation

### Paid Options

**Commercial CAs** (starting $10-$1000+/year):
- **Sectigo** (formerly Comodo) - $10-$50/year
- **DigiCert** - $200-$1000+/year (EV available)
- **GoDaddy** - $70-$300/year
- **GlobalSign** - $250-$800/year

**Purchase considerations:**
- DV (Domain Validation) - Cheapest, verifies domain ownership only
- OV (Organization Validation) - Shows company name, more trust
- EV (Extended Validation) - Highest trust, green bar, expensive

## Complete Example: Using Let's Encrypt Certificate

```bash
# 1. Get certificate from Let's Encrypt
sudo certbot certonly --manual --preferred-challenges dns \
  -d user.mydomain.com

# 2. Copy certificates to safe location
sudo cp /etc/letsencrypt/live/user.mydomain.com/fullchain.pem ~/ssl-certs/cert.pem
sudo cp /etc/letsencrypt/live/user.mydomain.com/privkey.pem ~/ssl-certs/key.pem
sudo chown $USER:$USER ~/ssl-certs/*.pem
chmod 600 ~/ssl-certs/key.pem

# 3. Deploy with custom certificate
cd infrastructure/
./setup-load-balancer.sh \
  lunari-microservices us-central1 usuario-service \
  user.mydomain.com \
  ~/ssl-certs/cert.pem \
  ~/ssl-certs/key.pem

# 4. Test
curl https://user.mydomain.com/actuator/health

# 5. Set reminder to renew in 60 days (Let's Encrypt expires in 90)
```

## Next Steps

After setting up with a custom certificate:

1. **Set renewal reminders** - Custom certificates require manual renewal
2. **Document certificate source** - Note where you got it and renewal process
3. **Test HTTPS** - Verify certificate is working correctly
4. **Monitor expiration** - Set up alerts for certificate expiration
5. **Plan rotation process** - Have a process for certificate updates

## Related Documentation

- **Main Guide**: `LOAD_BALANCER_SETUP.md` - Complete load balancer setup
- **Quick Start**: `DOMAIN_SETUP_QUICK_START.md` - Quick domain setup
- **Script Usage**: `setup-load-balancer.sh` - Automated deployment

## Support

For certificate-related issues:
- **OpenSSL Documentation**: https://www.openssl.org/docs/
- **Let's Encrypt**: https://letsencrypt.org/docs/
- **GCP SSL Certificates**: https://cloud.google.com/load-balancing/docs/ssl-certificates
