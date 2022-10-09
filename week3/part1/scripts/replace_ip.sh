#!/bin/bash

set -o errexit

root="$( pwd )"
echo "Root dir [${root}]"

my_ip="$( "${root}"/scripts/whats_my_ip.sh )"

echo "Replacing files with your ip [${my_ip}]"

rm -rf "${root}"/docker/
mkdir -p "${root}"/docker/grafana/provisioning/datasources/
cp "${root}"/docker_backup/grafana/provisioning/datasources/datasource.yml "${root}"/docker/grafana/provisioning/datasources/datasource.yml
sed -i -e "s/host.docker.internal/$my_ip/g" "${root}"/docker/grafana/provisioning/datasources/datasource.yml
mkdir -p "${root}"/docker/prometheus/
cp "${root}"/docker_backup/prometheus/prometheus.yml "${root}"/docker/prometheus/prometheus.yml
sed -i -e "s/host.docker.internal/$my_ip/g" "${root}"/docker/prometheus/prometheus.yml
