package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskType;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile();
    }

    public FileBackedTaskManager() {
        this(new File("tasks.csv"));
    }

    // --- Сохранение всех задач и истории ---
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

    // --- Загрузка из файла ---
    protected void loadFromFile() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        historyManager.getHistory().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // пропускаем заголовок
            String line;
            List<String> taskLines = new ArrayList<>();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                taskLines.add(line);
            }
            Map<Integer, Task> allTasks = new HashMap<>();
            for (String taskLine : taskLines) {
                Task task = CSVTaskConverter.fromString(taskLine);
                int id = task.getId();
                if (task.getType() == TaskType.TASK) {
                    tasks.put(id, task);
                    allTasks.put(id, task);
                } else if (task.getType() == TaskType.EPIC) {
                    epics.put(id, (Epic) task);
                    allTasks.put(id, task);
                } else if (task.getType() == TaskType.SUBTASK) {
                    subtasks.put(id, (Subtask) task);
                    allTasks.put(id, task);
                    int epicId = ((Subtask) task).getEpicId();
                    Epic epic = epics.get(epicId);
                    if (epic != null) {
                        epic.addSubtask((Subtask) task);
                    }
                }
                idCounter = Math.max(idCounter, id + 1);
            }
            // читаем историю
            String historyLine = reader.readLine();
            if (historyLine != null && !historyLine.isEmpty()) {
                for (String idStr : historyLine.split(",")) {
                    int id = Integer.parseInt(idStr);
                    Task task = allTasks.get(id);
                    if (task != null) {
                        historyManager.add(task);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // новый файл — пропускаем
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке задач", e);
        }
    }

    // --- Преобразование истории к строке ---
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

    // --- Переопределение методов для автосохранения ---
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
        FileBackedTaskManager loaded = new FileBackedTaskManager();

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
