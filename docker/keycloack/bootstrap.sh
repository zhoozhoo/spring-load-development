#!/bin/sh

export PATH=$PATH:/opt/keycloak/bin/

kcadm.sh config credentials --server http://localhost:8443 --realm master --user $KC_BOOTSTRAP_ADMIN_USERNAME --password $KC_BOOTSTRAP_ADMIN_PASSWORD
