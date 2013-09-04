package ca.riedler.play.plugins.handlebars

import sbt._
import sbt.Keys._
import org.apache.commons.io.FilenameUtils

object HandlebarsPlugin extends Plugin with HandlebarsTasks {

  val handlebarsSettings = Seq(
    handlebarsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates")),
    handlebarsExtension := ".hbs",
    handlebarsTemplateFile := "templates.pre.js",
    handlebarsAssetsGlob <<= (handlebarsAssetsDir)(assetsDir =>  (assetsDir ** "*.hbs")),
    handlebarsFileRegexFrom <<= (handlebarsExtension)(fileEnding => fileEnding),
    handlebarsFileRegexTo <<= (handlebarsExtension)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),
    resourceGenerators in Compile <+= HandlebarsCompiler
  )

  override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ handlebarsSettings

}
