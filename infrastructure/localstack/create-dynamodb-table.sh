#!/usr/bin/env bash
set -euo pipefail

AWS_ENDPOINT="${AWS_ENDPOINT:-http://localhost:4566}"
AWS_REGION="${AWS_REGION:-us-east-1}"
TABLE_NAME="${STRATEGY_TABLE_NAME:-CreditRecoveryStrategy}"

if aws --endpoint-url="${AWS_ENDPOINT}" dynamodb describe-table --table-name "${TABLE_NAME}" --region "${AWS_REGION}" >/dev/null 2>&1; then
  echo "DynamoDB table ${TABLE_NAME} already exists."
  exit 0
fi

aws --endpoint-url="${AWS_ENDPOINT}" dynamodb create-table \
  --table-name "${TABLE_NAME}" \
  --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S \
  --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region "${AWS_REGION}"

aws --endpoint-url="${AWS_ENDPOINT}" dynamodb wait table-exists \
  --table-name "${TABLE_NAME}" \
  --region "${AWS_REGION}"

echo "DynamoDB table ${TABLE_NAME} created."
