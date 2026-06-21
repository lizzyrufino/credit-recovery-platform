#!/usr/bin/env bash
set -euo pipefail

AWS_ENDPOINT="${AWS_ENDPOINT:-http://localhost:4566}"
AWS_REGION="${AWS_REGION:-us-east-1}"
MAIN_QUEUE_NAME="${MAIN_QUEUE_NAME:-credit-profile-received}"

QUEUE_URL="$(aws --endpoint-url="${AWS_ENDPOINT}" sqs get-queue-url \
  --queue-name "${MAIN_QUEUE_NAME}" \
  --region "${AWS_REGION}" \
  --query QueueUrl \
  --output text)"

MESSAGE_BODY="$(cat <<'JSON'
{
  "eventId": "evt-credit-profile-001",
  "correlationId": "corr-local-001",
  "occurredAt": "2026-06-20T10:00:00Z",
  "profile": {
    "document": {
      "value": "11222333000181"
    },
    "daysOverdue": 87,
    "debtAmount": 125000.50,
    "products": [
      {
        "type": "CREDIT_CARD_PJ",
        "active": true,
        "outstandingAmount": 85000.00
      },
      {
        "type": "WORKING_CAPITAL",
        "active": true,
        "outstandingAmount": 40500.50
      }
    ],
    "internalScore": 812,
    "paymentHistory": {
      "paidInstallments": 12,
      "delayedInstallments": 3,
      "debtRegularized": false
    },
    "preferredChannel": "WHATSAPP",
    "whatsappConsent": true,
    "riskLevel": "HIGH",
    "activePjCard": true
  }
}
JSON
)"

aws --endpoint-url="${AWS_ENDPOINT}" sqs send-message \
  --queue-url "${QUEUE_URL}" \
  --message-body "${MESSAGE_BODY}" \
  --region "${AWS_REGION}"

echo "Seed message published to ${QUEUE_URL}."
