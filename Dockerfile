FROM sbtscala/scala-sbt:graalvm-community-21.0.2_1.10.0_3.4.2

WORKDIR /app

COPY . /app

RUN sbt update

EXPOSE 8080

CMD ["sbt", "run"]