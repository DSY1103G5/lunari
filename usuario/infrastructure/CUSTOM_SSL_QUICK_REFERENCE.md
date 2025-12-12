# Custom SSL Certificate - Quick Reference

Quick commands for using your own SSL certificate with the Load Balancer.

## Prepare Your Certificate

### Check Certificate Format

```bash
# View certificate details
openssl x509 -in cert.pem -text -noout

# Check domains in certificate
openssl x509 -in cert.pem -noout -text | grep -A1 "Subject Alternative Name"

# Check expiration
openssl x509 -in cert.pem -noout -enddate

# Verify certificate and key match
openssl x509 -noout -modulus -in cert.pem | openssl md5
openssl rsa -noout -modulus -in key.pem | openssl md5
# MD5 hashes should match
```

### Convert Formats (if needed)

```bash
# PFX/P12 to PEM
openssl pkcs12 -in cert.pfx -clcerts -nokeys -out cert.pem
openssl pkcs12 -in cert.pfx -nocerts -nodes -out key.pem

# DER to PEM
openssl x509 -inform DER -in cert.der -out cert.pem
openssl rsa -inform DER -in key.der -out key.pem

# Remove passphrase from key
openssl rsa -in key.pem -out key-nopass.pem
```

### Create Certificate Bundle (with intermediates)

```bash
# Combine certificate with intermediate certificates
cat cert.pem intermediate.pem root.pem > fullchain.pem
```

## Deploy with Custom Certificate

### Quick Deploy

```bash
cd infrastructure/

# Deploy with your certificate
./setup-load-balancer.sh \
  lunari-microservices \
  us-central1 \
  usuario-service \
  user.mydomain.com \
  /path/to/cert.pem \
  /path/to/key.pem
```

### Manual Upload

```bash
# Upload certificate to GCP
gcloud compute ssl-certificates create usuario-cert \
  --certificate=/path/to/cert.pem \
  --private-key=/path/to/key.pem \
  --global

# Verify
gcloud compute ssl-certificates describe usuario-cert --global
```

## Certificate Management

### View Certificate

```bash
# List all certificates
gcloud compute ssl-certificates list

# View details
gcloud compute ssl-certificates describe usuario-cert --global

# Check expiration
gcloud compute ssl-certificates describe usuario-cert \
  --global \
  --format="value(expireTime)"
```

### Renew/Update Certificate

```bash
# Upload new certificate
gcloud compute ssl-certificates create usuario-cert-new \
  --certificate=new-cert.pem \
  --private-key=new-key.pem \
  --global

# Update HTTPS proxy
gcloud compute target-https-proxies update usuario-https-proxy \
  --ssl-certificates=usuario-cert-new \
  --global

# Test
curl -v https://user.mydomain.com 2>&1 | grep "expire date"

# Delete old certificate
gcloud compute ssl-certificates delete usuario-cert --global
```

## Get Free SSL Certificates

### Let's Encrypt

```bash
# Install certbot
sudo apt-get install certbot

# Get certificate (DNS challenge)
sudo certbot certonly --manual --preferred-challenges dns \
  -d user.mydomain.com

# Certificates will be at:
# /etc/letsencrypt/live/user.mydomain.com/fullchain.pem
# /etc/letsencrypt/live/user.mydomain.com/privkey.pem

# Copy and use
sudo cp /etc/letsencrypt/live/user.mydomain.com/fullchain.pem ~/cert.pem
sudo cp /etc/letsencrypt/live/user.mydomain.com/privkey.pem ~/key.pem
sudo chown $USER:$USER ~/cert.pem ~/key.pem
chmod 600 ~/key.pem

# Deploy
./setup-load-balancer.sh \
  lunari-microservices us-central1 usuario-service user.mydomain.com \
  ~/cert.pem ~/key.pem
```

### ZeroSSL

```bash
# Get free certificate from: https://zerossl.com
# Download certificate files (PEM format)
# Use with setup script
```

## Troubleshooting

### Certificate and Key Don't Match

```bash
# Compare modulus
CERT_MOD=$(openssl x509 -noout -modulus -in cert.pem | openssl md5)
KEY_MOD=$(openssl rsa -noout -modulus -in key.pem | openssl md5)
echo "Cert: $CERT_MOD"
echo "Key: $KEY_MOD"
# Should match!
```

### Certificate Upload Fails

```bash
# Check PEM format
head -1 cert.pem  # Should show: -----BEGIN CERTIFICATE-----
head -1 key.pem   # Should show: -----BEGIN PRIVATE KEY-----

# Check key is not encrypted
grep "ENCRYPTED" key.pem
# If you see "ENCRYPTED", remove passphrase:
openssl rsa -in key.pem -out key-nopass.pem
```

