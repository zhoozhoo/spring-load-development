#!/bin/bash

# Load Test Data Script
# This script authenticates to the Spring Load Development API and inserts test data
# equivalent to the data.sql file using REST API calls

set -e  # Exit on any error

# Configuration file path
HTTP_CLIENT_ENV_FILE="http-client.env.json"
# Will be derived from http-client.env.json (local.api_gateway_host)
API_BASE=""

# Function to load configuration from http-client.env.json
load_config() {
    print_info "Loading configuration from ${HTTP_CLIENT_ENV_FILE}..."
    
    if [ ! -f "$HTTP_CLIENT_ENV_FILE" ]; then
        print_error "Configuration file not found: ${HTTP_CLIENT_ENV_FILE}"
        print_info "Please ensure you're running this script from the spring-load-development directory"
        exit 1
    fi
    
    # Extract configuration values from the local environment in the JSON file
    AUTHORIZATION_HOST=$(jq -r '.local.authorization_host' "$HTTP_CLIENT_ENV_FILE")
    REALM=$(jq -r '.local.realm' "$HTTP_CLIENT_ENV_FILE")
    CLIENT_ID=$(jq -r '.local.client_id' "$HTTP_CLIENT_ENV_FILE")
    CLIENT_SECRET=$(jq -r '.local.client_secret' "$HTTP_CLIENT_ENV_FILE")
    USERNAME=$(jq -r '.local.username' "$HTTP_CLIENT_ENV_FILE")
    PASSWORD=$(jq -r '.local.password' "$HTTP_CLIENT_ENV_FILE")
    API_GATEWAY_HOST=$(jq -r '.local.api_gateway_host' "$HTTP_CLIENT_ENV_FILE")
    
    # Validate that all required values were extracted
     if [ "$AUTHORIZATION_HOST" = "null" ] || [ "$REALM" = "null" ] || [ "$CLIENT_ID" = "null" ] || \
         [ "$CLIENT_SECRET" = "null" ] || [ "$USERNAME" = "null" ] || [ "$PASSWORD" = "null" ] || \
         [ "$API_GATEWAY_HOST" = "null" ]; then
        print_error "Failed to extract configuration from ${HTTP_CLIENT_ENV_FILE}"
        print_info "Please ensure the file contains valid JSON with local environment configuration"
        exit 1
    fi
    
     # Derive API base from API gateway host
     API_BASE="${API_GATEWAY_HOST%/}/api"
    
    print_success "Configuration loaded successfully"
    print_info "Authorization Host: ${AUTHORIZATION_HOST}"
    print_info "Realm: ${REALM}"
    print_info "Client ID: ${CLIENT_ID}"
    print_info "Username: ${USERNAME}"
    print_info "API Gateway Host: ${API_GATEWAY_HOST}"
}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to authenticate and get access token
authenticate() {
    print_info "Authenticating to Keycloak..."
    
    local response=$(curl -s -X POST \
        "${AUTHORIZATION_HOST}/realms/${REALM}/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=${CLIENT_ID}" \
        -d "client_secret=${CLIENT_SECRET}" \
        -d "username=${USERNAME}" \
        -d "password=${PASSWORD}" \
        -d "scope=openid")
    
    if [ $? -ne 0 ]; then
        print_error "Failed to authenticate"
        exit 1
    fi
    
    # Extract access token using jq
    ACCESS_TOKEN=$(echo "$response" | jq -r '.access_token')
    
    if [ "$ACCESS_TOKEN" = "null" ] || [ -z "$ACCESS_TOKEN" ]; then
        print_error "Failed to extract access token"
        echo "Response: $response"
        exit 1
    fi
    
    print_success "Authentication successful"
}

# Function to create rifle
create_rifle() {
    print_info "Creating rifle: Tikka T3x CTR"
    
    local rifle_payload='{
        "name": "Tikka T3x CTR",
        "description": "6.5mm Creedmoor Tikka T3x CTR",
        "caliber": "6.5 Creedmoor"
    }'
    
    local response=$(curl -s -X POST \
        "${API_BASE}/rifles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "$rifle_payload")
    
    if [ $? -ne 0 ]; then
        print_error "Failed to create rifle"
        exit 1
    fi
    
    RIFLE_ID=$(echo "$response" | jq -r '.id')
    
    if [ "$RIFLE_ID" = "null" ] || [ -z "$RIFLE_ID" ]; then
        print_error "Failed to extract rifle ID"
        echo "Response: $response"
        exit 1
    fi
    
    print_success "Rifle created with ID: $RIFLE_ID"
}

# Function to create load
create_load() {
    print_info "Creating load: H4350 Load Development"
    
    local load_payload='{
        "name": "H4350 Load Development",
        "description": "Initial load development",
        "powderManufacturer": "Hodgdon",
        "powderType": "H4350",
        "bulletManufacturer": "Berger",
        "bulletType": "Hybrid Target",
        "bulletWeight": { "value": 140, "unit": "[gr]", "scale": "ABSOLUTE" },
        "primerManufacturer": "CCI",
        "primerType": "BR2",
        "distanceFromLands": { "value": 0.020, "unit": "[in_i]", "scale": "ABSOLUTE" },
        "caseOverallLength": { "value": 2.303, "unit": "[in_i]", "scale": "ABSOLUTE" },
        "neckTension": { "value": 0.003, "unit": "[in_i]", "scale": "ABSOLUTE" },
        "rifleId": "'$RIFLE_ID'"
    }'
    
    local response=$(curl -s -X POST \
        "${API_BASE}/loads" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "$load_payload")
    
    if [ $? -ne 0 ]; then
        print_error "Failed to create load"
        exit 1
    fi
    
    LOAD_ID=$(echo "$response" | jq -r '.id')
    
    if [ "$LOAD_ID" = "null" ] || [ -z "$LOAD_ID" ]; then
        print_error "Failed to extract load ID"
        echo "Response: $response"
        exit 1
    fi
    
    print_success "Load created with ID: $LOAD_ID"
}

