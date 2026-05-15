package codecrafters_shell.lib

import java.io.File

object Completion {
  def candidates(prefix: String): List[String] = {
    val results = scala.collection.mutable.LinkedHashSet[String]()

    // Builtin names
    Builtins.names.foreach { n => if (n.startsWith(prefix)) results += n }

    // Executables on PATH
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

    // Files in current directory
    try {
      val cwd = new File(System.getProperty("user.dir"))
      val files = cwd.list()
      if (files != null) files.foreach { f => if (f.startsWith(prefix)) results += f }
    } catch { case _: Throwable => () }

    results.toList.sorted
  }

  def longestCommonPrefix(candidates: List[String]): String = {
    if (candidates.isEmpty) return ""
    candidates.reduceLeft { (a, b) =>
      val lim = math.min(a.length, b.length)
      var i = 0
      while (i < lim && a.charAt(i) == b.charAt(i)) i += 1
      a.substring(0, i)
    }
  }

  def formatCandidates(candidates: List[String]): String = candidates.mkString("  ")
}
