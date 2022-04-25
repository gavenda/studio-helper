FROM openjdk:11-jdk
RUN mkdir /bot
COPY ./dist/basura/ /bot/basura
WORKDIR /bot/basura
ENTRYPOINT ["./bin/basura"]