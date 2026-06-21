#!/usr/bin/env bash
set -euo pipefail

AWS_ENDPOINT="${AWS_ENDPOINT:-http://localhost:4566}"
AWS_REGION="${AWS_REGION:-us-east-1}"
MAIN_QUEUE_NAME="${MAIN_QUEUE_NAME:-credit-profile-received}"
DLQ_NAME="${DLQ_NAME:-credit-profile-received-dlq}"

aws --endpoint-url="${AWS_ENDPOINT}" sqs create-queue \
  --queue-name "${DLQ_NAME}" \
  --attributes MessageRetentionPeriod=1209600 \
  --region "${AWS_REGION}" >/dev/null

DLQ_URL="$(aws --endpoint-url="${AWS_ENDPOINT}" sqs get-queue-url \
  --queue-name "${DLQ_NAME}" \
  --region "${AWS_REGION}" \
  --query QueueUrl \
  --output text)"

DLQ_ARN="$(aws --endpoint-url="${AWS_ENDPOINT}" sqs get-queue-attributes \
  --queue-url "${DLQ_URL}" \
  --attribute-names QueueArn \
  --region "${AWS_REGION}" \
  --query Attributes.QueueArn \
  --output text)"

REDRIVE_POLICY="{\"deadLetterTargetArn\":\"${DLQ_ARN}\",\"maxReceiveCount\":\"3\"}"

aws --endpoint-url="${AWS_ENDPOINT}" sqs create-queue \
  --queue-name "${MAIN_QUEUE_NAME}" \
  --attributes VisibilityTimeout=30,ReceiveMessageWaitTimeSeconds=10,RedrivePolicy="${REDRIVE_POLICY}" \
  --region "${AWS_REGION}" >/dev/null

MAIN_QUEUE_URL="$(aws --endpoint-url="${AWS_ENDPOINT}" sqs get-queue-url \
  --queue-name "${MAIN_QUEUE_NAME}" \
  --region "${AWS_REGION}" \
  --query QueueUrl \
  --output text)"

echo "Main queue: ${MAIN_QUEUE_URL}"
echo "DLQ: ${DLQ_URL}"
