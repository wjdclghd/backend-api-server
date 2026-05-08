# External Public Deployment Baseline

## Target Shape

```text
iOS/Web Client
-> HTTPS
-> Nginx or Load Balancer
-> BackendApiServer:8080
-> RDS PostgreSQL or private PostgreSQL
```

## Required Environment Variables

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<rds-private-endpoint>:5432/backend_api
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>
CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com
```

## Network Rules

- Public inbound: `80`, `443`
- Backend inbound: `8080` only from Nginx or load balancer security group
- Database inbound: `5432` only from backend security group
- Do not expose PostgreSQL directly to the public internet.

## Local Docker Compose

The local PostgreSQL port is bound to `127.0.0.1` so other LAN devices cannot connect to the database directly.

## HTTPS Front

Use `deploy/nginx/backend-api.conf` as the starting point. Replace `api.example.com` and certificate paths before use.

## Production Checklist

- Run with `SPRING_PROFILES_ACTIVE=prod`.
- Keep SQL logging disabled.
- Keep CORS origins explicit and HTTPS-only.
- Put rate limiting on both Nginx/load balancer and application endpoints.
- Store secrets in EC2 Parameter Store, Secrets Manager, or deployment environment variables.