### Domain Mismatch

```bash
# Check domains in certificate
openssl x509 -in cert.pem -noout -text | grep -A1 "Subject Alternative Name"

# Should include your domain:
# DNS:user.mydomain.com
# Or wildcard:
# DNS:*.mydomain.com
```

### Test SSL Certificate

```bash
# Test SSL connection
openssl s_client -connect user.mydomain.com:443 -servername user.mydomain.com

# Check certificate served
curl -vI https://user.mydomain.com 2>&1 | grep "expire date"

# Verify certificate details
echo | openssl s_client -connect user.mydomain.com:443 2>/dev/null | openssl x509 -noout -text
```

## Security Best Practices

```bash
# Set proper permissions
chmod 600 key.pem
chmod 644 cert.pem

# Never commit to git
echo "*.pem" >> .gitignore
echo "*.key" >> .gitignore
echo "*.crt" >> .gitignore

# Delete local copies after upload (optional but secure)
shred -vfz -n 10 key.pem
shred -vfz -n 10 cert.pem
```

## Comparison: Custom vs Google-Managed

| Feature | Custom | Google-Managed |
|---------|--------|----------------|
| **Setup Time** | Immediate | 10-60 minutes |
| **Cost** | CA fees | FREE |
| **Renewal** | Manual | Automatic |
| **Validation** | DV/OV/EV | DV only |
| **Wildcard** | Yes | Yes |
| **Management** | Manual | Zero |

## Certificate Providers

### Free Options
- **Let's Encrypt**: Free, 90-day validity, automatic with certbot
- **ZeroSSL**: Free, 90-day validity, manual process

### Paid Options
- **Sectigo**: $10-50/year
- **DigiCert**: $200-1000+/year (EV available)
- **GoDaddy**: $70-300/year
- **GlobalSign**: $250-800/year

## Common Certificate Files

```
certificate.crt     → Your domain certificate
certificate.pem     → Same as .crt (PEM format)
fullchain.pem       → Certificate + intermediate + root chain
privkey.pem         → Private key
private.key         → Private key
chain.pem           → Intermediate certificates only
bundle.crt          → Certificate bundle (similar to fullchain)
```

**What to use:**
- Certificate: `fullchain.pem` (preferred) or `certificate.pem`
- Private Key: `privkey.pem` or `private.key`

## Complete Example

```bash
# 1. Get Let's Encrypt certificate
sudo certbot certonly --manual --preferred-challenges dns -d user.mydomain.com

# 2. Copy certificates
sudo cp /etc/letsencrypt/live/user.mydomain.com/fullchain.pem ~/cert.pem
sudo cp /etc/letsencrypt/live/user.mydomain.com/privkey.pem ~/key.pem
sudo chown $USER:$USER ~/*.pem
chmod 600 ~/key.pem

# 3. Verify certificate
openssl x509 -in ~/cert.pem -noout -text | grep "DNS:"
openssl x509 -in ~/cert.pem -noout -enddate

# 4. Verify certificate and key match
CERT=$(openssl x509 -noout -modulus -in ~/cert.pem | openssl md5)
KEY=$(openssl rsa -noout -modulus -in ~/key.pem | openssl md5)
[ "$CERT" = "$KEY" ] && echo "✓ Match" || echo "✗ No match"

# 5. Deploy with custom certificate
cd infrastructure/
./setup-load-balancer.sh \
  lunari-microservices us-central1 usuario-service user.mydomain.com \
  ~/cert.pem ~/key.pem

# 6. Test
curl https://user.mydomain.com/actuator/health

# 7. Set renewal reminder (Let's Encrypt expires in 90 days)
echo "Renew certificate before: $(date -d '+80 days' +%Y-%m-%d)"
```

## Additional Resources

- **Full Guide**: [CUSTOM_SSL_CERTIFICATE.md](./CUSTOM_SSL_CERTIFICATE.md)
- **Load Balancer Setup**: [LOAD_BALANCER_SETUP.md](./LOAD_BALANCER_SETUP.md)
- **Quick Start**: [DOMAIN_SETUP_QUICK_START.md](./DOMAIN_SETUP_QUICK_START.md)
- **Script**: `setup-load-balancer.sh`

## Support

- **OpenSSL Docs**: https://www.openssl.org/docs/
- **Let's Encrypt**: https://letsencrypt.org/docs/
- **GCP SSL Certs**: https://cloud.google.com/load-balancing/docs/ssl-certificates
