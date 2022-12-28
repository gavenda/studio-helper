FROM eclipse-temurin:17-jre-alpine
RUN mkdir /bot
COPY ./dist/parrot /bot/parrot
WORKDIR /bot/parrot
ENTRYPOINT ["./bin/parrot"]
