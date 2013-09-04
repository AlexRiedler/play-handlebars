package ca.riedler.play.plugins.handlebars

import java.io._
import org.apache.commons.io.FilenameUtils

import sbt._
import scala.Left
import scala.Right
import scala.Some
import sbt.PlayExceptions.AssetCompilationException
import java.io.File

trait HandlebarsTasks extends HandlebarsKeys {

  private def loadResource(name: String): Option[Reader] = {
    Option(this.getClass.getClassLoader.getResource(name)).map(_.openConnection().getInputStream).map(s => new InputStreamReader(s))
  }

  def compile(name: String, source: String): Either[(String, Int, Int), String] = {

    import org.mozilla.javascript._
    import org.mozilla.javascript.tools.shell._

    val handlebars = "handlebars-1.0.0"
    val ctx = Context.enter
    ctx.setLanguageVersion(org.mozilla.javascript.Context.VERSION_1_7)
    ctx.setOptimizationLevel(9)

    val global = new Global
    global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    def loadScript(script: String) {
      val scriptFile = loadResource(script + ".js").getOrElse(throw new Exception("pla2-handlerbars: could not find script " + script))

      try {
        ctx.evaluateReader(scope, scriptFile, script, 1, null)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    loadScript(handlebars)

    ScriptableObject.putProperty(scope, "rawSource", source.replace("\r", ""))

    try {
      Right(ctx.evaluateString(scope, "(Handlebars.precompile(rawSource).toString())", "HandlebarsCompiler", 0, null).toString)
    } catch {
      case e: JavaScriptException => {
        Left(e.details(), e.lineNumber(), 0)
      }
      case e: org.mozilla.javascript.EcmaError => {
        Left(e.details(), e.lineNumber(), 0)
      }
    }
  }

  protected def templateName(sourceFile: String, assetsDir: String): String = {
    val sourceFileWithForwardSlashes = FilenameUtils.separatorsToUnix(sourceFile)
    val assetsDirWithForwardSlashes  = FilenameUtils.separatorsToUnix(assetsDir)
    FilenameUtils.removeExtension(sourceFileWithForwardSlashes.replace(assetsDirWithForwardSlashes + "/", ""))
  }

  import Keys._

  lazy val HandlebarsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, handlebarsTemplateFile, handlebarsFileRegexFrom, handlebarsFileRegexTo, handlebarsAssetsDir, handlebarsAssetsGlob).map {
      (src, resources, cache, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
      val cacheFile = cache / "handlebars"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir / templateFile
      val globalMinified = templatesDir / (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(
        f =>
          FileInfo.lastModified(f).lastModified).reverse
          .map(f => FileInfo.lastModified(f))
          .headOption
          .getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        previousGeneratedFiles.foreach(IO.delete)

        val output = new StringBuilder
        output ++= """(function() {
          var template = Handlebars.template,
              templates = JST = JST || {};
                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val jsSource = compile(templateName(sourceFile.getPath, assetsDir.getPath), IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                Some(line),
                Some(column))
            }.right.get

            output ++= "\ntemplates['%s'] = template(%s);\n\n".format(FilenameUtils.removeExtension(name), jsSource)

            val out = new File(resources, "public/templates/" + naming(name))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "})();\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }

}
