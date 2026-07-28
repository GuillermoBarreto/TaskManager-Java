/** Represents one task in the task list. */
public class Task {
    private final int id;
    private final String title;
    private boolean completed;

    public Task(int id, String title, boolean completed) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("A task title is required.");
        }
        this.id = id;
        this.title = title.trim();
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markComplete() {
        completed = true;
    }

    @Override
    public String toString() {
        return String.format("%d. [%s] %s", id, completed ? "x" : " ", title);
    }
}
