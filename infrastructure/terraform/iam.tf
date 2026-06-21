data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_execution" {
  name               = "${local.name_prefix}-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json
  tags               = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ecs_execution" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "api_task" {
  name               = "${local.name_prefix}-api-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json
  tags               = local.common_tags
}

resource "aws_iam_role" "processor_task" {
  name               = "${local.name_prefix}-processor-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json
  tags               = local.common_tags
}

data "aws_iam_policy_document" "api_permissions" {
  statement {
    actions = [
      "dynamodb:DescribeTable",
      "dynamodb:GetItem",
      "dynamodb:Query"
    ]
    resources = [aws_dynamodb_table.credit_recovery_strategy.arn]
  }

  dynamic "statement" {
    for_each = var.kms_key_arn == null ? [] : [var.kms_key_arn]
    content {
      actions = [
        "kms:Decrypt",
        "kms:DescribeKey",
        "kms:GenerateDataKey"
      ]
      resources = [statement.value]

      condition {
        test     = "StringEquals"
        variable = "kms:ViaService"
        values   = ["dynamodb.${var.aws_region}.amazonaws.com"]
      }
    }
  }
}

resource "aws_iam_role_policy" "api_permissions" {
  name   = "${local.name_prefix}-api-permissions"
  role   = aws_iam_role.api_task.id
  policy = data.aws_iam_policy_document.api_permissions.json
}

data "aws_iam_policy_document" "ecs_exec_permissions" {
  statement {
    actions = [
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "api_ecs_exec_permissions" {
  name   = "${local.name_prefix}-api-ecs-exec"
  role   = aws_iam_role.api_task.id
  policy = data.aws_iam_policy_document.ecs_exec_permissions.json
}

data "aws_iam_policy_document" "processor_permissions" {
  statement {
    actions = [
      "dynamodb:DescribeTable",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:TransactWriteItems"
    ]
    resources = [aws_dynamodb_table.credit_recovery_strategy.arn]
  }

  statement {
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:ChangeMessageVisibility",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl"
    ]
    resources = [aws_sqs_queue.credit_profile_received.arn]
  }

  dynamic "statement" {
    for_each = var.kms_key_arn == null ? [] : [var.kms_key_arn]
    content {
      actions = [
        "kms:Decrypt",
        "kms:DescribeKey",
        "kms:GenerateDataKey"
      ]
      resources = [statement.value]

      condition {
        test     = "StringEquals"
        variable = "kms:ViaService"
        values = [
          "dynamodb.${var.aws_region}.amazonaws.com",
          "sqs.${var.aws_region}.amazonaws.com"
        ]
      }
    }
  }
}

resource "aws_iam_role_policy" "processor_permissions" {
  name   = "${local.name_prefix}-processor-permissions"
  role   = aws_iam_role.processor_task.id
  policy = data.aws_iam_policy_document.processor_permissions.json
}

resource "aws_iam_role_policy" "processor_ecs_exec_permissions" {
  name   = "${local.name_prefix}-processor-ecs-exec"
  role   = aws_iam_role.processor_task.id
  policy = data.aws_iam_policy_document.ecs_exec_permissions.json
}
