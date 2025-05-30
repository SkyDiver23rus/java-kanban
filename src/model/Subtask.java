package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    // Возвращаем ссылку на эпик
    public int getEpicId() {
        return epicId;
    }

    // Реализация не используется.
    // Но вдруг мы решим переместить сабтаск в другой Эпик ))
    public Subtask setEpicId(int epicId) {
        this.epicId = epicId;
        return this;
    }

    @Override
    public String toString() {
        return "SubTask(id=" + id + ",of=Epic(" + getEpicId() + "))";
    }
}