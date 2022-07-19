#!/bin/bash

set -o errexit

root="$( pwd )"
echo "Root dir [${root}]"

my_ip="$( "${root}"/scripts/whats_my_ip.sh )"

echo "Replacing files with your ip [${my_ip}]"

rm -rf "${root}"/target/docker/
mkdir -p "${root}"/target/docker/grafana/provisioning/datasources/
cp "${root}"/docker/grafana/provisioning/datasources/datasource.yml "${root}"/target/docker/grafana/provisioning/datasources/datasource.yml
sed -i -e "s/host.docker.internal/$my_ip/g" "${root}"/target/docker/grafana/provisioning/datasources/datasource.yml
mkdir -p "${root}"/target/docker/prometheus/
cp "${root}"/docker/prometheus/prometheus.yml "${root}"/target/docker/prometheus/prometheus.yml
sed -i -e "s/host.docker.internal/$my_ip/g" "${root}"/target/docker/prometheus/prometheus.yml
