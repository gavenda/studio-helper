FROM openjdk:11-jdk
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
ENTRYPOINT ["./bin/vivy"]