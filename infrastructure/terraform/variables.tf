variable "aws_region" {
  type        = string
  description = "AWS region."
  default     = "us-east-1"
}

variable "environment" {
  type        = string
  description = "Deployment environment."
  default     = "dev"
}

variable "strategy_table_name" {
  type        = string
  description = "DynamoDB table name for materialized recovery strategies."
  default     = "CreditRecoveryStrategy"
}

variable "kms_key_arn" {
  type        = string
  description = "Optional customer managed KMS key ARN for DynamoDB, SQS and CloudWatch Logs."
  default     = null
}

variable "api_image" {
  type        = string
  description = "Container image for credit-strategy-api."
  default     = "replace-me-api-image"
}

variable "processor_image" {
  type        = string
  description = "Container image for credit-strategy-processor."
  default     = "replace-me-processor-image"
}

variable "create_ecs_services" {
  type        = bool
  description = "Whether to create ECS Fargate services. Requires vpc_id and private_subnet_ids."
  default     = false
}

variable "create_api_alb" {
  type        = bool
  description = "Whether to create an internet-facing ALB for the API. Requires vpc_id and public_subnet_ids."
  default     = false
}

variable "vpc_id" {
  type        = string
  description = "VPC id used by ECS services, security groups and optional ALB."
  default     = null
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet ids for the optional API ALB."
  default     = []
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnet ids for ECS services."
  default     = []
}

variable "security_group_ids" {
  type        = list(string)
  description = "Security groups for ECS services."
  default     = []
}

variable "api_desired_count" {
  type        = number
  description = "Desired task count for the API service."
  default     = 2
}

variable "processor_desired_count" {
  type        = number
  description = "Desired task count for the processor service."
  default     = 2
}

variable "log_retention_days" {
  type        = number
  description = "CloudWatch Logs retention in days."
  default     = 30
}
