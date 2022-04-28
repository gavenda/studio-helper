FROM postgres
RUN apt-get update -y
RUN apt-get install -y default-jre-headless
RUN mkdir /bot
COPY ./dist/basura/ /bot/basura
WORKDIR /bot/basura
ENTRYPOINT ["./bin/basura"]