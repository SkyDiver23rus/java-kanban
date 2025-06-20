package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void testSubtaskEqualsAndHashCode() {
        Subtask subtask1 = new Subtask("Сабтаск 1", "Описание 1", "NEW", 100);
        Subtask subtask2 = new Subtask("Сабтаск 2", "Описание 2", "IN_PROGRESS", 100);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны.");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хэшкоды подзадач с одинаковым id должны совпадать.");
    }

    @Test
    public void testSubtaskNotEquals() {
        Subtask subtask1 = new Subtask("Сабтаск 1", "Описание 1", "NEW", 100);
        Subtask subtask2 = new Subtask("Сабтаск 2", "Описание 2", "NEW", 101);

        subtask1.setId(1);
        subtask2.setId(2);

        assertNotEquals(subtask1, subtask2, "Подзадачи с разными id не должны быть равны.");
    }

    @Test
    public void testSettersAndGetters() {
        Subtask subtask = new Subtask("Название Сабтаска", "Описание", "IN_PROGRESS", 100);

        subtask.setId(456);
        subtask.setStatus(Status.DONE);
        subtask.setEpicId(200);

        assertEquals(456, subtask.getId());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals("Название Сабтаска", subtask.getTitle());
        assertEquals("Описание", subtask.getDescription());
        assertEquals(200, subtask.getEpicId());
    }
}