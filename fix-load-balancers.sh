#!/usr/bin/env bash
# Fix load balancers by recreating NEGs in the correct region

set -e

PROJECT_ID="lunari-prod-1765210460"
CORRECT_REGION="southamerica-west1"
OLD_REGION="us-central1"

echo "========================================="
echo "  Fixing Load Balancer Configuration"
echo "========================================="
echo ""
echo "Issue: NEGs in wrong region"
echo "  Services are in: $CORRECT_REGION"
echo "  NEGs are in: $OLD_REGION"
echo ""

# Set project
gcloud config set project $PROJECT_ID

echo "Step 1: Remove backends from backend services..."
gcloud compute backend-services remove-backend carrito-backend \
  --global \
  --network-endpoint-group=carrito-neg \
  --network-endpoint-group-region=$OLD_REGION

gcloud compute backend-services remove-backend inventario-backend \
  --global \
  --network-endpoint-group=inventario-neg \
  --network-endpoint-group-region=$OLD_REGION

echo "Step 2: Delete old NEGs..."
gcloud compute network-endpoint-groups delete carrito-neg \
  --region=$OLD_REGION \
  --quiet

gcloud compute network-endpoint-groups delete inventario-neg \
  --region=$OLD_REGION \
  --quiet

echo "Step 3: Create new NEGs in correct region..."
gcloud compute network-endpoint-groups create carrito-neg \
  --region=$CORRECT_REGION \
  --network-endpoint-type=serverless \
  --cloud-run-service=carrito-service

gcloud compute network-endpoint-groups create inventario-neg \
  --region=$CORRECT_REGION \
  --network-endpoint-type=serverless \
  --cloud-run-service=inventario-service

echo "Step 4: Add new NEGs to backend services..."
gcloud compute backend-services add-backend carrito-backend \
  --global \
  --network-endpoint-group=carrito-neg \
  --network-endpoint-group-region=$CORRECT_REGION

gcloud compute backend-services add-backend inventario-backend \
  --global \
  --network-endpoint-group=inventario-neg \
  --network-endpoint-group-region=$CORRECT_REGION

echo ""
echo "========================================="
echo "  Fix Complete!"
echo "========================================="
echo ""
echo "Verifying NEG status..."
gcloud compute network-endpoint-groups list --filter="name:(carrito-neg OR inventario-neg)"
echo ""
echo "Wait 2-3 minutes for changes to propagate, then test:"
echo "  curl https://cart.aframuz.dev/actuator/health"
echo "  curl https://inventory.aframuz.dev/actuator/health"
echo ""
