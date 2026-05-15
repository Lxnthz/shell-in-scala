package codecrafters_shell.lib

import java.io.File

object Completion {
  private val sep = File.separatorChar

  def candidates(prefix: String, argContext: Boolean = false): List[String] = {
    val results = scala.collection.mutable.LinkedHashSet[String]()

    // If the user typed a path component (contains '/'), complete within that directory
    val lastSep = prefix.lastIndexOf('/')
    if (lastSep >= 0) {
      val dirPart = prefix.substring(0, lastSep + 1) // keeps trailing '/'
      val base = prefix.substring(lastSep + 1)
      val dirFile = if (dirPart.startsWith("/")) new File(dirPart) else new File(System.getProperty("user.dir"), dirPart)
      try {
        if (dirFile.exists && dirFile.isDirectory) {
          val files = dirFile.list()
          if (files != null) files.foreach { f =>
            if (f.startsWith(base)) {
              val ff = new File(dirFile, f)
              val candidate = dirPart + f + (if (ff.isDirectory) "/" else "")
              results += candidate
            }
          }
        }
      } catch { case _: Throwable => () }

      return results.toList.sorted
    }

    // If completing an argument (after a space), only complete filenames/directories
    if (!argContext) {
      Builtins.names.foreach { n => if (n.startsWith(prefix)) results += n }

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
    }

    try {
      val cwd = new File(System.getProperty("user.dir"))
      val files = cwd.list()
      if (files != null) files.foreach { f =>
        if (f.startsWith(prefix)) {
          val ff = new File(cwd, f)
          results += (f + (if (ff.isDirectory) "/" else ""))
        }
      }
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
