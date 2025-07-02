ThisBuild / scalaVersion := "3.4.3"
ThisBuild / organization := "com.example"
ThisBuild / version := "1.0.0"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name := "document-matrix",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.11",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.2",
      "dev.zio" %% "zio-json" % "0.7.3",
      "dev.zio" %% "zio-http" % "3.0.1",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-free" % "2.12.0",
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "org.http4s" %% "http4s-ember-server" % "0.23.27",
      "org.http4s" %% "http4s-ember-client" % "0.23.27",
      "org.http4s" %% "http4s-circe" % "0.23.27",
      "org.http4s" %% "http4s-dsl" % "0.23.27",
      "dev.optics" %% "monocle-core" % "3.3.0",
      "dev.optics" %% "monocle-macro" % "3.3.0",
      "org.fusesource.jansi" % "jansi" % "2.4.1",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "dev.zio" %% "zio-test" % "2.1.11" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.1.11" % Test,
      "org.openjdk.jmh" % "jmh-core" % "1.37" % Test,
      "org.openjdk.jmh" % "jmh-generator-annprocess" % "1.37" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Yexplicit-nulls",
      "-Ysafe-init"
    ),
    // Docker settings
    Docker / packageName := "document-matrix",
    Docker / version := version.value,
    dockerBaseImage := "eclipse-temurin:21-jre-alpine",
    dockerExposedPorts := Seq(8080, 8081),
    dockerRepository := Some("ghcr.io"),
    dockerUsername := Some("hostilian"),

    // JMH settings
    Test / javaOptions += "-Djmh.separateClasspathJAR=true",

    // Test settings
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
    Test / parallelExecution := false
  )
