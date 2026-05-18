# ✅ Build stage (stable setup)
FROM node:20 AS build

WORKDIR /app

# ✅ Clean install (important fix)
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# ✅ Build
RUN npm run build

# ✅ Serve using nginx
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]