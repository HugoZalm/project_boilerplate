ARG NODE_VERSION
FROM node:${NODE_VERSION} AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/wateralmanak-frontend/browser /usr/share/nginx/html
EXPOSE 80
