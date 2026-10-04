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

            // Corrupt-line coverage: lines with bad separators, an invalid
            // completion flag, or a non-numeric id are skipped without losing
            // the good lines.
            String goodTitle = Base64.getEncoder().encodeToString("Good task".getBytes(StandardCharsets.UTF_8));
            Files.writeString(storage, "12\tfalse\t" + goodTitle + "\n"
                    + "this line has no separators\n"
                    + "13\tyes\t" + goodTitle + "\n"
                    + "notanid\tfalse\t" + goodTitle + "\n");
            TaskManager corrupt = new TaskManager(storage);
            assert corrupt.getTasks().size() == 1;
            assert corrupt.getTasks().get(0).getId() == 12;
            assert corrupt.getTasks().get(0).getTitle().equals("Good task");
            assert corrupt.addTask("Task after corruption").getId() == 13;
            System.out.println("Corrupt line skip checks passed.");

            // Failed adds (blank title) must not consume a task id.
            Path idGapStorage = Files.createTempFile("task-manager-idgap-", ".txt");
            try {
                TaskManager idGaps = new TaskManager(idGapStorage);
                Task first = idGaps.addTask("First");
                boolean rejected = false;
                try {
                    idGaps.addTask("   ");
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }
                assert rejected : "blank title should be rejected";
                Task second = idGaps.addTask("Second");
                assert second.getId() == first.getId() + 1 : "rejected add must not consume an id";
                System.out.println("Blank-title id checks passed.");
            } finally {
                Files.deleteIfExists(idGapStorage);
            }
        } finally {
            Files.deleteIfExists(storage);
        }
    }
}

