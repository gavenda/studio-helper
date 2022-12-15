FROM gcr.io/distroless/java17-debian11
RUN mkdir /bot
COPY ./dist/basura/ /bot/basura
WORKDIR /bot/basura
RUN chmod +x ./bin/basura
ENTRYPOINT ["./bin/basura"]
