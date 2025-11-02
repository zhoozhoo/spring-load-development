#!/bin/bash
# Script to create all required namespaces for Spring Load Development application
# This should be run before installing the Helm chart

set -e

echo "Creating namespaces for Spring Load Development..."

# Array of namespaces to create
NAMESPACES=(
  "postgres"
  "keycloak"
  "observability"
  "reloading"
)

# Create each namespace if it doesn't exist
for ns in "${NAMESPACES[@]}"; do
  if kubectl get namespace "$ns" &> /dev/null; then
    echo "✓ Namespace '$ns' already exists"
  else
    kubectl create namespace "$ns"
    echo "✓ Created namespace '$ns'"
  fi
done

echo ""
echo "All required namespaces have been created successfully!"
echo ""
echo "Namespaces created:"
kubectl get namespaces -l name --no-headers 2>/dev/null | grep -E "postgres|keycloak|observability|reloading" || kubectl get namespaces | grep -E "postgres|keycloak|observability|reloading"
