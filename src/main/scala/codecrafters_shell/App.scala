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
    var lastTabCandidates: List[String] = Nil
    var lastTabPrefix: String = ""
    var lastWasTab: Boolean = false

    def completePrefix(): Unit = {
      val lastSpace = sb.lastIndexWhere(_.isWhitespace)
      val prefix = if (lastSpace == -1) sb.toString() else sb.substring(lastSpace + 1)
      val candidates = Completion.candidates(prefix)

      // If the previous key was a Tab and the prefix hasn't changed, show the list
      if (lastWasTab && lastTabCandidates.nonEmpty && lastTabPrefix == prefix) {
        System.out.println()
        System.out.println(Completion.formatCandidates(lastTabCandidates))
        System.out.print(prompt + sb.toString())
        System.out.flush()
        lastTabCandidates = Nil
        lastTabPrefix = ""
        lastWasTab = false
        return
      }

      if (candidates.isEmpty) {
        // No completions: ring the terminal bell
        System.out.print("\u0007")
        System.out.flush()
        lastTabCandidates = Nil
        lastTabPrefix = ""
        lastWasTab = true
      } else if (candidates.size == 1) {
        val completion = candidates.head
        val rest = completion.substring(prefix.length)
        sb.append(rest)
        System.out.print(rest)
        // If completion is a directory (ends with '/'), don't append a space
        if (!completion.endsWith("/")) {
          sb.append(' ')
          System.out.print(" ")
        }
        System.out.flush()
        lastTabCandidates = Nil
        lastTabPrefix = ""
        lastWasTab = false
      } else {
        // Multiple candidates: try to complete to their longest common prefix
        val lcp = Completion.longestCommonPrefix(candidates)
        if (lcp.length > prefix.length) {
          val rest = lcp.substring(prefix.length)
          sb.append(rest)
          System.out.print(rest)
          System.out.flush()
        } else {
          // No extension possible: ring bell
          System.out.print("\u0007")
          System.out.flush()
        }
        // Remember this Tab state so a following Tab can list options
        lastTabCandidates = candidates
        lastTabPrefix = prefix
        lastWasTab = true
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
        case 8 | 127 =>
          handleBackspace()
          lastWasTab = false
          lastTabCandidates = Nil
        case 9 =>
          completePrefix() // Tab key
        
        case c =>
          sb.append(c.toChar)
          System.out.print(c.toChar)
          System.out.flush()
          lastWasTab = false
          lastTabCandidates = Nil
      }
    }
    ""
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
