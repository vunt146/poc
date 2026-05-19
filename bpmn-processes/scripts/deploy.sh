#!/bin/bash
# Deploy BPMN processes to Camunda Zeebe
# Usage: ./deploy.sh [zeebe-gateway-address]

GATEWAY=${1:-"localhost:26500"}

echo "Deploying BPMN processes to Zeebe at $GATEWAY..."

# Deploy lead lifecycle process
zbctl deploy resource ../processes/lead-lifecycle.bpmn --address "$GATEWAY" --insecure

echo "Deployment complete!"
echo ""
echo "Deployed processes:"
echo "  - lead-lifecycle (Lead Lifecycle Process)"
