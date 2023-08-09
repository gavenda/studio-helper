FROM eclipse-temurin:19-jre
RUN mkdir /bot
COPY ./dist/lumi /bot/lumi
WORKDIR /bot/lumi
RUN chmod +x ./bin/lumi
ENTRYPOINT ["./bin/lumi"]
