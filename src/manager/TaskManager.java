package manager;

import model.Task;
import model.Subtask;
import model.Epic;

import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Subtask createSubtask(Subtask subtask);

    Epic createEpic(Epic epic);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    void removeTask(int id);

    void removeSubtask(int id);

    void removeEpic(int id);

    void removeAllTasks();

    void removeAllSubTasks();

    void removeAllEpic();

    List<Task> getHistory();

    List<Subtask> getSubtasksByEpic(int epicId);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    List<Task> getPrioritizedTasks();

}