package manager;

import java.util.List;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setup() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addNewTask() {
        Task task = new Task("Тест", "Тест описание");
        task.setStatus(Status.NEW);
        final Task savedTask = taskManager.createTask(task);

        assertNotNull(savedTask, "Задача не создана.");
        assertEquals(task, taskManager.getTaskById(savedTask.getId()), "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Список задач не должен быть пустым.");
        assertEquals(1, tasks.size(), "Неверное количество задач в списке.");
        assertEquals(task, tasks.get(0), "Созданная задача не совпадает с ожидаемой.");
    }
}