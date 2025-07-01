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
    void saveAndLoadTasks() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW.name());
        task1.setStartTime(LocalDateTime.of(2023, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));

        Task task2 = new Task("Task2", "Desc2", Status.IN_PROGRESS.name());
        task2.setStartTime(LocalDateTime.of(2023, 1, 1, 11, 2));
        task2.setDuration(Duration.ofMinutes(30));

        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic = new Epic("Epic1", "EpicDesc");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "SubDesc1", Status.NEW.name(), epic.getId());
        subtask1.setStartTime(LocalDateTime.of(2023, 1, 1, 12, 0));
        subtask1.setDuration(Duration.ofMinutes(40));

        Subtask subtask2 = new Subtask("Sub2", "SubDesc2", Status.DONE.name(), epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2023, 1, 1, 12, 42));
        subtask2.setDuration(Duration.ofMinutes(20));

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // История
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());

        // Загружаем новый менеджер из файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<Task> tasks = loaded.getAllTasks();
        assertEquals(2, tasks.size(), "Должно быть 2 задачи");
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task2")));

        List<Epic> epics = loaded.getAllEpics();
        assertEquals(1, epics.size(), "Должен быть 1 эпик");
        assertEquals("Epic1", epics.get(0).getTitle());

        List<Subtask> subtasks = loaded.getAllSubtasks();
        assertEquals(2, subtasks.size(), "Должно быть 2 подзадачи");
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Sub1")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Sub2")));

        List<Task> history = loaded.getHistory();
        assertEquals(3, history.size(), "Должно быть 3 задачи в истории");
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask1.getId(), history.get(2).getId());
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