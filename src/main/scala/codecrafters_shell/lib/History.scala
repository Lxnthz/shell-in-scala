package codecrafters_shell
package lib
package shell

import java.io.{File, FileOutputStream, PrintStream}
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object History {
  private val entries         = ArrayBuffer[String]()
  private var appendBaseIndex = 0

  // Mutation -------------------------------------------------------------------------
  def add(line: String): Unit = if (line.nonEmpty) entries += line

  def size: Int = entries.length

  // Display --------------------------------------------------------------------------
  def display(lastN: Int = 0): Unit = {
    val startIdx = if (lastN > 0 && lastN < entries.length) entries.length - lastN else 0
    
    for (i <- startIdx until entries.length) {
      println(f"${i + 1}%5d  ${entries(i)}")
    }
  }

  // File I/O -------------------------------------------------------------------------
  def readFile(path: String): Unit = {
    val file = new File(path)
    if (!file.exists()) System.err.println(s"history: $path: cannot read history file"); return

    try {
      val lines = Source.fromFile(file).getLines().toList
      entries.insertAll(0, lines)
      appendBaseIndex = entries.length
    } catch {
      case e: Exception => System.err.println(s"history: $path: ${e.getMessage}")
    }
  }

  // Overwrite `path` with the entire in-memory history
  def writeFile(path: String): Unit = {
    withWriter(path, append = false) pw => entries.foreach(pw.println)
  }

  // Append only the new entries (since last append) to `path`.
  def appendFile(path: String): Unit = {
    val newEntries = entries.drop(appendBaseIndex)
    if (newEntries.nonEmpty) {
      withWriter(path, append = true) pw => newEntries.foreach(pw.println); appendBaseIndex = entries.length
    }
  }

  // Helpers --------------------------------------------------------------------------
  private def withWriter(path: String, append: Boolean)(f: PrintWriter => Unit): Unit = {
    try {
      val pw = new PrintWriter(new FileOutputStream(new File), append)
      try f(pw) finally pw.close()
    } catch {
      case e: Exception => System.err.println(s"history: $path: ${e.getMessage}")
    }
  }
  
}