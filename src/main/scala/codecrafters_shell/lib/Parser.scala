package codecrafters_shell.lib

object Parser {
  case class ParsedCommand(args: List[String])

  def tokenize(input: String): List[String] = {
    /**
      * 'args' will be used to store the final list of arguments after tokenization.
      * 'curr' is a StringBuilder that will be used to build the current argument as we iterate through the input string.
      * 'i' is an index variable to keep track of our current position in the input string.
      * 'inSingleQuotes' and 'inDoubleQuotes' are boolean flags to track whether we are currently inside single or double quotes, which affects how we treat spaces and other characters.
    **/
    val args           = scala.collection.mutable.ListBuffer[String]()
    var curr           = new StringBuilder 
    var i              = 0
    var inSingleQuotes = false
    var inDoubleQuotes = false

    /**
      * The main loop iterates through each character in the input string. Depending on the character and the current quoting state, it performs different actions:
      * - If it encounters a backslash and we're not inside single quotes, it treats the next character as a literal (with special handling for double quotes).
      * - If it encounters a single quote and we're not inside double quotes, it toggles the 'inSingleQuotes' flag.
      * - If it encounters a double quote and we're not inside single quotes, it toggles the 'inDoubleQuotes' flag.
      * - If it encounters whitespace and we're not inside any quotes, it treats it as a delimiter between arguments and adds the current argument to the list if it's not empty.
      * - For any other character, it appends it to the current argument being built.
    **/

    while (i < input.length) {
      val c = input(i)

      c match {
        // * Backlash escape sequence
        case '\\' if !inSingleQuotes => {
          i += 1
          if (i < input.length) {
            val nextChar = input(i)
            if (inDoubleQuotes) {
              // * Inside double quotes only \" and \\\\ are treated as escape sequences
              if (nextChar == '"' || nextChar == '\\') {
                curr += nextChar
              } else {
                curr += '\\'
                curr += nextChar
              }
            } else {
              curr += nextChar
            }
          }
          i += 1
        }

        // * Single quote toggle
        case '\'' if !inDoubleQuotes => {
          inSingleQuotes = !inSingleQuotes
          i += 1
        }

        // * Double quote toggle
        case '"' if !inSingleQuotes => {
          inDoubleQuotes = !inDoubleQuotes
          i += 1
        }

        // * Whitespace delimiter (only if not inside quotes)
        case ch if ch.isWhitespace && !inSingleQuotes && !inDoubleQuotes => {
          if (curr.nonEmpty) {
            args += curr.toString()
            curr.clear()
          }
          i += 1
        }

        // * Regular character
        case ch => {
          curr += ch
          i += 1
        }
      }
    }
    // * Flush the last token
      if (curr.nonEmpty) args += curr.toString()
      args.toList
  }

  /**
    * Convienience wrapper that also detects the pipe character at the top level (not inside quotes) to determine if the command is a pipeline.
  **/
  def containsPipe(input: String): Boolean = {
    var inSingleQuotes = false
    var inDoubleQuotes = false

    input.exists {
      case '\'' => inSingleQuotes = !inSingleQuotes; false
      case '"'  => inDoubleQuotes = !inDoubleQuotes; false
      case '|'  => !inSingleQuotes && !inDoubleQuotes
      case _    => false 
    }
  }

  /**
    * Split a raw line on the top-level pipe character (not inside quotes) to separate pipeline segments.
    * Each segment is trimmed of leading/trailing whitespace and empty segments are filtered out.
    * This allows us to handle commands like: `echo "Hello | World" | grep Hello` correctly, treating the pipe inside quotes as part of the argument rather than a pipeline separator.
  **/
  def splitOnPipe(input: String): List[String] = {
    val segments       = scala.collection.mutable.ListBuffer[String]()
    var curr           = new StringBuilder
    var inSingleQuotes = false
    var inDoubleQuotes = false

    for (c <- input) c match {
      case '\'' => inSingleQuotes = !inSingleQuotes; curr += c
      case '"'  => inDoubleQuotes = !inDoubleQuotes; curr += c
      case '|' if !inSingleQuotes && !inDoubleQuotes => {
        segments += curr.toString().trim
        curr.clear()
      }
      case _    => curr += c
    }
    // * Flush the last segment 
    segments += curr.toString().trim
    segments.toList.filter(_.nonEmpty)
  }
}