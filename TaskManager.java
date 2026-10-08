import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    /** Adds a task with the given title, saves, and returns the created task. */
    public Task addTask(String title) {
        int id = nextId;
        // Validate the title before spending the id: a blank title throws in
        // the Task constructor, and must not leave a gap in the id sequence.
        Task task = new Task(id, title, false);
        nextId = id + 1;
        tasks.add(task);
        save();
        return task;
    }

    /**
     * Marks the task with the given id complete and saves.
     * Returns false when the task does not exist or is already complete.
     */
    public boolean completeTask(int id) {
        Optional<Task> task = findTask(id);
        if (task.isEmpty() || task.get().isCompleted()) {
            return false;
        }
        task.get().markComplete();
        save();
        return true;
    }

    /**
     * Removes the task with the given id and saves. Returns false when no
     * task with that id exists.
     */
    public boolean deleteTask(int id) {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if (removed) {
            save();
        }
        return removed;
    }

    /** Returns all tasks ordered by id. */
    public List<Task> getTasks() {
        return tasks.stream().sorted(Comparator.comparingInt(Task::getId)).toList();
    }

    private Optional<Task> findTask(int id) {
        return tasks.stream().filter(task -> task.getId() == id).findFirst();
    }

    /**
     * Loads tasks from the storage file at startup. Skips corrupt lines so one
     * bad line can never prevent the rest of the tasks from loading, then
     * advances nextId past the highest id found.
     */
    private void load() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
                parseLine(line).ifPresent(tasks::add);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read task data from " + storagePath, exception);
        }
        for (Task task : tasks) {
            nextId = Math.max(nextId, task.getId() + 1);
        }
    }

    /**
     * Parses one line of the data file. Returns empty for corrupt lines so a
     * single bad line can never prevent the rest of the tasks from loading.
     */
    private static Optional<Task> parseLine(String line) {
        try {
            // Accept the literal separators written by older versions too.
            String[] parts = line.replace("\\t", "\t").split("\t", 3);
            if (parts.length != 3) {
                return Optional.empty();
            }
            if (!parts[1].equals("true") && !parts[1].equals("false")) {
                return Optional.empty();
            }
            int id = Integer.parseInt(parts[0]);
            String title = new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            return Optional.of(new Task(id, title, Boolean.parseBoolean(parts[1])));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /**
     * Persists all tasks to the storage file, writing to a temp file first and
     * atomically replacing the data file so a crash mid-write cannot leave a
     * half-written tasks file behind.
     */
    private void save() {
        List<String> lines = tasks.stream()
                .map(task -> task.getId() + "\t" + task.isCompleted() + "\t"
                        + Base64.getEncoder().encodeToString(task.getTitle().getBytes(StandardCharsets.UTF_8)))
                .toList();
        // Write to a temp file first, then atomically replace the data file so a
        // crash mid-write can never leave a half-written tasks file behind.
        Path parent = storagePath.toAbsolutePath().getParent();
        try {
            Path tempFile = Files.createTempFile(parent, ".tasks", ".tmp");
            try {
                Files.write(tempFile, lines, StandardCharsets.UTF_8);
                try {
                    Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException atomicNotSupported) {
                    // The temp file and the data file can live on different
                    // filesystems (e.g. a network-mounted home directory): fall
                    // back to a plain replace instead of failing the save.
                    Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException | RuntimeException exception) {
                Files.deleteIfExists(tempFile);
                throw exception;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save task data to " + storagePath, exception);
        }
    }
}

