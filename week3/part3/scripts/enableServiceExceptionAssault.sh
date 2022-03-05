#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

port="${1}"
curl -X POST -H "Content-Type: application/json" -d @enableServiceWatcher.json "http://localhost:${port}/actuator/chaosmonkey/watchers"
curl -X POST -H "Content-Type: application/json" -d @enableExceptionAssault.json "http://localhost:${port}/actuator/chaosmonkey/assaults"
