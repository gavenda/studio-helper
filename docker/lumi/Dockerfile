FROM eclipse-temurin:17-jre-alpine
RUN mkdir /bot
COPY ./dist/lumi /bot/lumi
WORKDIR /bot/lumi
RUN chmod +x ./bin/lumi
ENTRYPOINT ["./bin/lumi"]
