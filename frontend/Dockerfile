# Multi-stage build za React frontend

# Stage 1: Build React aplikacije
FROM node:20-alpine AS builder

WORKDIR /app

# Kopiraj package.json
COPY package*.json ./

# Instaliraj dependencies
RUN npm ci

# Kopiraj izvorni kod
COPY . .

# Build argument za API URL
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=${REACT_APP_API_URL}

# Build produkcione verzije
RUN npm run build

# Stage 2: Nginx za serviranje static fajlova
FROM nginx:alpine

# Kopiraj build iz prethodnog stage-a
COPY --from=builder /app/build /usr/share/nginx/html

# Kopiraj nginx konfiguraciju
COPY nginx.conf /etc/nginx/nginx.conf

# Expose portovi
EXPOSE 80 443

# Startuj nginx
CMD ["nginx", "-g", "daemon off;"]