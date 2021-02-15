name := "musique-concrete-iii"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.3"

libraryDependencies += "com.illposed.osc" % "javaosc-core" % "0.2"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "net.soundmining" %% "soundmining-tools" % "1.0-SNAPSHOT"

libraryDependencies += "net.soundmining" %% "soundmining-modular" % "1.0-SNAPSHOT"

libraryDependencies += "org.quifft" % "quifft" % "0.1.1"

initialCommands in console := """
    |import net.soundmining._
    |MusiqueConcrete3.init()
""".trim().stripMargin

cleanupCommands in console += """
    MusiqueConcrete3.stop()
"""
