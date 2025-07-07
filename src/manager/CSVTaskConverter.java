package manager;

import model.*;

public class CSVTaskConverter {

    // Преобразовать задачу в CSV-строку
    public static String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getTitle()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task.getType() == TaskType.SUBTASK) {
            sb.append(((Subtask) task).getEpicId());
        } else {
            sb.append("");
        }
        return sb.toString();
    }

    // Преобразовать CSV-строку в задачу
    public static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        String statusStr = fields[3];
        String description = fields[4];

        // Если status пустой или равен "null", используем по умолчанию
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
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(title, description, status.name(), epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }
}