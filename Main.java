import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/** Console entry point for the task manager. */
public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager(Path.of("tasks.txt"));

        System.out.println("Simple Task Manager");
        System.out.println("Your tasks are saved in tasks.txt. Type 'help' for commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                String[] commandAndArgument = input.split("\\s+", 2);
                String command = commandAndArgument[0].toLowerCase();
                String argument = commandAndArgument.length == 2 ? commandAndArgument[1].trim() : "";

                try {
                    switch (command) {
                        case "add" -> addTask(manager, argument);
                        case "list" -> listTasks(manager.getTasks());
                        case "complete" -> completeTask(manager, argument);
                        case "delete" -> deleteTask(manager, argument);
                        case "help" -> printHelp();
                        case "quit", "exit" -> {
                            System.out.println("Goodbye!");
                            return;
                        }
                        default -> System.out.println("Unknown command. Type 'help' to see available commands.");
                    }
                } catch (IllegalArgumentException exception) {
                    System.out.println("Error: " + exception.getMessage());
                } catch (IllegalStateException exception) {
                    System.out.println("Could not save your changes: " + exception.getMessage());
                }
            }
        }
    }

    private static void addTask(TaskManager manager, String title) {
        Task task = manager.addTask(title);
        System.out.println("Added task " + task.getId() + ".");
    }

    private static void listTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Add one with: add Buy groceries");
            return;
        }
        tasks.forEach(System.out::println);
    }

    private static void completeTask(TaskManager manager, String argument) {
        int id = parseId(argument);
        System.out.println(manager.completeTask(id) ? "Task marked complete." : "Task not found or already complete.");
    }

    private static void deleteTask(TaskManager manager, String argument) {
        int id = parseId(argument);
        System.out.println(manager.deleteTask(id) ? "Task deleted." : "Task not found.");
    }

    private static int parseId(String argument) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Provide a task number, for example: complete 2");
        }
    }

    private static void printHelp() {
        System.out.println("Commands:\n"
                + "  add <task>       Add a task\n"
                + "  list             Show all tasks\n"
                + "  complete <id>    Mark a task complete\n"
                + "  delete <id>      Remove a task\n"
                + "  help             Show this message\n"
                + "  quit             Exit the program");
    }
}
