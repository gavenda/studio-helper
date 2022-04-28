FROM postgres
RUN apt-get update -y
RUN apt-get install -y default-jre-headless
RUN mkdir /bot
COPY ./dist/vivy /bot/vivy
WORKDIR /bot/vivy
ENTRYPOINT ["./bin/vivy"]