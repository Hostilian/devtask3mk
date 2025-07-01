ThisBuild / scalaVersion := "3.4.3"
ThisBuild / organization := "com.example"
ThisBuild / version := "1.0.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "document-matrix",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.11",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.2",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "org.http4s" %% "http4s-ember-server" % "0.23.27",
      "org.http4s" %% "http4s-circe" % "0.23.27",
      "org.http4s" %% "http4s-dsl" % "0.23.27",
      "com.github.julien-truffaut" %% "monocle-core" % "3.2.0",
      "com.github.julien-truffaut" %% "monocle-macro" % "3.2.0",
      "org.fusesource.jansi" % "jansi" % "2.4.1",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "dev.zio" %% "zio-test" % "2.1.11" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.1.11" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Yexplicit-nulls",
      "-Ysafe-init"
    )
  )
