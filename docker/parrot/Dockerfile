FROM gcr.io/distroless/java17-debian11
RUN mkdir /bot
COPY ./dist/parrot /bot/parrot
WORKDIR /bot/parrot
ENTRYPOINT ["./bin/parrot"]
