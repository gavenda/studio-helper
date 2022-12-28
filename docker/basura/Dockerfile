FROM eclipse-temurin:17-jre-alpine
RUN mkdir /bot
COPY ./dist/basura/ /bot/basura
WORKDIR /bot/basura
RUN chmod +x ./bin/basura
ENTRYPOINT ["./bin/basura"]
