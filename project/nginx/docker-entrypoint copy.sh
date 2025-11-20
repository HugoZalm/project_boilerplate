#!/bin/sh
envsubst '${NGINX_PORT} ${APP_HOSTNAME}' \
  < /etc/nginx/conf.d/default.conf.template \
  > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'


#!/bin/sh
set -e

echo "Substituting environment variables in templates..."

# Substitute variables in all template files
for template in /etc/nginx/templates/*.template; do
    filename=$(basename "$template" .template)
    echo "Processing $template -> /etc/nginx/conf.d/$filename"
    envsubst '${NGINX_PORT} ${SERVER_NAME} ${API_HOST} ${API_PORT}' \
        < "$template" \
        > "/etc/nginx/conf.d/$filename"
done

echo "Testing nginx configuration..."
nginx -t

echo "Starting nginx..."
exec nginx -g 'daemon off;'