# Function to create a group and its shots
create_group_with_shots() {
    local powder_charge=$1
    local group_size=$2
    shift 2
    local velocities=("$@")
    
    print_info "Creating group with powder charge: ${powder_charge}gr"
    
    # Get current date in YYYY-MM-DD format
    local current_date=$(date +%Y-%m-%d)
    
    local group_payload='{
        "date": "'$current_date'",
        "loadId": "'$LOAD_ID'",
        "powderCharge": { "value": '$powder_charge', "unit": "[gr]", "scale": "ABSOLUTE" },
        "targetRange": { "value": 100, "unit": "[yd_i]", "scale": "ABSOLUTE" },
        "groupSize": { "value": '$group_size', "unit": "[in_i]", "scale": "ABSOLUTE" }
    }'
    
    local response=$(curl -s -X POST \
        "${API_BASE}/groups" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "$group_payload")
    
    if [ $? -ne 0 ]; then
        print_error "Failed to create group for powder charge ${powder_charge}gr"
        return 1
    fi
    
    local group_id=$(echo "$response" | jq -r '.id')
    
    if [ "$group_id" = "null" ] || [ -z "$group_id" ]; then
        print_error "Failed to extract group ID for powder charge ${powder_charge}gr"
        echo "Response: $response"
        return 1
    fi
    
    print_success "Group created with ID: $group_id"
    
    # Create shots for this group
    for velocity in "${velocities[@]}"; do
        print_info "Creating shot with velocity: ${velocity} fps"
        
        local shot_payload='{
            "groupId": "'$group_id'",
            "velocity": { "value": '$velocity', "unit": "[ft_i]/s", "scale": "ABSOLUTE" }
        }'
        
        local shot_response=$(curl -s -X POST \
            "${API_BASE}/shots" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -d "$shot_payload")
        
        if [ $? -ne 0 ]; then
            print_error "Failed to create shot with velocity ${velocity} fps"
            continue
        fi
        
        local shot_id=$(echo "$shot_response" | jq -r '.id')
        
        if [ "$shot_id" = "null" ] || [ -z "$shot_id" ]; then
            print_error "Failed to extract shot ID for velocity ${velocity} fps"
            continue
        fi
        
        print_success "Shot created with ID: $shot_id (${velocity} fps)"
    done
}

# Function to create all groups and shots
create_all_groups_and_shots() {
    print_info "Creating all groups and shots..."
    
    # Group data from data.sql
    create_group_with_shots 37.8 0.834 2364 2335 2328
    create_group_with_shots 38.1 0.514 2377 2339 2360
    create_group_with_shots 38.4 0.259 2397 2415 2411
    create_group_with_shots 38.7 0.444 2415 2418
    create_group_with_shots 39.0 1.054 2446 2476 2460
    create_group_with_shots 39.3 0.628 2466 2460 2458
    create_group_with_shots 39.6 0.173 2484 2465 2462
    create_group_with_shots 39.9 0.630 2490 2485 2492
    create_group_with_shots 40.2 0.785 2528 2513 2499
    create_group_with_shots 40.5 0.738 2507 2501 2548
    create_group_with_shots 40.8 0.819 2569 2559 2545
    create_group_with_shots 41.1 0.895 2556 2574
    
    print_success "All groups and shots created successfully"
}

# Function to check if required tools are available
check_dependencies() {
    print_info "Checking dependencies..."
    
    if ! command -v curl &> /dev/null; then
        print_error "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is required but not installed. Please install jq to parse JSON responses."
        print_info "On Ubuntu/Debian: sudo apt-get install jq"
        print_info "On RHEL/CentOS: sudo yum install jq"
        print_info "On macOS: brew install jq"
        exit 1
    fi
    
    print_success "All dependencies available"
}

# Function to verify services are running
verify_services() {
    print_info "Verifying services are running..."
    
    # Check Keycloak
    if ! curl -s --connect-timeout 5 "${AUTHORIZATION_HOST}/realms/${REALM}/.well-known/openid-configuration" > /dev/null; then
        print_error "Keycloak server is not accessible at ${AUTHORIZATION_HOST}"
        print_info "Please ensure Keycloak is running on port 7080"
        exit 1
    fi
    
    # Check API Gateway
    if ! curl -s --connect-timeout 5 "${API_BASE%/api}/actuator/health" > /dev/null; then
        print_error "API Gateway is not accessible at ${API_BASE%/api}"
        print_info "Please ensure the API Gateway is running on port 8080"
        exit 1
    fi
    
    print_success "All services are accessible"
}

# Main execution
main() {
    echo "=========================================="
    echo "Spring Load Development - Test Data Loader"
    echo "=========================================="
    echo
    
    check_dependencies
    load_config
    verify_services
    authenticate
    create_rifle
    create_load
    create_all_groups_and_shots
    
    echo
    print_success "Test data loading completed successfully!"
    echo
    echo "Summary:"
    echo "- Rifle ID: $RIFLE_ID"
    echo "- Load ID: $LOAD_ID"
    echo "- Created 12 groups with associated shots"
    echo
    echo "You can now test the API endpoints using the provided .http files"
    echo "Update the rifle_id and load_id variables in the .http files with:"
    echo "  @rifle_id = $RIFLE_ID"
    echo "  @load_id = $LOAD_ID"
}

# Run main function
main "$@"
