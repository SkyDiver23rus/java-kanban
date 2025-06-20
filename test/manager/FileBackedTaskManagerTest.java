package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File file;

    @BeforeEach
    void setup() throws Exception {
        file = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void teardown() {
        file.delete();
    }

    @Test
    void saveAndLoadTasks() {
        // Создаем задачи
        Task task1 = new Task("Task1", "Desc1", "NEW");
        Task task2 = new Task("Task2", "Desc2", "IN_PROGRESS");
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic = new Epic("Epic1", "EpicDesc");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "SubDesc1", "NEW", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "SubDesc2", "DONE", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Добавляем в историю
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask1.getId());

        // Загружаем новый менеджер из файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // Проверяем задачи
        List<Task> tasks = loaded.getAllTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Task1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Task2")));

        // Проверяем эпики
        List<Epic> epics = loaded.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals("Epic1", epics.get(0).getName());

        // Проверяем подзадачи
        List<Subtask> subtasks = loaded.getAllSubtasks();
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub1")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub2")));

        // Проверяем историю
        List<Task> history = loaded.getHistory();
        assertEquals(3, history.size());
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
        // После заголовка и пустой строки истории больше ничего нет
        assertTrue(lines.size() <= 2);
    }

    @Test
    void loadFromFileWithNoHistory() throws Exception {
        Task task = new Task("Task", "Desc", "NEW");
        manager.createTask(task);
        // Удаляем всю историю (имитируем ситуацию без неё)
        manager.getHistory().clear();
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> loadedHistory = loaded.getHistory();
        assertTrue(loadedHistory.isEmpty());
    }

    @Test
    void testCreateManager() {
        FileBackedTaskManager manager = new FileBackedTaskManager(new File("tasks.csv"));
        assertNotNull(manager);
    }
}