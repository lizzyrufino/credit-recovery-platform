#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/create-sqs-queues.sh"
"${SCRIPT_DIR}/create-dynamodb-table.sh"

echo "Local AWS resources are ready."
