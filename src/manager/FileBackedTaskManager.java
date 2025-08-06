package manager;

import model.Task;
import model.Epic;
import model.Subtask;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {
    protected final File file;

    protected int nextId = 1;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public FileBackedTaskManager() {
        this(new File("tasks.csv"));
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // skip header
            String line;

            while ((line = reader.readLine()) != null && !line.isBlank()) {
                Task task = CSVTaskConverter.fromString(line);
                int id = task.getId();
                if (id > maxId) maxId = id; // отслеживаем максимальный id
                switch (task.getType()) {
                    case TASK -> manager.tasks.put(id, task);
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> manager.subtasks.put(id, (Subtask) task);
                }
            }

            String historyLine = reader.readLine();
            if (historyLine != null && !historyLine.isBlank()) {
                for (String idStr : historyLine.split(",")) {
                    int id = Integer.parseInt(idStr.trim());
                    Task task = manager.tasks.get(id);
                    if (task == null) task = manager.epics.get(id);
                    if (task == null) task = manager.subtasks.get(id);
                    if (task != null) manager.historyManager.add(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке задач", e);
        }
        manager.nextId = maxId + 1; // устанавливаем следующий id
        return manager;
    }


    // Сохранение всех задач и истории
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : tasks.values()) {
                writer.write(CSVTaskConverter.toString(task));
                writer.newLine();
            }
            for (Epic epic : epics.values()) {
                writer.write(CSVTaskConverter.toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(CSVTaskConverter.toString(subtask));
                writer.newLine();
            }
            writer.newLine(); // разделитель задач и истории
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач", e);
        }
    }


    //  Преобразование истории к строке
    public static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i).getId());
            if (i < history.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    //  Переопределение методов для автосохранения
    @Override
    public Task createTask(Task task) {
        if (checkTaskIntersection(task, -1)) {
            throw new IllegalArgumentException("Task intersects with existing tasks");
        }
        task.setId(generateNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
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
}
   /* public static void main(String[] args) {
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

        // загрузка менеджера из того же файла через статический метод
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
} */