val scalaVersion_3 = "3.1.0"

ThisBuild / organization := "com.github.tototoshi"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scalaVersion_3

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "2.0.204" % Test,
    "org.typelevel" %% "cats-effect" % "3.3.1" % Test,
    "org.scalameta" %% "munit" % "1.0.0-M1" % Test
  )
)

lazy val core = project
  .in(file("core"))
  .settings(
    commonDependencies,
    name := "sjdbc-core",
    libraryDependencies ++= Seq(
      "org.mockito" % "mockito-all" % "2.0.2-beta"
    )
  )

lazy val `cats-effect` = project
  .in(file("cats-effect"))
  .settings(
    commonDependencies,
    name := "sjdbc-cats-effect",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.1"
    )
  )
  .dependsOn(core)

lazy val example = project
  .in(file("example"))
  .settings(
    commonDependencies,
    name := "sjdbc-example",
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "2.0.204"
    ),
    run / fork := true
  )
  .dependsOn(`cats-effect`)

lazy val root = project
  .in(file("."))
  .aggregate(core, `cats-effect`, example)
