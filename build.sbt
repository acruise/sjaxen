name := "sjaxen"

organization := "com.metaforsoftware"

version := "0.1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "jaxen" % "jaxen" % "1.1.6" intransitive(),
  "junit" % "junit" % "4.12" % "test"
)
