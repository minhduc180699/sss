#!/bin/bash

echo "🔍 Testing Keycloak Client Configuration"
echo "======================================="

# Test 1: Check if Keycloak is accessible
echo "1. Testing Keycloak accessibility..."
curl -s http://localhost:8080/realms/sss-realm/.well-known/openid_configuration | head -10

# Test 2: Get authorization URL
echo -e "\n2. Authorization URL:"
echo "http://localhost:8080/realms/sss-realm/protocol/openid-connect/auth?client_id=sss-frontend&redirect_uri=http://localhost:3000/auth/callback&state=test&response_type=code&scope=openid"

# Test 3: Check JWK endpoint
echo -e "\n3. Testing JWK endpoint..."
curl -s http://localhost:8080/realms/sss-realm/protocol/openid-connect/certs | head -5

# Test 4: Check backend JWT test endpoint
echo -e "\n4. Testing backend JWT endpoint..."
curl -s http://localhost:8082/api/auth/public/test-jwt

echo -e "\n✅ Test completed!"
echo -e "\n📋 Next steps:"
echo "1. Open browser: http://localhost:8080/admin"
echo "2. Check client 'sss-frontend' settings"
echo "3. Ensure 'Client authentication' is OFF"
echo "4. Clear browser cache and test again"




