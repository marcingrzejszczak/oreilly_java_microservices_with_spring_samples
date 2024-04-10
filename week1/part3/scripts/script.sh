#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

echo "Add entries to vault"
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='vault-plaintext-root-token'

echo "Adding a property for all applications (same as application.yml)"
vault kv put secret/application only.in.vault="Hello"

echo "Adding a property for app bar with profile foo"
vault kv put secret/bar,foo a="bar with foo profile value from Vault"

echo "Reading the properties back"
vault kv get secret/application
vault kv get secret/bar,foo
