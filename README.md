[![progress-banner](https://backend.codecrafters.io/progress/shell/8f746a54-6b71-4b13-acf4-da97bc9c39d2)](https://app.codecrafters.io/users/Lxnthz?r=2qF)

["Build Your Own Shell" Challenge](https://app.codecrafters.io/courses/shell/overview).

---

# Build Your Own Shell (JavaScript)

This project is a deep-dive into systems programming where I built a fully functional, custom command-line interpreter entirely in Rust. By implementing a Read-Eval-Print Loop (REPL) from the ground up, I created a program capable of managing the entire lifecycle of user commands, from initial parsing and process spawning to handling complex input/output streams. The project transitions from basic command execution to advanced terminal features, mimicking the behavior of industry-standard shells like Bash and ZSH.

---

**Core Features and Functionalities:**

**Basics:**

- Print a prompt
- Handle invalid commands
- Implement a REPL
- Implement exit
- Implement echo
- Implement type
- Locate executable files
- Run a program

**Navigation:**

- The pwd builtin
- The cd builtin: Absolute paths
- The cd builtin: Relative paths
- The cd builtin: Home direct

**Quoting:**

- Single quotes
- Double quotes
- Backslash outside quotes
- Backslash within a single quotes
- Backslash within a double quotes
- Executing a quoted executable

**Redirection:**

- Redirect stdout
- Redirect stderr
- Append stdout
- Appedn stderr

**Command Completion:**

- Builtin completion
- Completion with arguments
- Missing completions
- Executable completions
- Multiple completions
- Partial completions

**Filename Completion:**

- File completion
- Nested file completion
- Directory completion
- Missing completions
- Multiple matches
- Partial completions
- Multi-argument completions

**Programmable Completion:**

- Register complete builtin
- Printing missing specifications
- Displaying registered specifications
- Single completion
- Handling no completions
- Passing command-line arguments
- Passing environment variables
- Multiple completer candidates
- Longest common prefix
- Unregister a completion

**Background Jobs:**

- The jobs builtin
- Starting background jobs
- Printing background job output
- List a single job
- List multiple jobs
- Reap one job
- Reap multiple jobs
- Reap before the next prompt
- Recycle job numbers

**Pipeline:**

- Dual-command pipeline
- Pipelines with built-ins
- Multi-command pipelines

**History:**

- The history builtin
- Listing history
- Limiting history entires
- Up-arrow navigation
- Down-arrow navigation
- Executing commands from history

**History Persistence:**

- Read history from file
- Write history to file
- Append history to file
- Read history on startup
- Write history on exit
- Append history on exit

**Parameter Expansion:**

- The declare builtin
- Printing missing variables
- Storing shell variables
- Validating variable names
- Expanding variables
- Expansion with braces
- Expanding empty variables