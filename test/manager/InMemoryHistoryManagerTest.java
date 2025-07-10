package manager;

import java.util.List;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void testNoDuplicatesAndOrderInHistory() {
        Task task1 = new Task("Таск1", "Описание1", "NEW");
        task1.setId(1);
        Task task2 = new Task("Таск2", "Описание2", "NEW");
        task2.setId(2);
        Task task3 = new Task("Таск3", "Описание3", "NEW");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Должно быть три задачи");

        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task2, history.get(2));
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Таск1", "Описание1", "NEW");
        task1.setId(1);
        Task task2 = new Task("Таск2", "Описание2", "NEW");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void testRemoveMiddleNode() {
        Task t1 = new Task("Таск1", "Описание1", "NEW");
        t1.setId(1);
        Task t2 = new Task("Таск2", "Описание2", "NEW");
        t2.setId(2);
        Task t3 = new Task("Таск3", "Описание3", "NEW");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t1, history.get(0));
        assertEquals(t3, history.get(1));
    }

    @Test
    void testRemoveHeadAndTail() {
        Task t1 = new Task("Таск1", "Описание1", "NEW");
        t1.setId(1);
        Task t2 = new Task("Таск2", "Описание2", "NEW");
        t2.setId(2);
        Task t3 = new Task("Таск3", "Описание3", "NEW");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t2, history.get(0));
        assertEquals(t3, history.get(1));

        historyManager.remove(3);
        history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t2, history.getFirst());
    }
}