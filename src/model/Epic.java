package model;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
        subtask.setEpicId(id);
    }

    // Удаление сабтаска из этого эпика по id
    public void removeSubtask(int subtaskId) {
        if (subtaskIds.contains(subtaskId)) {
            subtaskIds.remove((Integer) subtaskId);
        }
    }

    // Удаление всех сабтасков из этого эпика
    public void removeAllSubtask() {
        subtaskIds.clear();
    }
}