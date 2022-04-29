FROM eclipse-temurin:11-alpine
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
ENTRYPOINT ["./bin/vivy"]