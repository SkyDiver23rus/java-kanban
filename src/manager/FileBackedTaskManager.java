package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    protected void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
            writer.write("\n");
            // Сохраняем историю
            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < history.size(); i++) {
                    sb.append(history.get(i).getId());
                    if (i < history.size() - 1) {
                        sb.append(",");
                    }
                }
                writer.write(sb + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + file.getName(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;
        Map<Integer, Task> allTasks = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break;
                Task task = fromString(line);
                switch (task) {
                    case null -> {
                        continue;
                    }
                    case Epic epic -> manager.epics.put(task.getId(), epic);
                    case Subtask subtask -> manager.subtasks.put(task.getId(), subtask);
                    default -> manager.tasks.put(task.getId(), task);
                }
                allTasks.put(task.getId(), task);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
                // восстановление связей с эпиками для Subtask
                if (task instanceof Subtask subtask) {
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask);
                    }
                }
            }
            // Восстановление истории
            String historyLine = reader.readLine();
            if (historyLine != null && !historyLine.isEmpty()) {
                for (String idStr : historyLine.split(",")) {
                    int id = Integer.parseInt(idStr);
                    Task t = allTasks.get(id);
                    if (t != null) manager.historyManager.add(t);
                }
            }
            manager.idCounter = maxId + 1;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла: " + file.getName(), e);
        }
        return manager;
    }

    private static String toString(Task task) {

        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task instanceof Epic ? TaskType.EPIC : task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");
        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

    private static Task fromString(String value) {

        String[] fields = value.split(",", -1);
        if (fields.length < 5) return null;
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        String status = fields[3];
        String description = fields[4];
        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            }
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        Task t = super.createTask(task);
        save();
        return t;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    @Override
    public void removeAllEpic() {
        super.removeAllEpic();
        save();
    }

    public static void main(String[] args) {
        // создаём файл
        File file = new File("tasks.csv");

        // создаём менеджер и добавляем задачи
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Приступить к спринту 8", "пройти первую тему", "NEW");
        Task task2 = new Task("Купить продукты", "Купить хлеб и молоко", "NEW");
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Сделать ремонт", "Полностью обновить комнату");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Купить краску", "Выбрать цвет и купить", "NEW", epic1.getId());
        Subtask subtask2 = new Subtask("Покрасить стены", "В два слоя", "NEW", epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Подготовить отпуск", "Поездка на море");
        manager.createEpic(epic2);

        // Обращаемся к задачам, чтобы появилась история
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());

        // загрузка менеджера из того же файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // проверка, что все задачи, эпики, подзадачи восстановились корректно
        System.out.println("--- Проверка восстановления из файла ---");
        System.out.println("Задачи:");
        for (Task t : loaded.getAllTasks()) {
            System.out.println(t);
        }
        System.out.println("Эпики:");
        for (Epic e : loaded.getAllEpics()) {
            System.out.println(e);
        }
        System.out.println("Подзадачи:");
        for (Subtask s : loaded.getAllSubtasks()) {
            System.out.println(s);
        }
        System.out.println("История:");
        for (Task t : loaded.getHistory()) {
            System.out.println(t);
        }
    }
}