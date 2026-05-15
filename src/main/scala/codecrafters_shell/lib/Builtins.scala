package codecrafters_shell.lib

import java.io.{File, PrintStream}
import java.nio.file.{Files, Paths}

object Builtins {
  val names: Set[String] = Set("cd", "echo", "exit", "pwd", "history", "type", "complete")

  def run(
    args:     List[String], 
    out:      PrintStream = System.out, 
    errOut:   PrintStream = System.err
  ): Option[Int] = {
    args match {
      case Nil                    => None
      case "echo"     :: rest     => Some(runEcho(rest, out, errOut))
      case "pwd"      :: _        => Some(runPwd(out, errOut))
      case "cd"       :: rest     => Some(runCd(rest, out, errOut))
      case "exit"     :: rest     => Some(runExit(rest))
      case "history"  :: rest     => Some(runHistory(rest, out, errOut))
      case "type"     :: rest     => Some(runType(rest, out, errOut))
      case "complete" :: rest     => Some(runComplete(rest, out, errOut))
      case _                      => None
    }
  }

  // `echo` ----------------------------------------------------------------------------
  private def runEcho(args: List[String], out: PrintStream, errOut: PrintStream): Int = {
    out.println(args.mkString(" "))
    0
  }

  // `pwd` ----------------------------------------------------------------------------
  private def runPwd(out: PrintStream, errOut: PrintStream): Int = {
    val cwd = System.getProperty("user.dir")
    out.println(cwd)
    0
  }

  // `cd` -----------------------------------------------------------------------------
  private def runCd(args: List[String], out: PrintStream, errOut: PrintStream): Int = {
    // Determine the target path, expanding `~` to HOME when necessary.
    val maybeTarget: Either[String, String] = args.headOption match {
      case None | Some("~") => Option(System.getenv("HOME")).toRight("cd: HOME environment variable not set")
      case Some(path)        => Right(path)
    }

    maybeTarget match {
      case Left(err) => errOut.println(err); 1
      case Right(target) =>
        // Resolve relative paths against the shell's current `user.dir` property
        val cwd = System.getProperty("user.dir")
        val dir = {
          val f = new File(target)
          if (f.isAbsolute) f else new File(cwd, target)
        }

        if (!dir.exists()) {
          errOut.println(s"cd: $target: No such file or directory")
          1
        } else {
          System.setProperty("user.dir", dir.getCanonicalPath)
          0
        }
    }
  }

  // `type` ---------------------------------------------------------------------------
  private def runType(args: List[String], out: PrintStream, errOut: PrintStream): Int = {
    if (args.isEmpty) {
      errOut.println("type: missing file operand")
      return 1
    }

    val cmd = args.head
    if (names.contains(cmd)) {
      out.println(s"$cmd is a shell builtin")
      0
    } else {
      findInPath(cmd) match {
        case Some(fullPath) => out.println(s"$cmd is $fullPath"); 0
        case None           => errOut.println(s"$cmd: not found"); 1
      }
    }
  }

  // `history` ------------------------------------------------------------------------
  private def runHistory(args: List[String], out: PrintStream, errOut: PrintStream): Int = {
    args match {
      case "-r" :: Nil =>
        errOut.println("history: -r: option requires an argument"); 1
      case "-r" :: file :: _ =>
        History.readFile(file); 0
      case "-w" :: Nil =>
        errOut.println("history: -w: option requires an argument"); 1
      case "-w" :: file :: _ =>
        History.writeFile(file); 0
      case "-a" :: Nil =>
        errOut.println("history: -a: option requires an argument"); 1
      case "-a" :: file :: _ =>
        History.appendFile(file); 0
      case Nil =>
        // Temporarily redirect History display to `out`
        val saved = System.out
        // History.display writes to System.out; swap if redirected
        if (out ne System.out) Console.withOut(out)(History.display())
        else History.display()
        0
      case n :: _ =>
        try {
          val count = n.toInt
          if (out ne System.out) Console.withOut(out)(History.display(count))
          else History.display(count)
          0
        } catch {
          case _: NumberFormatException =>
            errOut.println(s"history: $n: numeric argument required"); 1
        }
    }
  }

  // `exit` ----------------------------------------------------------------------------
  private def runExit(args: List[String]): Int = {
    // Persist history before leaving
    Option(System.getenv("HISTFILE")).foreach(History.writeFile)
    val code = args.headOption.flatMap(s => scala.util.Try(s.toInt).toOption).getOrElse(0)
    sys.exit(code)
    code
  }

  // Helpers ---------------------------------------------------------------------------
  def findInPath(cmd: String): Option[String] = {
    val pathEnv = Option(System.getenv("PATH")).getOrElse("")
    pathEnv.split(File.pathSeparator).collectFirst {
      case dir if {
        val f = new File(dir, cmd)
        f.exists() && f.canExecute
      } => new File(dir, cmd).getPath
    }
  }

  // `complete` ----------------------------------------------------------------------
  private def runComplete(args: List[String], out: PrintStream, errOut: PrintStream): Int = {
    args match {
      case Nil =>
        errOut.println("complete: missing subcommand (register|unregister|list|show)")
        1
      case "list" :: Nil =>
        val regs = ProgrammableCompletion.listRegistered()
        if (regs.isEmpty) out.println("(no completions registered)")
        else regs.toList.sortBy(_._1).foreach { case (k, v) => out.println(s"$k -> $v") }
        0
      case "-p" :: cmd :: Nil =>
        ProgrammableCompletion.get(cmd) match {
          case Some(c) => out.println(s"complete: $cmd: ${c.specString}"); 0
          case None    => out.println(s"complete: $cmd: no completion specification"); 0
        }
      case "show" :: cmd :: Nil =>
        ProgrammableCompletion.get(cmd) match {
          case Some(c) => out.println(s"$cmd -> ${c.specString}"); 0
          case None    => errOut.println(s"No completion spec registered for '$cmd'"); 1
        }
      case "register" :: cmd :: spec :: rest =>
        val params = rest
        val specStr = spec
        ProgrammableCompletion.buildCompleterFromSpec(specStr, params) match {
          case Right(comp) =>
            ProgrammableCompletion.register(cmd, comp)
            out.println(s"Registered completer for '$cmd'")
            0
          case Left(err) => errOut.println(s"complete register: $err"); 1
        }
      case "unregister" :: cmd :: Nil =>
        ProgrammableCompletion.unregister(cmd) match {
          case Some(_) => out.println(s"Unregistered completer for '$cmd'"); 0
          case None    => errOut.println(s"No completer registered for '$cmd'"); 1
        }
      case other =>
        errOut.println(s"complete: unknown arguments: ${other.mkString(" ")}")
        1
    }
  }
}