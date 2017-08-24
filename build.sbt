name := "storedq-domain"

organization := "com.grandsys.inu"

scalaVersion := "2.12.3"

version := "0.1.0"

resolvers += Resolver.sonatypeRepo("snapshots")

enablePlugins(CucumberPlugin)

CucumberPlugin.glue := "com/grandsys/inu/storedq/"

lazy val cucumber = Seq(
  "io.cucumber" % "cucumber-core" ,
  "io.cucumber" %% "cucumber-scala",
  "io.cucumber" % "cucumber-jvm",
  "io.cucumber" % "cucumber-junit").map(_ % "2.0.0-SNAPSHOT").map(_ % "test")

lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-cluster",
  "com.typesafe.akka" %% "akka-cluster-metrics",
  "com.typesafe.akka" %% "akka-slf4j",
  "com.typesafe.akka" %% "akka-remote"
).map(_ % "2.5.4")

libraryDependencies ++= Seq (
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.lightbend.akka" %% "akka-management-cluster-http" % "0.3",
  "com.waioeka.sbt" %% "cucumber-runner" % "0.1.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
) ++ cucumber ++ akka

val framework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks += framework

// Configure the arguments.
testOptions in Test += Tests.Argument(framework,"--glue","")
testOptions in Test += Tests.Argument(framework,"--plugin","pretty")
testOptions in Test += Tests.Argument(framework,"--plugin","html:/tmp/html")
testOptions in Test += Tests.Argument(framework,"--plugin","json:/tmp/json")
