FROM openjdk:11-jdk
RUN mkdir /app
COPY ./build/install/parrot/ /app/
WORKDIR /app/bin
CMD ["./parrot"]