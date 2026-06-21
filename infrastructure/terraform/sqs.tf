resource "aws_sqs_queue" "credit_profile_received_dlq" {
  name                      = "${local.name_prefix}-credit-profile-received-dlq"
  message_retention_seconds = 1209600
  kms_master_key_id         = var.kms_key_arn
  sqs_managed_sse_enabled   = var.kms_key_arn == null

  tags = local.common_tags
}

resource "aws_sqs_queue" "credit_profile_received" {
  name                       = "${local.name_prefix}-credit-profile-received"
  visibility_timeout_seconds = 30
  receive_wait_time_seconds  = 10
  kms_master_key_id          = var.kms_key_arn
  sqs_managed_sse_enabled    = var.kms_key_arn == null

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.credit_profile_received_dlq.arn
    maxReceiveCount     = 3
  })

  tags = local.common_tags
}
