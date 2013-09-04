import sbt._
import sbt.Keys._

object PluginBuild extends Build {

  lazy val playHandlebars = Project(
    id = "play-handlebars", base = file(".")
  )

}
