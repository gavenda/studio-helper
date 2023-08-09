FROM eclipse-temurin:19-jre
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
RUN chmod +x ./bin/vivy
ENTRYPOINT ["./bin/vivy"]
