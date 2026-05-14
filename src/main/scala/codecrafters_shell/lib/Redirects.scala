package codecrafters_shell.lib

import java.io.{File, FileOutputStream, PrintStream}

object Redirects {
  // * Describes how stdout and stderr should be redirected based on the presence of >, >>, 2>, and 2>> operators in the command arguments (if at all).
  case class RedirectSpec(
    stdoutFile: Option[String]      = None,
    stdoutAppend: Boolean           = false,
    stderrFile: Option[String]      = None,
    stderrAppend: Boolean           = false
  )

  /**
    * Scan `args` for redirection tokens
    * Return `(cleanArgs, RedirectSpec)` where `cleanArgs` has the operator
    * filename tokens removed and `RedirectSpec` describes the redirections to be performed.
  **/

  def parse(args: List[String]): (List[String], RedirectSpec) = {
    val clean  = scala.collection.mutable.ListBuffer[String]()
    var spec   = RedirectSpec()
    var i      = 0

    while (i < args.length) {
      args(i) match {
        case ">>" | "1>>" => spec = spec.copy(stdoutFile = nextArg(i, args), stdoutAppend = true); i += 2
        case ">"  | "1>"  => spec = spec.copy(stdoutFile = nextArg(i, args), stdoutAppend = false); i += 2
        case "2>>"        => spec = spec.copy(stderrFile = nextArg(i, args), stderrAppend = true); i += 2
        case "2>"         => spec = spec.copy(stderrFile = nextArg(i, args), stderrAppend = false); i += 2
        case token        => clean += token; i += 1
      }
    }
    (clean.toList, spec)
  }

  /**
    * Open a PrintStream for writing / appending.
  **/

  def openStream(path: String, append: Boolean): PrintStream = {
    new PrintStream(new FileOutputStream(new File(path), append))
  }

  /**
    * Helpers
  **/

  private def nextArg(i: Int, args: List[String]): Option[String] = {
    if (i + 1 < args.length) Some(args(i + 1)) else None
  }
}