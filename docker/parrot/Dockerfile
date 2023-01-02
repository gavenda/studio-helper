FROM eclipse-temurin:17-jre
RUN mkdir /bot
COPY ./dist/parrot /bot/parrot
WORKDIR /bot/parrot
RUN chmod +x ./bin/parrot
ENTRYPOINT ["./bin/parrot"]
