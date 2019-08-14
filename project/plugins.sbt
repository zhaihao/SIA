addSbtPlugin("com.github.gseitz"                 % "sbt-release"                % "1.0.11")
addSbtPlugin("com.lightbend.paradox"             % "sbt-paradox"                % "0.4.4")
addSbtPlugin("io.github.jonas"                   % "sbt-paradox-material-theme" % "0.6.0")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-site"                   % "1.3.3")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-ghpages"                % "0.6.3")
addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings"           % "latest.release")
addSbtPlugin("com.timushev.sbt"                  % "sbt-updates"                % "0.4.0")
addSbtPlugin("org.tpolecat"                      % "tut-plugin"                 % "0.6.10")
addSbtPlugin("net.virtual-void"                  % "sbt-dependency-graph"       % "0.9.2")
addSbtPlugin("com.typesafe.play"                 % "sbt-plugin"                 % "2.7.2")
addSbtPlugin("com.typesafe.sbt"                  % "sbt-multi-jvm"              % "0.4.0")
addSbtPlugin("com.thesamet"                      % "sbt-protoc"                 % "0.99.23")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0-M7"
