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

libraryDependencies ++= Seq (
  "com.waioeka.sbt" %% "cucumber-runner" % "0.1.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
) ++ cucumber

val framework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks += framework

// Configure the arguments.
testOptions in Test += Tests.Argument(framework,"--glue","")
testOptions in Test += Tests.Argument(framework,"--plugin","pretty")
testOptions in Test += Tests.Argument(framework,"--plugin","html:/tmp/html")
testOptions in Test += Tests.Argument(framework,"--plugin","json:/tmp/json")
