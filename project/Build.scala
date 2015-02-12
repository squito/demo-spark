import sbt._
import sbt.Keys._

import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbtassembly.Plugin._
import AssemblyKeys._

object MyBuild extends Build {
  
  lazy val demo1 = Project(id="demo1", base=file("demo1"), settings = demo1Settings).
    settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  def sharedSettings = Defaults.defaultSettings ++ assemblySettings ++
  Seq(
    version := "0.1",
    scalaVersion := "2.10.4",
    scalacOptions := Seq("-deprecation", "-unchecked", "-optimize"),
    unmanagedJars in Compile <<= baseDirectory map { base => (base / "lib" ** "*.jar").classpath },
    retrieveManaged := true,
    transitiveClassifiers in Scope.GlobalScope := Seq("sources"),
    resolvers ++= Seq(
      "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases",
      "cloudera-repos"  at "https://repository.cloudera.com/artifactory/cloudera-repos",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(
      "com.quantifind" % "sumac_2.10" % "0.3.0",
      "org.apache.spark" % "spark-core_2.10" % "1.0.0-cdh5.1.0" % "provided",
      "org.scalatest" %% "scalatest" % "2.1.3" % "test"
    ), 

    publishMavenStyle := true,

    publishArtifact in Test := false,
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    assemblyOption in assembly ~= { _.copy(includeScala = false) }
  )

  def demo1Settings = sharedSettings ++ Seq(
      name := "demo1"
    )
}
