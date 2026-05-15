[![progress-banner](https://backend.codecrafters.io/progress/shell/8f746a54-6b71-4b13-acf4-da97bc9c39d2)](https://app.codecrafters.io/users/Lxnthz?r=2qF)

["Build Your Own Shell" Challenge](https://app.codecrafters.io/courses/shell/overview).

---

# Build Your Own Shell (Scala)

This project is a deep-dive into systems programming where I built a fully functional, custom command-line interpreter entirely in Scala. By implementing a Read-Eval-Print Loop (REPL) from the ground up, I created a program capable of managing the entire lifecycle of user commands, from initial parsing and process spawning to handling complex input/output streams. The project transitions from basic command execution to advanced terminal features, mimicking the behavior of industry-standard shells like Bash and ZSH.

---

**Core Features and Functionalities:**

**Basics:**

- Print a prompt

  ```bash
    $
  ```

- Handle invalid commands

  ```bash
    $ xyz
    xyz: command not found
  ```

- Implement a REPL

  ```bash
  # REPL (Read-Eval-Print-Loop) is an interactive loop that forms the core of a shell.
  # It follow a repeating cycle:

  # 1. Read   : Display a prompt and wait for user input
  # 2. Eval   : Parse and execute the command
  # 3. Print  : Display the output or error message
  # 4. Loop   : Return to step 1 and wait for next command

  # The cycle continues indefinitely until the shell process is terminated.

  # Example:

    $ invalid_command_1
    invalid_command_1: command not found
    $ invalid_command_2
    invalid_command_2: command not found
    $ invalid_command_3
    invalid_command_3: command not found
    $                                        # Back to Read, waiting for user input
  ```

- Implement exit

  ```bash
  # The exit builtin is a special command that terminates the shell.
  # When your shell receives the exit command, it should terminate immediately.

  # Example:

    $ invalid_command_1
    invalid_command_1: command not found
    $ exit                                  # Shell terminated
  ```

- Implement echo

  ```bash
  # The echo builtin prints its arguments to stdout, with spaces between them, and a newline (\n) at the end.

  # Example:

    $ echo hello world
    hello world
    $ echo one two three
    one two three
  ```

- Implement type

  ```bash
  # The type builtin is used to determine how a command would be interpreted if it were used.
  # It checks whether a command is a builtin, an executable file, or unrecognized.

  # Example:

    $ type echo
    echo is a shell builtin
    $ type exit
    exit is a shell builtin
    $ type invalid_command
    invalid_command: not found
  ```

- Locate executable files

  ```bash
  # The PATH environment variable specifies a list of directories where the shell should look for executable programs.
  # For example:
    # if the PATH is set to /dir1:/dir2:/dir3, the shell would search for executables in /dir1, then /dir2, and finally /dir3, in that order.
  # When type receives a command input, your shell must follow these steps:
    # 1. Check if the command is a builtin command (like exit or echo). If it is, report it as a builtin (<command> is a shell builtin) and stop.
    # 2. If the command is not a builtin, your shell must go through every directory in PATH. For each directory:
      # 1. Check if a file with the command name exists.
      # 2. Check if the file has execute permissions.
      # 3. If the file exists and has execute permissions, print <command> is <full_path> and stop.
      # 4. If the file exists but lacks execute permissions, skip it and continue to the next directory.
    # 3. If no executable is found in any directory, print <command>: not found.

  # Example:

    $ type grep
    grep is /usr/bin/grep
    $ type invalid_command
    invalid_command: not found
    $ type echo
    echo is a shell builtin
  ```

- Run a program

  ```bash
  # When a command isn't a builtin, your shell should:
    # 1. Determine if the given command is an executable (you can reuse the logic from type)
    # 2. If it is, execute the program
    # 3. Pass any arguments from the command line to the program
  # For example, if the user types custom_exe arg1 arg2, your shell should:
    # - Determine if custom_exe is an executable in PATH
    # - Execute it with three arguments: custom_exe (the program name), arg1, and arg2

  # Example:

    $ custom_exe_1234 alice
    Program was passed 2 args (including program name).
    Arg #0 (program name): custom_exe_1234
    Arg #1: alice
    Program Signature: 5998595441
  ```

**Navigation:**

- The pwd builtin

  ```bash
  # The pwd (print working directory) builtin prints the full, absolute path of the current working directory to stdout.
  # When your shell starts, its current working directory is typically the directory from which it was executed.
  # Your pwd implementation needs to retrieve this information from the operating system and print it.

  # Example:

    $ pwd
    /home/user/projects
    $ pwd
    /usr/local/bin
  ```

- The cd builtin: Absolute paths

  ```bash
  # The cd (change directory) builtin is used to change the current working directory.

  # The cd command can handle different types of arguments:
    # - Absolute paths, like /usr/local/bin.
    # - Relative paths, like ./, ../, ./dir.
    # - The ~ character, which represents the user's home directory.

  # Handling Absolute Paths

  # An absolute path starts with / and specifies a location from the root of the filesystem.
  # When cd receives an absolute path:
    # - If directory exists, change to that directory.
    # - If the directory doesn't exist, print cd: <directory>: No such file or directory

  # Example:

    $ pwd
    /home/user
    $ cd /usr/local/bin
    $ pwd
    /usr/local/bin
    $ cd /does_not_exist
    cd: /does_not_exist: No such file or directory
    $ pwd
    /usr/local/bin
  ```

- The cd builtin: Relative paths

  ```bash
  # Relative Paths

  # A relative path specifies a location relative to the current working directory, rather than from the root of the filesystem.
  # Your shell must correctly interpret and navigate the following components of a relative path:
    # - ./ (Current Directory): Refers to the current working directory itself.
    # - ../ (Parent Directory): Refers to the directory immediately above the current working directory in the file system hierarchy.
    # - Subdirectories: Paths like ./dirname or dirname are treated as relative to the current directory.

  # Example:

    $ pwd
    /usr
    $ cd ./local/bin    # Go to "local/bin" inside current directory (/usr)
    $ pwd
    /usr/local/bin
    $ cd ../../         # Go up two levels
    $ pwd
    /usr
    $ cd local          # "local" is shorthand for "./local"
    $ pwd
    /usr/local
  ```

- The cd builtin: Home direct

  ```bash
  # The ~ Character
  # The ~ (tilde) character is shorthand for the user's home directory.
  # It's a convenient way to quickly navigate back to your home directory from anywhere in the filesystem.

  # The home directory is specified by the HOME environment variable. When your shell sees cd ~, it should:
    # 1. Read the value of the HOME environment variable.
    # 2. Change to that directory

  # Example:

    $ pwd
    /usr/local/bin
    $ cd ~
    $ pwd
    /home/user
    $ cd /var/log
    $ pwd
    /var/log
    $ cd ~
    $ pwd
    /home/user
  ```

**Quoting:**
**Quoting:**

- **Single quotes**: Preserve literal text exactly as written. Use single quotes when you want the shell to treat every character inside as a literal (no expansions, no backslash escapes).

  ```bash
  $ echo 'a $B \ c'
  a $B \ c
  ```

- **Double quotes**: Allow parameter expansion and command/escape interpretation while preserving spaces. Double quotes let `$VAR` and `"` be interpreted but keep words with spaces as a single argument.

  ```bash
  $ NAME=world
  $ echo "hello $NAME"
  hello world
  ```

- **Backslash outside quotes**: A backslash escapes the next character when not inside single quotes. Use it to include a literal space or special char in an argument.

  ```bash
  $ echo a\ b
  a b
  ```

- **Backslash within single quotes**: Inside single quotes a backslash is treated literally (no escaping).

  ```bash
  $ echo '\\n'
  \n
  ```

- **Backslash within double quotes**: Inside double quotes a backslash can escape certain characters (e.g., `"`, `\`, `$`).

  ```bash
  $ echo "She said \"hi\""
  She said "hi"
  ```

- **Executing a quoted executable**: If an executable or path contains spaces or special characters you can quote it to run it. Quoting/escaping the name should not prevent execution when the file exists and is executable.

  ```bash
  $ ./"my program"
  # or
  $ ./my\ program
  ```

**Redirection:**

- **Redirect stdout (`>` )**: Send standard output of a command to a file, overwriting the file if it exists.

  ```bash
  $ echo hello > out.txt
  $ cat out.txt
  hello
  ```

- **Redirect stderr (`2>` )**: Send standard error to a file.

  ```bash
  $ ls nonexist 2> err.txt
  $ cat err.txt
  ls: cannot access 'nonexist': No such file or directory
  ```

- **Append stdout (`>>` )**: Append standard output to the end of a file instead of overwriting.

  ```bash
  $ echo a >> file.txt
  $ echo b >> file.txt
  $ cat file.txt
  a
  b
  ```

- **Append stderr (`2>>` )**: Append standard error to a file.

**Command Completion:**

- **Builtin completion**: Tab-completion suggests shell builtins when typing the first word.

  Example:

  ```bash
  $ e<TAB>
  # expands to:
  $ echo
  ```

- **Completion with arguments**: After a command name, Tab completes filenames or context-appropriate values (e.g., directory names for `cd`).

  Example:

  ```bash
  $ cat rea<TAB>
  # expands to:
  $ cat README.md
  ```

- **Missing completions**: When there are no matches for the current word the shell signals (bell) and does not change the input.

  Example (no visible output; terminal bell):

  ```bash
  $ nomatch<TAB>
  # (bell) no change
  ```

- **Executable completions**: Completes executable names found on `PATH` as well as filenames in the current directory.

  Example:

  ```bash
  $ gre<TAB>
  # expands to:
  $ grep
  ```

- **Multiple completions**: If there are several candidates a second Tab prints the list of matches so the user can choose.

  Example:

  ```bash
  $ xyz_<TAB><TAB>
  xyz_ant  xyz_dog  xyz_pig
  ```

- **Partial completions**: If multiple matches share a prefix, Tab completes up to the longest common prefix.

  Example:

  ```bash
  $ src/mai<TAB>
  # completes to:
  $ src/main/
  ```

**Filename Completion:**

- **File completion**: Complete file names present in the current working directory.

  Example:

  ```bash
  $ cat rea<TAB>
  # expands to:
  $ cat README.md
  ```

- **Nested file completion**: Support completion for paths that include slashes (`dir/subdir/file`).

  Example:

  ```bash
  $ cd src/mai<TAB>
  # expands to:
  $ cd src/main/
  ```

- **Directory completion**: Directory candidates are marked with a trailing slash so a following Tab continues into that directory.

  Example:

  ```bash
  $ du <TAB>
  # completes to:
  $ du pig/
  ```

- **Missing completions**: If no filename matches the current token the shell rings the bell and does not modify the input.

  Example (bell):

  ```bash
  $ no_such_file<TAB>
  # (bell) no change
  ```

- **Multiple matches**: Double-Tab prints the list of matching filenames so the user can pick one.

  Example:

  ```bash
  $ xyz_<TAB><TAB>
  xyz_ant  xyz_dog  xyz_pig
  ```

- **Partial completions**: The shell completes the token up to the longest common prefix shared by the matches.

  Example:

  ```bash
  $ fi<TAB>
  # completes to: file_ (if file_a and file_b exist)
  ```

- **Multi-argument completions**: Only the token under the cursor is completed; earlier arguments remain unchanged.

  Example:

  ```bash
  $ cp README.md new_<TAB>
  # completes the second argument only, e.g. new_file.txt
  ```

**Programmable Completion:**

- **Register complete builtin**: Register a custom completer for a command so completions can be programmatic and context-aware.

  Example (conceptual):

  ```bash
  $ complete register git _git_completer
  Registered completer for 'git'
  ```

- **Printing missing specifications**: If no programmable completion exists for a command the shell falls back to filename completion or prints a helpful message.

  Example:

  ```bash
  $ complete show mycmd
  No completion spec registered for 'mycmd'
  ```

- **Displaying registered specifications**: List the commands that have registered completers.

  Example:

  ```bash
  $ complete list
  git -> _git_completer
  docker -> _docker_completer
  ```

- **Single completion**: If a completer returns a single candidate it is inserted immediately.

  Example:

  ```bash
  $ git br<TAB>
  # expands to:
  $ git branch
  ```

- **Handling no completions**: Programmable completers may return no candidates; the shell indicates this (bell or message).

  Example (bell):

  ```bash
  $ myprog foobar<TAB>
  # (bell) no change
  ```

- **Passing command-line arguments**: Completers receive the full tokenized command line so they can deliver context-aware suggestions.

  Example (conceptual):

  ```text
  Completer receives: ["git","che","--path=src"]
  ```

- **Passing environment variables**: Completers may consult environment variables (like `PATH`, `GIT_DIR`) when producing suggestions.

  Example (conceptual):

  ```text
  Completer uses $PATH to suggest executable names
  ```

- **Multiple completer candidates**: If a completer returns multiple values, the shell supports listing them and partial completion.

  Example:

  ```bash
  $ mycli act<TAB><TAB>
  action_one  action_two  action_three
  ```

- **Longest common prefix**: Tab completes up to the shared prefix when multiple candidates share one.

  Example:

  ```bash
  $ do<TAB>
  # completes to: doc (if doc1 and doc2 exist)
  ```

- **Unregister a completion**: Remove a previously-registered completer when it's no longer needed.

  Example (conceptual):

  ```bash
  $ complete unregister git
  Unregistered completer for 'git'
  ```

**Background Jobs:**

- **The `jobs` builtin**: Lists current background jobs with job ids, PIDs, and command lines.

  Example:

  ```bash
  $ jobs
  [1]+  Running    sleep 60 &
  ```

- **Starting background jobs**: Append `&` to run a command in the background and immediately return to the prompt.

  Example:

  ```bash
  $ sleep 5 &
  [1] 12345
  $ # prompt returns immediately
  ```

- **Printing background job output**: Background jobs that write to the terminal will have their output printed when available (interleaved with prompt notifications).

  Example:

  ```bash
  $ (sleep 1; echo done) &
  [1] 12346
  # After ~1s:
  done
  ```

- **List a single job / List multiple jobs**: `jobs` shows one or many jobs depending on how many are active.

  Example:

  ```bash
  $ jobs
  [1]-  Running    sleep 100 &
  [2]+  Running    tail -f /var/log/syslog &
  ```

- **Reap one job / Reap multiple jobs**: The shell waits on finished child processes to prevent zombies. Use `wait` or rely on automatic reaping.

  Example:

  ```bash
  $ wait %1
  # waits for job 1 to finish
  ```

- **Reap before the next prompt**: Completed jobs are reaped and the shell prints completion notifications before showing the next prompt.

  Example:

  ```bash
  # job finishes while you were typing; next prompt shows:
  [1]+ Done    sleep 2
  $
  ```

- **Recycle job numbers**: Job IDs may be reused after jobs exit; numbering is recycled.

  Example:

  ```bash
  $ sleep 1 &
  [1]
  $ # after job 1 exits
  $ sleep 2 &
  [1]
  ```

**Pipeline:**

- **Dual-command pipeline**: Support `cmd1 | cmd2` where the stdout of `cmd1` feeds stdin of `cmd2`.

  Example:

  ```bash
  $ echo hello | wc -c
  6
  ```

- **Pipelines with built-ins**: Builtins that read stdin or write stdout can be used inside pipelines.

  Example:

  ```bash
  $ echo hello | grep h
  hello
  ```

- **Multi-command pipelines**: Support chains like `a | b | c` with proper pipe setup and reaping.

  Example:

  ```bash
  $ cat file.txt | grep foo | sort
  ```

**History:**

- **The `history` builtin**: Prints previously executed commands with indices.

  Example:

  ```bash
  $ history
  1  ls
  2  echo hello
  3  cat README.md
  ```

- **Listing history**: View recent entries or pass a number to limit results.

  Example:

  ```bash
  $ history 5
  # shows last 5 commands
  ```

- **Limiting history entries**: `history N` prints the last N commands.

  Example:

  ```bash
  $ history 10
  ```

- **Up-arrow / Down-arrow navigation**: Use the arrow keys to recall and edit previous commands interactively.

  Example:

  ```text
  Press ↑ to recall the previous command, ↓ to move forward in history
  ```

- **Executing commands from history**: Execute a previous command by its index using `!N` (if implemented).

  Example:

  ```bash
  $ !2
  # runs the command numbered 2 from history
  ```

**History Persistence:**

- **Read history from file**: Use `history -r <file>` to read history from a file and populate session history.

  Example:

  ```bash
  $ history -r ~/.myshell_history
  ```

- **Write history to file**: Use `history -w <file>` to write the session history to a file.

  Example:

  ```bash
  $ history -w ~/.myshell_history
  ```

- **Append history to file**: Use `history -a <file>` to append the current session's history to an existing file.

  Example:

  ```bash
  $ history -a ~/.myshell_history
  ```

- **Read history on startup / Write on exit / Append on exit**: Set `HISTFILE` to enable automatic read/write behavior at startup/exit.

  Example:

  ```bash
  $ export HISTFILE=~/.myshell_history
  $ ./your_program.sh
  # history will be read on startup and written/appended on exit
  ```

**Parameter Expansion:**

- **The `declare` builtin**: Define shell variables and control simple attributes.

  Example:

  ```bash
  $ declare NAME=alice
  $ echo $NAME
  alice
  ```

- **Printing missing variables**: Use expansion forms to supply defaults or detect missing variables.

  Example:

  ```bash
  $ echo ${UNSET_VAR:-default}
  default
  ```

- **Storing shell variables**: Assign variables and then reference them with `$`.

  Example:

  ```bash
  $ FOO=bar
  $ echo $FOO
  bar
  ```

- **Validating variable names**: Variable names should follow simple rules (letters, digits, underscore; not starting with a digit).

  Example (invalid):

  ```bash
  $ declare 1bad=val
  declare: invalid variable name: 1bad
  ```

- **Expanding variables**: Support `$VAR` and `${VAR}` expansions in words and arguments.

  Example:

  ```bash
  $ NAME=world
  $ echo Hello $NAME
  Hello world
  ```

- **Expansion with braces**: Use `${VAR:-default}` to supply default values when variables are unset/empty.

  Example:

  ```bash
  $ echo ${NAME:-guest}
  guest
  ```

- **Expanding empty variables**: Empty or unset variables expand to an empty string unless a default is provided.

  Example:

  ```bash
  $ EMPTY=""
  $ echo "${EMPTY}"
  # prints an empty line
  ```
