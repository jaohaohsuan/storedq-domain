lazy val cpJarsForDocker = taskKey[Unit]("prepare for building Docker image")

val akkaVersion = "2.5.4"

lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-cluster",
  "com.typesafe.akka" %% "akka-cluster-tools",
  "com.typesafe.akka" %% "akka-cluster-metrics",
  "com.typesafe.akka" %% "akka-persistence",
  "com.typesafe.akka" %% "akka-slf4j",
  "com.typesafe.akka" %% "akka-remote").map(_ % akkaVersion)

lazy val cucumber = Seq(
  "io.cucumber" %  "cucumber-core",
  "io.cucumber" %% "cucumber-scala" ,
  "io.cucumber" %  "cucumber-jvm",
  "io.cucumber" %  "cucumber-junit").map(_ % "2.0.0" % "test")


lazy val refined = Seq(
  "eu.timepit" %% "refined",
  "eu.timepit" %% "refined-pureconfig",
  "eu.timepit" %% "refined-scalacheck",
  "eu.timepit" %% "refined-scalaz",
  "eu.timepit" %% "refined-scodec").map(_ % "0.8.2" )

lazy val cucumberFramework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")

lazy val root: Project = project.in(file(".")).settings(
  name               := "storedq-domain",
  organization       := "grandsys",
  scalaVersion       := "2.12.3",
  version            := "0.1.0",
  exportJars         := true,
  fork in run in Global := true,
  resolvers          ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    "jitpack" at "https://jitpack.io"
  ),
  cpJarsForDocker    := {

    val dockerDir = (target in Compile).value / "docker"

    val jar = (packageBin in Compile).value
    IO.copyFile(jar, dockerDir / "app" / jar.name)

    (dependencyClasspath in Compile).value.files.foreach { f => IO.copyFile(f, dockerDir / "libs" / f.name )}

    (mainClass in Compile).value.foreach { content => IO.write( dockerDir / "mainClass", content ) }

    IO.copyFile(baseDirectory.value / "Dockerfile", dockerDir / "Dockerfile")
  },
  libraryDependencies ++= Seq (
    "org.iq80.leveldb"            % "leveldb"                       % "0.7",
    "org.fusesource.leveldbjni"   % "leveldbjni-all"                % "1.8",
    "ch.qos.logback"              % "logback-classic"               % "1.2.3",
    "com.typesafe.scala-logging"  %% "scala-logging"                % "3.7.2",
    "com.lightbend.akka"          %% "akka-management-cluster-http" % "0.4",
    "com.github.jaohaohsuan"      % "storedq-grpc"                  % "master-SNAPSHOT",
    "com.waioeka.sbt"             %% "cucumber-runner"              % "0.1.2",
    "com.typesafe.akka"           %% "akka-testkit"                 % akkaVersion         % "test",
    "org.scalatest"               %% "scalatest"                    % "3.0.1"             % "test",
    "org.scalacheck"              %% "scalacheck"                   % "1.13.4"            % "test"
  ) ++ cucumber ++ akka ++ refined,
  unmanagedClasspath in Test += baseDirectory.value / "src/test/resources",
  CucumberPlugin.glue := "com/grandsys/inu/storedq",
  testFrameworks += cucumberFramework,
  // Configure the arguments.
  testOptions in Test ++= Seq(
    Tests.Argument(cucumberFramework,"--glue",""),
    Tests.Argument(cucumberFramework,"--plugin","pretty"),
    Tests.Argument(cucumberFramework,"--plugin","html:/tmp/html"),
    Tests.Argument(cucumberFramework,"--plugin","json:/tmp/json"))
).enablePlugins(CucumberPlugin)
