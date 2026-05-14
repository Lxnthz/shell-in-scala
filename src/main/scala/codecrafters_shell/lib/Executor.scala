package codecrafters_shell.lib

import java.io.{File, FileOutputStream, PrintStream}

object Executor {
  def run(args: List[String], spec: Redirects.RedirectSpec = Redirects.RedirectSpec()): Int = {
    if (args.isEmpty) return 0

    val executable = resolveExecutable(args.head) match {
      case Some(p) => p
      case None    => System.err.println(s"${args.head}: command not found"); return 127
    }

    val pb = new ProcessBuilder((executable :: args.tail).asJava)
    pb.directory(new File(System.getProperty("user.dir")))

    spec.stdoutFile match {
      case Some(path) => {
        val f = new File(path)
        pb.redirectOutput(
          if (spec.stdoutAppend) ProcessBuilder.Redirect.appendTo(f)
          else ProcessBuilder.Redirect.to(f)
        )
      }
      case None => pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }
    // stderr handling: mirror Pipeline.runPipeline behavior so that stderr
    // is inherited by default or redirected to a file when requested.
    spec.stderrFile match {
      case Some(path) =>
        val f = new File(path)
        pb.redirectError(
          if (spec.stderrAppend) ProcessBuilder.Redirect.appendTo(f)
          else ProcessBuilder.Redirect.to(f)
        )
      case None => pb.redirectError(ProcessBuilder.Redirect.INHERIT)
    }
    val process = pb.start()
    process.waitFor()
  } 

  // Helpers --------------------------------------------------------------------------
  private def resolveExecutable(cmd: String): Option[String] = {
    if (cmd.contains(File.separator)) {
      val f = new File(cmd)
      if (f.exists() && f.canExecute()) Some(f.getAbsolutePath) else None
    } else {
      // If the command exists somewhere on PATH, return the command name
      // (not the absolute path) so that the spawned process receives the
      // original argv[0] value (e.g. "custom_exe_3037") instead of a
      // full path like "/tmp/owl/custom_exe_3037".
      Builtins.findInPath(cmd) match {
        case Some(_) => Some(cmd)
        case None    => None
      }
    }
  }

  // Implicits ------------------------------------------------------------------------
  implicit private class ListOps(val list: List[String]) {
    def asJava: java.util.List[String] = {
      val jl = new java.util.ArrayList[String](list.size)
      list.foreach(jl.add)
      jl
    }
  }
}