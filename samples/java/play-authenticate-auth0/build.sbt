organization := "com.github.quixotik"

name := "play-authenticate-auth0"

scalaVersion := "2.13.14"

version := "3.0.5-SNAPSHOT"

val appDependencies = Seq(
  // Comment the next line for local development of the Play Authentication core:
  // Use the latest release version when copying this code, e.g. "0.9.0"
  "com.github.quixotik" %% "play-authenticate" % "3.0.5-SNAPSHOT",
  "com.h2database" % "h2" % "2.3.232",
  cacheApi,
  ehcache,
  javaWs
)

// add resolver for local artifacts
resolvers += Resolver.file("local-ivy", new File(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )
  /* Uncomment the next lines for local development of the Play Authenticate core: */
  //.dependsOn(playAuthenticate)
  //.aggregate(playAuthenticate)
