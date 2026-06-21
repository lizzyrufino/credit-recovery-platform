#!/usr/bin/env bash
set -euo pipefail

AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_ENDPOINT="${AWS_ENDPOINT:-http://localstack:4566}"
TABLE_NAME="${STRATEGY_TABLE_NAME:-CreditRecoveryStrategy}"
MAIN_QUEUE_NAME="${MAIN_QUEUE_NAME:-credit-profile-received}"
DLQ_NAME="${DLQ_NAME:-credit-profile-received-dlq}"

export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-test}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-test}"
export AWS_DEFAULT_REGION="${AWS_REGION}"

AWS_CMD=(awslocal --endpoint-url="${AWS_ENDPOINT}" --region "${AWS_REGION}")

until curl -fsS "${AWS_ENDPOINT}/_localstack/health" >/dev/null; do
  echo "Waiting for LocalStack at ${AWS_ENDPOINT}..."
  sleep 2
done

"${AWS_CMD[@]}" sqs create-queue \
  --queue-name "${DLQ_NAME}" \
  --attributes MessageRetentionPeriod=1209600 \
  >/dev/null

DLQ_URL="$("${AWS_CMD[@]}" sqs get-queue-url \
  --queue-name "${DLQ_NAME}" \
  --query QueueUrl \
  --output text)"

DLQ_ARN="$("${AWS_CMD[@]}" sqs get-queue-attributes \
  --queue-url "${DLQ_URL}" \
  --attribute-names QueueArn \
  --query Attributes.QueueArn \
  --output text)"

REDRIVE_POLICY="$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' "${DLQ_ARN}")"
export REDRIVE_POLICY

QUEUE_ATTRIBUTES="$(python3 - <<'PY'
import json
import os

print(json.dumps({
    "VisibilityTimeout": "30",
    "ReceiveMessageWaitTimeSeconds": "10",
    "RedrivePolicy": os.environ["REDRIVE_POLICY"],
}))
PY
)"

"${AWS_CMD[@]}" sqs create-queue \
  --queue-name "${MAIN_QUEUE_NAME}" \
  --attributes "${QUEUE_ATTRIBUTES}" \
  >/dev/null

if "${AWS_CMD[@]}" dynamodb describe-table --table-name "${TABLE_NAME}" >/dev/null 2>&1; then
  echo "DynamoDB table ${TABLE_NAME} already exists."
else
  "${AWS_CMD[@]}" dynamodb create-table \
    --table-name "${TABLE_NAME}" \
    --attribute-definitions AttributeName=pk,AttributeType=S AttributeName=sk,AttributeType=S \
    --key-schema AttributeName=pk,KeyType=HASH AttributeName=sk,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    >/dev/null

  "${AWS_CMD[@]}" dynamodb wait table-exists --table-name "${TABLE_NAME}"
fi

MAIN_QUEUE_URL="$("${AWS_CMD[@]}" sqs get-queue-url \
  --queue-name "${MAIN_QUEUE_NAME}" \
  --query QueueUrl \
  --output text)"

echo "LocalStack resources are ready."
echo "Main queue: ${MAIN_QUEUE_URL}"
echo "DLQ: ${DLQ_URL}"
