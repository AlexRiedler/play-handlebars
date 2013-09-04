package ca.riedler.play.plugins.handlebars

import sbt._

trait HandlebarsKeys {
    val handlebarsAssetsDir = SettingKey[File]("play-handlebars-assets-dir")
    val handlebarsExtension = SettingKey[String]("play-handlebars-file-ending")
    val handlebarsAssetsGlob = SettingKey[PathFinder]("play-handlebars-assets-glob")
    val handlebarsTemplateFile = SettingKey[String]("play-handlebars-template-file")
    val handlebarsFileRegexFrom = SettingKey[String]("play-handlebars-file-regex-from")
    val handlebarsFileRegexTo = SettingKey[String]("play-handlebars-file-regex-to")
    val handlebarsVersion = SettingKey[String]("play-handlebars-version")
}
