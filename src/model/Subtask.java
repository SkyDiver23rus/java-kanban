package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, String status, int epicId) {
        super(title, description, status);
        this.setStatus(Status.valueOf(status));
        this.epicId = epicId;
    }

    // Возвращаем ссылку на эпик
    public int getEpicId() {
        return epicId;
    }


    // Но вдруг мы решим переместить сабтаск в другой Эпик ))
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask(id=" + id + ",of=Epic(" + getEpicId() + "))";
    }
}