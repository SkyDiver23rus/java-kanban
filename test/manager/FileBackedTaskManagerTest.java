package manager;

import model.Task;
import model.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;
    private final File testFile = new File("test_tasks.csv");

    @BeforeEach
    void setUp() {
        if (testFile.exists()) {
            assertTrue(testFile.delete(), "Cannot delete test file before test");
        }
        manager = new FileBackedTaskManager(testFile);
    }

    @AfterEach
    void tearDown() {
        if (testFile.exists()) {
            assertTrue(testFile.delete(), "Cannot delete test file after test");
        }
    }

    @Test
    void createTask_ShouldAddTask() {
        Task task = new Task("Test Task", "Description", "NEW");
        manager.createTask(task);

        Map<Integer, Task> tasks = manager.tasks; // или manager.getTasks(), если есть геттер
        assertEquals(1, tasks.size());
        assertTrue(tasks.containsValue(task));
    }

    @Test
    void createEpic_ShouldAddEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.createEpic(epic);

        Map<Integer, Epic> epics = manager.epics; // или manager.getEpics(), если есть геттер
        assertEquals(1, epics.size());
        assertTrue(epics.containsValue(epic));
    }

    @Test
    void saveAndLoadFromFile_ShouldPersistData() throws IOException {
        Task task = new Task("Test Task", "Description", "NEW");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Epic Description");
        manager.createEpic(epic);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        Map<Integer, Task> loadedTasks = loadedManager.tasks; // или loadedManager.getTasks()
        assertEquals(1, loadedTasks.size());
        assertTrue(loadedTasks.containsValue(task));

        Map<Integer, Epic> loadedEpics = loadedManager.epics; // или loadedManager.getEpics()
        assertEquals(1, loadedEpics.size());
        assertTrue(loadedEpics.containsValue(epic));
    }

    @Test
    void historyToString_ShouldReturnCorrectHistoryFormat() {
        Task task1 = new Task("Task 1", "Description 1", "NEW");
        Task task2 = new Task("Task 2", "Description 2", "NEW");

        manager.createTask(task1);
        manager.createTask(task2);

        manager.historyManager.add(task1);
        manager.historyManager.add(task2);

        String historyString = FileBackedTaskManager.historyToString(manager.historyManager);

        assertEquals(task1.getId() + "," + task2.getId(), historyString);
    }
}