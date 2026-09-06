# ===================================================================
# Multi-stage Dockerfile for Materia Frontend (React + Vite)
# ===================================================================

# -------------------------------------------------------------------
# Stage 1: Build Application
# -------------------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# Build arguments for Vite environment variables
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Cache npm dependencies layer
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code
COPY . .

# Build production bundle directly with Vite
RUN npx vite build

# -------------------------------------------------------------------
# Stage 2: Serve with Nginx
# -------------------------------------------------------------------
FROM nginx:1.27-alpine

# Replace default Nginx configuration with SPA configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy production build from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
