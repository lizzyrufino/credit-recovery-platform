output "dynamodb_table_name" {
  value = aws_dynamodb_table.credit_recovery_strategy.name
}

output "main_queue_url" {
  value = aws_sqs_queue.credit_profile_received.url
}

output "dlq_url" {
  value = aws_sqs_queue.credit_profile_received_dlq.url
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "api_alb_dns_name" {
  value       = local.create_alb ? aws_lb.api[0].dns_name : null
  description = "DNS name for the optional API ALB."
}
