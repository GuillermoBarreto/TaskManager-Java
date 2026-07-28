import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Stores tasks and saves them to a small local data file. */
public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();
    private final Path storagePath;
    private int nextId = 1;

    public TaskManager(Path storagePath) {
        this.storagePath = storagePath;
        load();
    }

    public Task addTask(String title) {
        Task task = new Task(nextId++, title, false);
        tasks.add(task);
        save();
        return task;
    }

    public boolean completeTask(int id) {
        Optional<Task> task = findTask(id);
        if (task.isEmpty() || task.get().isCompleted()) {
            return false;
        }
        task.get().markComplete();
        save();
        return true;
    }

    public boolean deleteTask(int id) {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    public List<Task> getTasks() {
        return tasks.stream().sorted(Comparator.comparingInt(Task::getId)).toList();
    }

    private Optional<Task> findTask(int id) {
        return tasks.stream().filter(task -> task.getId() == id).findFirst();
    }

    private void load() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\t", 3);
                if (parts.length != 3) {
                    continue;
                }
                int id = Integer.parseInt(parts[0]);
                String title = new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8);
                tasks.add(new Task(id, title, Boolean.parseBoolean(parts[1])));
                nextId = Math.max(nextId, id + 1);
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not read task data from " + storagePath, exception);
        }
    }

    private void save() {
        List<String> lines = tasks.stream()
                .map(task -> task.getId() + "\\t" + task.isCompleted() + "\\t"
                        + Base64.getEncoder().encodeToString(task.getTitle().getBytes(StandardCharsets.UTF_8)))
                .toList();
        try {
            Files.write(storagePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save task data to " + storagePath, exception);
        }
    }
}
