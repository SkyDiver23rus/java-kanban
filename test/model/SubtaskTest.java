package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void testSubtask() {
        Subtask subtask1 = new Subtask("Сабтаск 1", "Описание 1", 100);
        Subtask subtask2 = new Subtask("Сабтаск 2", "Описание 2", 100);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2);
        assertEquals(subtask1.hashCode(), subtask2.hashCode());
    }

    @Test
    public void testSubtaskId() {
        Subtask subtask1 = new Subtask("Сабтаск 1", "Описание 1", 100);
        Subtask subtask2 = new Subtask("Сабтаск 2", "Описание 2", 101);

        subtask1.setId(1);
        subtask2.setId(2);

        assertNotEquals(subtask1, subtask2);
    }

    @Test
    public void testSettersAndGetters() {
        Subtask subtask = new Subtask("Название Сабтаска", "Описание", 100);

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