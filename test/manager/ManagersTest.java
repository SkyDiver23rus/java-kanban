package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    void testGetDefault() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Метод getDefault  возвращает объект TaskManager.");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Метод getDefault возвращает экземпляр InMemoryTaskManager.");
    }

    @Test
    void testGetDefaultHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Метод getDefaultHistory возвращает объект HistoryManager.");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Метод getDefaultHistory возвращаеть экземпляр InMemoryHistoryManager.");
    }
}