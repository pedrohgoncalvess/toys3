ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "toys3",
    idePackagePrefix := Some("pedro.goncalves")
  )

val AkkaVersion = "2.8.5"
val AkkaHttpVersion = "10.5.0"
val SlickVersion = "3.5.1"
val JwtVersion = "10.0.0"
val JacksonVersion = "2.17.0"


libraryDependencies ++= Seq(
  //akka
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

  //database
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
  "org.xerial" % "sqlite-jdbc" % "3.46.0.0",
  "org.flywaydb" % "flyway-core" % "6.0.0",

  //json
  "org.codehaus.jackson" % "jackson-core-lgpl" % "1.9.13",
  "org.json4s" %% "json4s-native" % "4.0.7",
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,

  //jwt
  "com.github.jwt-scala" %% "jwt-play-json" % JwtVersion,
  "com.github.jwt-scala" %% "jwt-core" % JwtVersion,

  //others
  "com.password4j" % "password4j" % "1.8.1",
  "org.duckdb" % "duckdb_jdbc" % "0.10.2",
  "io.github.cdimascio" % "java-dotenv" % "5.2.2"
)