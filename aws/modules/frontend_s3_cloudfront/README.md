# Frontend S3 & CloudFront CDN Module

This module provisions the Presentation Layer for the Materia React SPA:
- **Amazon S3**: Fully private object storage bucket encrypted at rest with AES256 and versioning enabled.
- **Origin Access Control (OAC)**: Securely isolates S3 so assets can **only** be accessed via CloudFront CDN. Direct public S3 access is blocked.
- **CloudFront CDN**: Worldwide edge caching, SSL/TLS termination, HTTP-to-HTTPS redirect, and gzip/brotli compression.
- **SPA Client Routing**: Custom error responses for HTTP 403 and 404 rewrite to `/index.html` with HTTP 200, enabling seamless React Router client-side routing.
- **Hostinger DNS Compatible**: Exports `cloudfront_domain_name` (e.g. `d123456789.cloudfront.net`), which you point your Hostinger DNS CNAME (e.g., `app.yourdomain.com` or `@`) towards.

## Resources Created

- `aws_s3_bucket.frontend`
- `aws_s3_bucket_versioning.frontend`
- `aws_s3_bucket_server_side_encryption_configuration.frontend`
- `aws_s3_bucket_public_access_block.frontend`
- `aws_cloudfront_origin_access_control.frontend`
- `aws_cloudfront_distribution.frontend`
- `aws_s3_bucket_policy.frontend`

## Usage Example

```hcl
module "frontend" {
  source                = "../../modules/frontend_s3_cloudfront"
  name_prefix           = "materia-dev"
  price_class           = "PriceClass_100"
  custom_domain_aliases = [] # e.g. ["app.materia.com"]
  certificate_arn       = null # e.g. "arn:aws:acm:us-east-1:..."
  tags                  = { Environment = "dev" }
}
```
