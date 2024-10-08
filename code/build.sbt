organization := "com.github.quixotik"

name := "play-authenticate"

javacOptions ++= Seq("-Werror")

scalacOptions ++= Seq("" +
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8")

crossScalaVersions := Seq("2.13.14", "3.3.1")
scalaVersion := crossScalaVersions.value.head

playEnhancerEnabled := false

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5",
  "com.github.quixotik" %% "play-easymail" % "3.0.5",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-lang3" % "3.4",
  cacheApi,
  javaWs,
  openId,
  guice
)

// add resolver for local artifacts
resolvers += Resolver.file("local-ivy", new File(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

lazy val playAuthenticate = (project in file(".")).enablePlugins(PlayJava)

releasePublishArtifactsAction := PgpKeys.publishSigned.value
