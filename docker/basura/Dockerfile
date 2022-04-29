FROM eclipse-temurin:11-jre-alpine
RUN mkdir /bot
COPY ./dist/basura/ /bot/basura
WORKDIR /bot/basura
ENTRYPOINT ["./bin/basura"]