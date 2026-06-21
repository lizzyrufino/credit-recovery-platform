terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  name_prefix            = "credit-recovery-${var.environment}"
  create_ecs             = var.create_ecs_services && var.vpc_id != null && length(var.private_subnet_ids) > 0
  create_alb             = local.create_ecs && var.create_api_alb && var.vpc_id != null && length(var.public_subnet_ids) > 0
  ecs_security_group_ids = length(var.security_group_ids) > 0 ? var.security_group_ids : (local.create_ecs && var.vpc_id != null ? [aws_security_group.ecs_tasks[0].id] : [])
  common_tags = {
    Application = "credit-recovery-platform"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

resource "terraform_data" "input_validation" {
  input = local.name_prefix

  lifecycle {
    precondition {
      condition     = !var.create_ecs_services || (var.vpc_id != null && length(var.private_subnet_ids) > 0)
      error_message = "create_ecs_services=true requires vpc_id and at least one private_subnet_ids value."
    }

    precondition {
      condition     = !var.create_api_alb || (var.create_ecs_services && var.vpc_id != null && length(var.public_subnet_ids) > 0)
      error_message = "create_api_alb=true requires create_ecs_services=true, vpc_id and at least one public_subnet_ids value."
    }
  }
}
