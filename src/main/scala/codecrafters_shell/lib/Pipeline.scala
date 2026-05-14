package codecrafters_shell
package lib

import java.io._
import java.lang.ProcessBuilder.Redirect

object Pipeline {
  def execute(rawSegments: List[String]): Int = {
    val cmds = rawSegments.map(Parser.tokenize).filter(_.nonEmpty)
    if (cmds.isEmpty) return 0
    if (cmds.length == 1) return runSingle(cmds.head)
    runPipeline(cmds)
  }

  // Core Pipeline Logic ----------------------------------------------------------------
  private def runPipeline(cmds: List[List[String]]): Int = {
    val n     = cmds.length
    val procs = new Array[Process](n)

    for (i <- 0 until n) {
      val (cleanArgs, spec) = Redirects.parse(cmds(i))
      val pb = buildProcessBuilder(cleanArgs)

      // stdin
      if (i == 0) pb.redirectInput(Redirect.INHERIT)
      else        pb.redirectInput(Redirect.PIPE)

      // stdout
      if (i == n - 1) {
        spec.stdoutFile match {
          case Some(path) =>
            val f = new File(path)
            pb.redirectOutput(
              if (spec.stdoutAppend) Redirect.appendTo(f) else Redirect.to(f))
          case None =>
            pb.redirectOutput(Redirect.INHERIT)
        }
      } else {
        pb.redirectOutput(Redirect.PIPE)
      }

      // stderr
      spec.stderrFile match {
        case Some(path) =>
          val f = new File(path)
          pb.redirectError(
            if (spec.stderrAppend) Redirect.appendTo(f) else Redirect.to(f))
        case None =>
          pb.redirectError(Redirect.INHERIT)
      }

      procs(i) = pb.start()

      // Pump stdout of previous process into stdin of this one
      if (i > 0) pump(procs(i - 1).getInputStream, procs(i).getOutputStream)
    }

    var lastCode = 0
    for (i <- 0 until n) lastCode = procs(i).waitFor()
    lastCode
  }

  // Helpers --------------------------------------------------------------------------
  /** Daemon thread: copy all bytes from src → dest, then close dest. */
  private def pump(src: InputStream, dest: OutputStream): Unit = {
    val t = new Thread(new Runnable {
      def run(): Unit = {
        val buf = new Array[Byte](8192)
        try {
          var n = src.read(buf)
          while (n >= 0) { dest.write(buf, 0, n); n = src.read(buf) }
        } catch { case _: IOException => }
        finally { dest.close() }
      }
    })
    t.setDaemon(true)
    t.start()
  }

  private def runSingle(args: List[String]): Int = {
    val (cleanArgs, spec) = Redirects.parse(args)
    Builtins.run(cleanArgs) match {
      case Some(code) => code
      case None       => Executor.run(cleanArgs, spec)
    }
  }

  private def buildProcessBuilder(cleanArgs: List[String]): ProcessBuilder = {
    val executable = cleanArgs.headOption.getOrElse("")
    // Preserve the original `cleanArgs` so the first element (argv[0]) remains
    // the name as invoked (not replaced with an absolute path). This ensures
    // external programs receive the same program-name value the tester expects.
    val resolvedArgs = cleanArgs
    val jl = new java.util.ArrayList[String](resolvedArgs.size)
    resolvedArgs.foreach(jl.add)
    val pb = new ProcessBuilder(jl)
    pb.directory(new File(System.getProperty("user.dir")))
    pb
  }
}