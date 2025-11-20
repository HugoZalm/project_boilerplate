FROM nginx:alpine

COPY default.conf.template /etc/nginx/templates/default.conf.template
COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

# Override the default entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 80
