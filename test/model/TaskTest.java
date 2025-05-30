package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void testTaskEqualsId() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");

        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разными id не должны быть равны.");
    }

    @Test
    void testTaskNotEqualsNull() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);

        assertNotEquals(task, null, "Задача не null.");
    }
}
