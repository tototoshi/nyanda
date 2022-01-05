val scalaVersion_3 = "3.1.0"

ThisBuild / organization := "com.github.tototoshi"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scalaVersion_3

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "2.0.206" % Test,
    "org.scalameta" %% "munit" % "1.0.0-M1" % Test,
    "org.mockito" % "mockito-all" % "2.0.2-beta" % Test
  )
)

lazy val module = project
  .in(file("module"))
  .settings(
    commonDependencies,
    name := "nyanda",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.1"
    )
  )

lazy val example = project
  .in(file("example"))
  .settings(
    commonDependencies,
    name := "nyanda-example",
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "2.0.206"
    ),
    run / fork := true
  )
  .dependsOn(module)

lazy val root = project
  .in(file("."))
  .aggregate(module, example)
