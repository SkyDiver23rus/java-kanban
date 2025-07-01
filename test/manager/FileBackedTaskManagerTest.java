package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File file;

    @BeforeEach
    void setup() throws Exception {
        file = File.createTempFile("tasks", ".csv");
        file.delete();
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void teardown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void saveEmptyManager() throws Exception {
        manager.save();
        List<String> lines = Files.readAllLines(file.toPath());
        // Первая строка — это заголовок
        assertTrue(lines.get(0).contains("id,type,name,status,description,epic"));
        // После заголовка должна быть пустая строка (разделитель между задачами и историей)
        assertTrue(lines.size() >= 1);
    }

    @Test
    void loadFromFileWithNoHistory() throws Exception {
        Task task = new Task("Task", "Desc", Status.NEW.name());
        task.setStartTime(LocalDateTime.of(2023, 1, 2, 10, 0));
        task.setDuration(Duration.ofMinutes(10));
        manager.createTask(task);
        // Очищаем историю
        manager.getHistory().clear();
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> loadedHistory = loaded.getHistory();
        assertTrue(loadedHistory.isEmpty(), "История после загрузки должна быть пустой");
    }

    @Test
    void testCreateManager() {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File("tasks.csv"));
        assertNotNull(manager);
    }

    @Test
    void testEpicStatusAllNewAndAllDone() {
        Epic epic = new Epic("EpicTest", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW.name(), epic.getId());
        sub1.setStartTime(LocalDateTime.of(2023, 2, 1, 10, 0));
        sub1.setDuration(Duration.ofMinutes(20));
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW.name(), epic.getId());
        sub2.setStartTime(LocalDateTime.of(2023, 2, 1, 10, 22));
        sub2.setDuration(Duration.ofMinutes(20));
        manager.createSubtask(sub2);

        Epic loadedEpic = manager.getEpicById(epic.getId());
        assertEquals(Status.NEW, loadedEpic.getStatus(), "Если все NEW, эпик NEW");

        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        loadedEpic = manager.getEpicById(epic.getId());
        assertEquals(Status.DONE, loadedEpic.getStatus(), "Если все DONE, эпик DONE");
    }

    @Test
    void testEpicStatusNewAndDoneMix() {
        Epic epic = new Epic("EpicMix", "Desc");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW.name(), epic.getId());
        sub1.setStartTime(LocalDateTime.of(2023, 3, 1, 10, 0));
        sub1.setDuration(Duration.ofMinutes(20));
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Sub2", "Desc", Status.DONE.name(), epic.getId());
        sub2.setStartTime(LocalDateTime.of(2023, 3, 1, 10, 22));
        sub2.setDuration(Duration.ofMinutes(20));
        manager.createSubtask(sub2);

        Epic loadedEpic = manager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "NEW + DONE = IN_PROGRESS");
    }
}