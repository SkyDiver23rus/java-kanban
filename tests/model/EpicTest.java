package model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void testEpicEqualsId() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        Epic epic2 = new Epic("Эпик 2", "Описание 2");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны.");

        epic2.setId(2);

        assertNotEquals(epic1, epic2, "Эпики с разными id не должны быть равны.");
    }
    @Test
    void testEpicNotNull() {

        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(1);

        assertNotEquals(epic, null, "Эпик не null.");
    }
}