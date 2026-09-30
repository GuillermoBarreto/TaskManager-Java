/** Represents one task in the task list. */
public class Task {
    private final int id;
    private final String title;
    private boolean completed;

    /**
     * Creates a task with the given id, title, and completion state.
     *
     * @param id        unique task id, assigned by TaskManager
     * @param title     non-blank task title; surrounding whitespace is trimmed
     * @param completed initial completion state
     * @throws IllegalArgumentException if the title is null or blank
     */
    public Task(int id, String title, boolean completed) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("A task title is required.");
        }
        this.id = id;
        this.title = title.trim();
        this.completed = completed;
    }

    /** Returns the unique id of this task. */
    public int getId() {
        return id;
    }

    /** Returns the task title. */
    public String getTitle() {
        return title;
    }

    /** Returns true when the task has been marked complete. */
    public boolean isCompleted() {
        return completed;
    }

    /** Marks the task complete. Completing a task is one-way; there is no undo. */
    public void markComplete() {
        completed = true;
    }

    @Override
    public String toString() {
        return String.format("%d. [%s] %s", id, completed ? "x" : " ", title);
    }
}
