#!/bin/bash

set -o errexit

echo "Sets up IPs for datasources"
./scripts/replace_ip.sh
