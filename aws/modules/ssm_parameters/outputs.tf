output "path" {
  description = "Parameter path holding the application environment."
  value       = var.path
}

output "path_arns" {
  description = "ARNs covering the path itself (GetParametersByPath) and every parameter under it."
  value = [
    "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter${var.path}",
    "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter${var.path}/*",
  ]
}

output "parameter_names" {
  description = "Names of every parameter created (values are not exposed)."
  value       = concat([for p in aws_ssm_parameter.plain : p.name], [for p in aws_ssm_parameter.secure : p.name])
}
