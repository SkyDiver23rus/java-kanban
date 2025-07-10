package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, "NEW");
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

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    public void removeAllSubtask() {
        subtaskIds.clear();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }


    private LocalDateTime endTime;

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}