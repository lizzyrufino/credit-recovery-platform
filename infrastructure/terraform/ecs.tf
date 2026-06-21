resource "aws_ecs_cluster" "this" {
  name = local.name_prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = local.common_tags
}

resource "aws_ecs_task_definition" "api" {
  family                   = "${local.name_prefix}-api"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.api_task.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = "credit-strategy-api"
      image     = var.api_image
      essential = true
      portMappings = [{ containerPort = 8080, protocol = "tcp" }]
      environment = [
        { name = "AWS_REGION", value = var.aws_region },
        { name = "STRATEGY_TABLE_NAME", value = aws_dynamodb_table.credit_recovery_strategy.name },
        { name = "JWT_ENABLED", value = "true" }
      ]
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.api.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = local.common_tags
}

resource "aws_ecs_task_definition" "processor" {
  family                   = "${local.name_prefix}-processor"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.processor_task.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = "credit-strategy-processor"
      image     = var.processor_image
      essential = true
      portMappings = [{ containerPort = 8081, protocol = "tcp" }]
      environment = [
        { name = "AWS_REGION", value = var.aws_region },
        { name = "STRATEGY_TABLE_NAME", value = aws_dynamodb_table.credit_recovery_strategy.name },
        { name = "CREDIT_PROFILE_QUEUE_URL", value = aws_sqs_queue.credit_profile_received.url }
      ]
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:8081/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.processor.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = local.common_tags
}

resource "aws_ecs_service" "api" {
  count           = local.create_ecs ? 1 : 0
  name            = "${local.name_prefix}-api"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = var.api_desired_count
  launch_type     = "FARGATE"
  enable_execute_command = true

  dynamic "load_balancer" {
    for_each = local.create_alb ? [1] : []
    content {
      target_group_arn = aws_lb_target_group.api[0].arn
      container_name   = "credit-strategy-api"
      container_port   = 8080
    }
  }

  network_configuration {
    subnets         = var.private_subnet_ids
    security_groups = local.ecs_security_group_ids
  }

  depends_on = [aws_lb_listener.api_http]

  tags = local.common_tags
}

resource "aws_ecs_service" "processor" {
  count           = local.create_ecs ? 1 : 0
  name            = "${local.name_prefix}-processor"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.processor.arn
  desired_count   = var.processor_desired_count
  launch_type     = "FARGATE"
  enable_execute_command = true

  network_configuration {
    subnets         = var.private_subnet_ids
    security_groups = local.ecs_security_group_ids
  }

  tags = local.common_tags
}
