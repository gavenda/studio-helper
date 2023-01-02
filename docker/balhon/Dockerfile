FROM eclipse-temurin:17-jre-alpine
RUN mkdir /bot
COPY ./dist/balhon /bot/balhon
WORKDIR /bot/balhon
RUN chmod +x ./bin/balhon
ENTRYPOINT ["./bin/balhon"]
