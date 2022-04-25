FROM openjdk:11-jdk
RUN mkdir /bot
COPY ./dist/parrot /bot/parrot
WORKDIR /bot/parrot
ENTRYPOINT ["./bin/parrot"]