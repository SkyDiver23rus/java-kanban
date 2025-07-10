package manager;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVTaskConverter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getTitle()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        sb.append(task.getDuration() != null ? task.getDuration().toMinutes() : "").append(",");
        sb.append(task.getStartTime() != null ? task.getStartTime().format(FORMATTER) : "");
        if (task.getType() == TaskType.SUBTASK) {
            sb.append(",").append(((Subtask) task).getEpicId());
        } else {
            sb.append(",");
        }
        return sb.toString();
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        String statusStr = fields[3];
        String description = fields[4];

        Duration duration = Duration.ZERO;
        LocalDateTime startTime = null;
        int epicId = -1;

        if (fields.length > 5 && !fields[5].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(fields[5]));
        }
        if (fields.length > 6 && !fields[6].isEmpty()) {
            startTime = LocalDateTime.parse(fields[6]);
        }
        if (fields.length > 7 && !fields[7].isEmpty()) {
            epicId = Integer.parseInt(fields[7]);
        }

        Status status;
        if (statusStr == null || statusStr.isEmpty() || statusStr.equalsIgnoreCase("null")) {
            status = Status.NEW;
        } else {
            status = Status.valueOf(statusStr);
        }

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status.name());
                task.setId(id);
                task.setStatus(status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, status.name(), epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }
}//мусорный коммент