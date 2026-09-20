FROM node:16

WORKDIR /app
COPY . .

RUN yarn config set registry https://registry.npm.taobao.org

RUN yarn && yarn install

RUN yarn build

FROM nginx:alpine

WORKDIR /data

RUN rm /etc/nginx/conf.d/default.conf

COPY docker/pig-ui.conf /etc/nginx/conf.d/

COPY --from=0 /app/docker/dist/ /data
