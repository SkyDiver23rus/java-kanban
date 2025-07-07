package manager;

public class Managers {

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(); // теперь возвращает FileBackedTaskManager с дефолтным файлом
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}