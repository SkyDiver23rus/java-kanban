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
        Task task = new Task("Task", "Desc", "NEW");
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
}
