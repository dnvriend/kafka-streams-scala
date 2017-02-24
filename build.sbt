name := "kafka-streams-scala"

organization := "com.github.dnvriend"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.1"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.9"
libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.2"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "0.10.2.0"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M1"

// testing
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.2" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"

// testing configuration
fork in Test := true
parallelExecution := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2017", "Dennis Vriend"),
  "conf" -> Apache2_0("2017", "Dennis Vriend", "#")
)

enablePlugins(AutomateHeaderPlugin, SbtScalariform)