#!/bin/bash

set -o errexit

rm -rf docker
cp -rf docker_backup docker

echo "Sets up IPs for datasources"
./scripts/replace_ip.sh
