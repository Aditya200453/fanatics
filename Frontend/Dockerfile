# ✅ Use latest stable node (important)
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

# ✅ FIX for CustomEvent error
ENV NODE_OPTIONS=--experimental-global-webcrypto

RUN npm run build

# Stage 2: Nginx
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]