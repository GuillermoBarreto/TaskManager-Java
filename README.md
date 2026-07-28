# Task Manager (Java)

A small command-line task manager written with the Java standard library. Tasks are stored locally in `tasks.txt`, so they remain available the next time you run the program.

## Requirements

- Java Development Kit (JDK) 17 or newer

## Run it

From this folder, compile and run:

```powershell
javac Main.java Task.java TaskManager.java
java Main
```

## Commands

```text
add <task>       Add a task
list             Show all tasks
complete <id>    Mark a task complete
delete <id>      Remove a task
help             Show the command list
quit             Exit the program
```

Example session:

```text
> add Finish Java assignment
Added task 1.
> list
1. [ ] Finish Java assignment
> complete 1
Task marked complete.
> list
1. [x] Finish Java assignment
```

`tasks.txt` is created automatically and is intentionally ignored by Git because it contains your local task list.
