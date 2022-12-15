FROM gcr.io/distroless/java17-debian11
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
RUN chmod +x ./bin/vivy
ENTRYPOINT ["./bin/vivy"]
