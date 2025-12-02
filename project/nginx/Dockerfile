FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx.conf.template /etc/nginx/templates/nginx.conf.template

COPY health /usr/share/nginx/html/health
EXPOSE 80
