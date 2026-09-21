import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/** Run with: java -ea TaskManagerTest */
public class TaskManagerTest {
    public static void main(String[] args) throws Exception {
        Path storage = Files.createTempFile("task-manager-test-", ".txt");
        try {
            TaskManager manager = new TaskManager(storage);
            Task task = manager.addTask("Read a chapter");
            manager.completeTask(task.getId());
            TaskManager restored = new TaskManager(storage);
            assert restored.getTasks().size() == 1;
            assert restored.getTasks().get(0).getTitle().equals("Read a chapter");
            assert restored.getTasks().get(0).isCompleted();

            String title = Base64.getEncoder().encodeToString("Legacy task".getBytes(StandardCharsets.UTF_8));
            Files.writeString(storage, "7\\tfalse\\t" + title);
            TaskManager legacy = new TaskManager(storage);
            assert legacy.getTasks().get(0).getTitle().equals("Legacy task");
            assert legacy.addTask("Next task").getId() == 8;
            assert new TaskManager(storage).getTasks().size() == 2;
            System.out.println("Task persistence checks passed.");

            // Delete coverage: removing a task persists the removal.
            TaskManager deleter = new TaskManager(storage);
            assert deleter.getTasks().size() == 2;
            assert deleter.deleteTask(7);
            assert !deleter.deleteTask(7);
            TaskManager afterDelete = new TaskManager(storage);
            assert afterDelete.getTasks().size() == 1;
            assert afterDelete.getTasks().get(0).getTitle().equals("Next task");
            System.out.println("Task delete checks passed.");
        } finally {
            Files.deleteIfExists(storage);
        }
    }
}
