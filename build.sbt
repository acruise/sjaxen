name := "sjaxen"

organization := "com.metaforsoftware"

version := "0.1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "jaxen" % "jaxen" % "1.1.3" intransitive(),
  "junit" % "junit" % "4.8.2" % "test"
)
