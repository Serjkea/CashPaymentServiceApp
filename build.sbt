import sbt.Keys._
import sbt._

lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      name := "root",
      skip in publish := true    )
    .withId("root")
    .settings(
      commonSettings
    )
    .aggregate(
      cashPaymentServicePipeline,
      dataModel,
      filePaymentsIngress,
      participantInitializeIngress,
      paymentCheckingStreamlet,
      paymentProcessingStreamlet,
      paymentLoggingEgress
    )

lazy val cashPaymentServicePipeline = appModule("cash-payment-service-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "cashpaymentservice",
    runLocalConfigFile := Some("cash-payment-service-pipeline/src/main/resources/local.conf")
  )

lazy val dataModel = appModule("datamodel")
  .enablePlugins(CloudflowLibraryPlugin)
  .settings(
    commonSettings
  )

lazy val filePaymentsIngress = appModule("file-payments-ingress")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    runLocalConfigFile := Some("file-payments-ingress/src/main/resources/local.conf"),
    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-stream-alpakka-file" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
    )
  )
  .dependsOn(dataModel)

lazy val participantInitializeIngress = appModule("participant-initialize-ingress")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
    )
  )
  .dependsOn(dataModel)

lazy val paymentCheckingStreamlet = appModule("payment-checking-streamlet")
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)

lazy val paymentProcessingStreamlet = appModule("payment-processing-streamlet")
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)

lazy val paymentLoggingEgress = appModule("payment-logging-egress")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(
      name := moduleID
    )
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val commonSettings = Seq(
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),
  scalacOptions in(Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in(Test, console) := (scalacOptions in(Compile, console)).value
)
