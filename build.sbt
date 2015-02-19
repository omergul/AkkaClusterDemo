name := """ClusterDemo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
	"com.typesafe.play.plugins" %% "play-plugins-redis" % "2.3.1",
	"com.typesafe.akka" % "akka-cluster_2.10" % "2.3.9",
	"com.typesafe.akka" % "akka-contrib_2.10" % "2.3.9"
	//"com.typesafe.play.plugins" % "play-plugins-redis_2.10" % "2.3.1"
  //"com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT"
)

resolvers += "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"