package codecrafters_shell
package lib

import java.io.{BufferedReader, InputStreamReader}

object Main {
  private val Prompt = "$ "

  def main(args: Array[String]): Unit = {
    // Load history from file HISTFILE if set
    Option(System.getenv("HISTFILE")).foreach(History.readFile)

    val reader = new BufferedReader(new InputStreamReader(System.in))
    val out = new java.io.PrintStream(System.out)
    System.setOut(out)

    while (true) {
      print(Prompt)
      System.out.flush()

      val line = reader.readLine()

      // EOF (Ctrl+D) handling
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
