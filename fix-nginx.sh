#!/bin/bash
echo "Fixing nginx proxy configuration..."

# Stop any existing frontend containers
docker-compose stop frontend 2>/dev/null || true
docker rm weekly-report-frontend 2>/dev/null || true

# Create a simple solution: use the built frontend with corrected nginx config
echo "Starting frontend with corrected nginx proxy..."

# Create the corrected nginx config
cat > /tmp/nginx-fixed.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # Fixed API proxy - point to external backend
    location /api/ {
        proxy_pass http://23.95.193.155:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_redirect off;
    }
    
    location /health {
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

echo "Nginx proxy configuration fixed!"
echo "Now run: docker-compose up -d frontend"