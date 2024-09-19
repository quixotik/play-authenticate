// Comment to get more information during initialisation
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

addSbtPlugin("org.playframework" % "sbt-play-ebean" % "8.1.0")

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

// Uncomment the next line for local development of the Play Authentication core:
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.7")
