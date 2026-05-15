package codecrafters_shell.lib

import java.io.File

/**
 * Simple programmable completion registry and a few builtin completers.
 *
 * Completers implement `complete(cmd, args, prefix)` and return matching candidates.
 */
object ProgrammableCompletion {
  trait Completer {
    def complete(cmd: String, args: List[String], prefix: String): List[String]
    def specString: String
  }

  private val registry = scala.collection.mutable.Map.empty[String, Completer]

  def register(cmd: String, c: Completer): Unit = registry.put(cmd, c)
  def unregister(cmd: String): Option[Completer] = registry.remove(cmd)
  def get(cmd: String): Option[Completer] = registry.get(cmd)
  def listRegistered(): Map[String, String] = registry.toMap.view.mapValues(_.specString).toMap

  // ---- Builtin completers ----
  case class StaticCompleter(items: List[String]) extends Completer {
    override def complete(cmd: String, args: List[String], prefix: String): List[String] =
      items.filter(_.startsWith(prefix)).sorted
    override def specString: String = s"static(${items.mkString(",")})"
  }

  case object GitCompleter extends Completer {
    private val cmds = List(
      "add", "bisect", "branch", "checkout", "clone", "commit", "diff",
      "fetch", "grep", "init", "log", "merge", "mv", "pull", "push",
      "rebase", "remote", "reset", "show", "status", "tag"
    )
    override def complete(cmd: String, args: List[String], prefix: String): List[String] =
      cmds.filter(_.startsWith(prefix)).sorted
    override def specString: String = "git"
  }

  case object FilesCompleter extends Completer {
    private def listInDir(dir: File, base: String, prefix: String): List[String] = {
      val files = Option(dir.list()).getOrElse(Array.empty)
      files.filter(_.startsWith(prefix)).map { f =>
        val ff = new File(dir, f)
        base + f + (if (ff.isDirectory) "/" else "")
      }.toList.sorted
    }

    override def complete(cmd: String, args: List[String], prefix: String): List[String] = {
      // Handle nested paths like dir/sub/
      val lastSep = prefix.lastIndexOf('/')
      if (lastSep >= 0) {
        val dirPart = prefix.substring(0, lastSep + 1)
        val base = prefix.substring(lastSep + 1)
        val dirFile = if (dirPart.startsWith("/")) new File(dirPart) else new File(System.getProperty("user.dir"), dirPart)
        try {
          if (dirFile.exists() && dirFile.isDirectory) listInDir(dirFile, dirPart, base) else Nil
        } catch { case _: Throwable => Nil }
      } else {
        val cwd = new File(System.getProperty("user.dir"))
        try listInDir(cwd, "", prefix) catch { case _: Throwable => Nil }
      }
    }

    override def specString: String = "files"
  }

  case object PathCompleter extends Completer {
    override def complete(cmd: String, args: List[String], prefix: String): List[String] = {
      val results = scala.collection.mutable.LinkedHashSet[String]()
      Option(System.getenv("PATH")).getOrElse("").split(File.pathSeparator).foreach { d =>
        try {
          val dir = new File(d)
          if (dir.exists && dir.isDirectory) {
            val files = dir.list()
            if (files != null) files.foreach { f =>
              if (f.startsWith(prefix)) {
                val ff = new File(dir, f)
                if (ff.canExecute) results += f
              }
            }
          }
        } catch { case _: Throwable => () }
      }
      results.toList.sorted
    }
    override def specString: String = "path"
  }

  case class EnvCompleter(varName: String) extends Completer {
    override def complete(cmd: String, args: List[String], prefix: String): List[String] = {
      val v = Option(System.getenv(varName)).getOrElse("")
      if (v.isEmpty) Nil
      else {
        v.split(File.pathSeparator).filter(_.startsWith(prefix)).toList.sorted
      }
    }
    override def specString: String = s"env:$varName"
  }

  // Factory for common specs
  def buildCompleterFromSpec(spec: String, params: List[String]): Either[String, Completer] = {
    if (spec == "files") Right(FilesCompleter)
    else if (spec == "path") Right(PathCompleter)
    else if (spec == "git") Right(GitCompleter)
    else if (spec.startsWith("env:")) Right(EnvCompleter(spec.substring(4)))
    else if (spec == "static") {
      if (params.nonEmpty) Right(StaticCompleter(params)) else Left("static completer requires a list of values")
    } else if (spec.startsWith("static:")) {
      val items = spec.substring(7).split(",").toList.filter(_.nonEmpty)
      Right(StaticCompleter(items))
    } else Left(s"unknown spec: $spec")
  }
}
