# Stage 1: Build Vue app
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --prefer-offline

COPY . .

# Override outDir về dist/ thay vì path relative sang backend
RUN npx vue-tsc --noEmit && npx vite build --outDir dist --emptyOutDir

# Stage 2: Serve với nginx
FROM nginx:1.25-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
