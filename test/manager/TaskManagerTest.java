package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    protected Task generateTask() {
        Task task = new Task("task", "desc", "NEW");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now());
        return task;
    }

    protected Epic generateEpic() {
        return new Epic("epic", "desc");
    }

    protected Subtask generateSubtask(int epicId, String status) {
        Subtask subtask = new Subtask("sub", "desc", status, epicId);
        subtask.setDuration(Duration.ofMinutes(30));
        subtask.setStartTime(LocalDateTime.now());
        return subtask;
    }


    @Test
    void createAndGetTask() {
        Task task = generateTask();
        manager.createTask(task);
        Task fetched = manager.getTaskById(task.getId());
        assertEquals(task, fetched);
    }

    @Test
    void removeAllTasksRemovesEverything() {
        Task task = generateTask();
        manager.createTask(task);
        manager.removeAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void updateTaskWorks() {
        Task task = generateTask();
        manager.createTask(task);
        task.setStatus(Status.DONE);
        manager.updateTask(task);
        assertEquals(Status.DONE, manager.getTaskById(task.getId()).getStatus());
    }

    // Тесты на статус Epic

    @Test
    void epicStatusAllNew() {
        Epic epic = manager.createEpic(generateEpic());
        manager.createSubtask(generateSubtask(epic.getId(), "NEW"));
        manager.createSubtask(generateSubtask(epic.getId(), "NEW"));
        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = manager.createEpic(generateEpic());
        manager.createSubtask(generateSubtask(epic.getId(), "DONE"));
        manager.createSubtask(generateSubtask(epic.getId(), "DONE"));
        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusNewAndDone() {
        Epic epic = manager.createEpic(generateEpic());
        manager.createSubtask(generateSubtask(epic.getId(), "NEW"));
        manager.createSubtask(generateSubtask(epic.getId(), "DONE"));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = manager.createEpic(generateEpic());
        manager.createSubtask(generateSubtask(epic.getId(), "IN_PROGRESS"));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void getSubtasksByEpicReturnsCorrectList() {
        Epic epic = manager.createEpic(generateEpic());
        Subtask s1 = generateSubtask(epic.getId(), "NEW");
        Subtask s2 = generateSubtask(epic.getId(), "IN_PROGRESS");
        manager.createSubtask(s1);
        manager.createSubtask(s2);
        List<Subtask> subtasks = manager.getSubtasksByEpic(epic.getId());
        assertTrue(subtasks.contains(s1));
        assertTrue(subtasks.contains(s2));
    }

    @Test
    void getPrioritizedTasksSortedByStartTime() {
        Task t1 = generateTask();
        t1.setStartTime(LocalDateTime.now().plusDays(1));
        manager.createTask(t1);
        Task t2 = generateTask();
        t2.setStartTime(LocalDateTime.now());
        manager.createTask(t2);
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2, prioritized.get(0));
        assertEquals(t1, prioritized.get(1));
    }

    //  Тест пересечения интервалов
    @Test
    void intervalIntersectionPreventsOverlap() {
        Task t1 = generateTask();
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.createTask(t1);

        Task t2 = generateTask();
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(60));
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(t2));
    }
}
