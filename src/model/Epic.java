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


    public void calculateFields(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            return;
        }
        Duration total = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        for (Subtask s : subtasks) {
            if (s.getStartTime() != null && s.getDuration() != null) {
                total = total.plus(s.getDuration());
                LocalDateTime subStart = s.getStartTime();
                LocalDateTime subEnd = s.getEndTime();
                if (minStart == null || subStart.isBefore(minStart)) minStart = subStart;
                if (maxEnd == null || (subEnd != null && subEnd.isAfter(maxEnd))) maxEnd = subEnd;
            }
        }
        setDuration(total);
        setStartTime(minStart);

        this.endTime = maxEnd;
    }

    private LocalDateTime endTime;

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}