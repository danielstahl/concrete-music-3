name := "concrete-music-3"

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.3"

libraryDependencies += "com.illposed.osc" % "javaosc-core" % "0.2"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "net.soundmining" %% "soundmining-tools" % "1.0-SNAPSHOT"

libraryDependencies += "net.soundmining" %% "soundmining-modular" % "1.0-SNAPSHOT"

libraryDependencies += "org.quifft" % "quifft" % "0.1.1"

console / initialCommands := """
    |import net.soundmining._
    |ConcreteMusic3.init()
""".trim().stripMargin

console / cleanupCommands += """
    ConcreteMusic3.stop()
"""
