FROM eclipse-temurin:11-jre-alpine
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
RUN chmod +x ./bin/vivy
ENTRYPOINT ["./bin/vivy"]