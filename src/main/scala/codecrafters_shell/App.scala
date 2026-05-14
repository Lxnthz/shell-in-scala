package codecrafters_shell
package lib

import java.io.{BufferedReader, InputStreamReader, File}

object Main {
  private val Prompt = "$ "

  // Enable raw terminal mode so we can read keypresses (Tab, Backspace)
  private def execStty(args: String): Unit = {
    try {
      new ProcessBuilder("sh", "-c", s"stty $args").inheritIO().start().waitFor()
    } catch { case _: Throwable => () }
  }

  private def enableRawMode(): Unit = execStty("-echo -icanon min 1 time 0")
  private def restoreMode(): Unit = execStty("sane")

  def main(args: Array[String]): Unit = {
    // Load history from file HISTFILE if set
    Option(System.getenv("HISTFILE")).foreach(History.readFile)

    // Ensure terminal is restored on exit
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        try restoreMode() finally Option(System.getenv("HISTFILE")).foreach(History.writeFile)
      }
    })

    enableRawMode()
    try {
      while (true) {
        val line = readLineWithCompletion(Prompt)
        if (line == null) {
          Option(System.getenv("HISTFILE")).foreach(History.writeFile)
          System.exit(0)
        }
        val trimmedLine = line.trim
        if (trimmedLine.nonEmpty) {
          History.add(trimmedLine)
          dispatch(trimmedLine)
        }
      }
    } finally restoreMode()
  }

  // Read input character-by-character, handle Tab completion and simple editing.
  private def readLineWithCompletion(prompt: String): String = {
    System.out.print(prompt); System.out.flush()
    val sb = new StringBuilder
    val in = System.in

    def completePrefix(): Unit = {
      val lastSpace = sb.lastIndexWhere(_.isWhitespace)
      val prefix = if (lastSpace == -1) sb.toString() else sb.substring(lastSpace + 1)
      val candidates = completionCandidates(prefix)
      if (candidates.size == 1) {
        val completion = candidates.head
        val rest = completion.substring(prefix.length)
        sb.append(rest)
        sb.append(' ')
        System.out.print(rest + " ")
        System.out.flush()
      }
    }

    def handleBackspace(): Unit = {
      if (sb.nonEmpty) {
        sb.setLength(sb.length - 1)
        // Erase last character on terminal
        System.out.print("\b \b")
        System.out.flush()
      }
    }

    while (true) {
      val r = in.read()
      if (r == -1) return null
      r match {
        case 10 | 13 => System.out.println(); return sb.toString()
        case 8 | 127 => handleBackspace()
        case 9 => completePrefix() // Tab key
        case c =>
          sb.append(c.toChar)
          System.out.print(c.toChar)
          System.out.flush()
      }
    }
    ""
  }

  private def completionCandidates(prefix: String): List[String] = {
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

  private def dispatch(input: String): Unit = {
    // 1. Pipe chain?
    if (Parser.containsPipe(input)) {
      Pipeline.execute(Parser.splitOnPipe(input))
      return
    }

    // 2. Tokenize
    val tokens = Parser.tokenize(input)
    if (tokens.isEmpty) return

    // 3. Strip redirection operators
    val (cleanArgs, spec) = Redirects.parse(tokens)
    if (cleanArgs.isEmpty) return

    // 4. Open redirects streams (if any) and execute
    val stdoutStream = spec.stdoutFile.map(p => Redirects.openStream(p, spec.stdoutAppend))
    val stderrStream = spec.stderrFile.map(p => Redirects.openStream(p, spec.stderrAppend))

    val out = stdoutStream.getOrElse(System.out)
    val errOut = stderrStream.getOrElse(System.err)

    try {
      Builtins.run(cleanArgs, out, errOut) match {
        case Some(_) => // Builtin handled it
        case None    => Executor.run(cleanArgs, spec)
      }
    } finally {
      stdoutStream.foreach(_.close())
      stderrStream.foreach(_.close())
    }
  }
}
