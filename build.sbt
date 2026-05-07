name := "gatling-demo"

scalaVersion := "2.13.14"

enablePlugins(GatlingPlugin)

scalacOptions := Seq(
  "-encoding", "UTF-8",
  "-release", "11",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions"
)

javacOptions := Seq(
  "-encoding", "UTF-8"
)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.15.0" % "test,it",
  "io.gatling"            % "gatling-test-framework"    % "3.15.0" % "test,it",
  "org.scalameta"        %% "munit"                     % "1.0.0"  % Test
)
