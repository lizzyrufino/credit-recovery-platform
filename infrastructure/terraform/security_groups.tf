resource "aws_security_group" "alb" {
  count       = local.create_alb ? 1 : 0
  name        = "${local.name_prefix}-alb"
  description = "Ingress for credit recovery API ALB"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.common_tags
}

resource "aws_security_group" "ecs_tasks" {
  count       = local.create_ecs && var.vpc_id != null && length(var.security_group_ids) == 0 ? 1 : 0
  name        = "${local.name_prefix}-ecs-tasks"
  description = "ECS task security group for credit recovery services"
  vpc_id      = var.vpc_id

  dynamic "ingress" {
    for_each = local.create_alb ? [1] : []
    content {
      description     = "API from ALB"
      from_port       = 8080
      to_port         = 8080
      protocol        = "tcp"
      security_groups = [aws_security_group.alb[0].id]
    }
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.common_tags
}
