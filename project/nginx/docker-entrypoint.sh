#!/bin/sh
set -e

echo "Substituting environment variables in conf-files..."

# Substitute variables in nginx.conf file
envsubst '${NGINX_PORT} ${APP_HOSTNAME}' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf
echo "... nginx.cong"${APP_URL} from env-file)

# Substitute variables in all template files
for template in /etc/nginx/templates/*.template; do
    filename=$(basename "$template" .template)
    # echo "Processing $template -> /etc/nginx/conf.d/$filename"
    envsubst '${NGINX_PORT} ${APP_HOSTNAME}' \
        < "$template" \
        > "/etc/nginx/conf.d/$filename"
    echo "... conf.d/$filename"
done

echo "Testing nginx configuration..."
nginx -t

echo "Starting nginx..."
exec nginx -g 'daemon off;'
