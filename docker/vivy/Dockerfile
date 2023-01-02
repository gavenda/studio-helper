FROM eclipse-temurin:17-jre
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
RUN chmod +x ./bin/vivy
ENTRYPOINT ["./bin/vivy"]
