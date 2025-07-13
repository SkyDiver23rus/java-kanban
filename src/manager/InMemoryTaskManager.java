package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int idCounter = 1;
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    protected int generateNextId() {
        return idCounter++;
    }

    protected boolean checkTaskIntersection(Task newTask, int ignoreId) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) return false;
        for (Task task : prioritizedTasks) {
            if (task.getId() == ignoreId) continue; // игнорируем обновляемую задачу
            if (task.getStartTime() == null || task.getDuration() == null) continue;
            boolean overlaps = !(newTask.getEndTime().isBefore(task.getStartTime()) ||
                    newTask.getStartTime().isAfter(task.getEndTime()));
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    protected void updateEpicFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;
        List<Subtask> epicSubtasks = subtasks.values().stream()
                .filter(s -> s.getEpicId() == epicId)
                .toList();
        // Переносим сюда вычисления:
        if (epicSubtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        Duration total = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        for (Subtask s : epicSubtasks) {
            if (s.getStartTime() != null && s.getDuration() != null) {
                total = total.plus(s.getDuration());
                LocalDateTime subStart = s.getStartTime();
                LocalDateTime subEnd = s.getEndTime();
                if (minStart == null || subStart.isBefore(minStart)) minStart = subStart;
                if (maxEnd == null || (subEnd != null && subEnd.isAfter(maxEnd))) maxEnd = subEnd;
            }
        }
        epic.setDuration(total);
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

    @Override
    public Task createTask(Task task) {
        checkTaskIntersection(task, -1);
        task.setId(generateNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task); // добавляем в приоритетные задачи
        return task;
    }


    @Override
    public Subtask createSubtask(Subtask subtask) {
        checkTaskIntersection(subtask, -1);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            subtask.setId(generateNextId());
            epic.addSubtask(subtask);
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
            updateEpicStatus(subtask.getEpicId());
            updateEpicFields(subtask.getEpicId());
            return subtask;
        }
        return null;
    }

    @Override
    public Epic createEpic(Epic epic) {
        checkTaskIntersection(epic, -1);
        epic.setId(generateNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);

        }
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }


    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
                updateEpicFields(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }  // удаляем все подзадачи эпика из истории и коллекции подзадач

    @Override
    public void removeAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();

        //  удаляем из приоритезированной коллекции
        if (prioritizedTasks != null) {
            prioritizedTasks.removeIf(task -> task.getType() == TaskType.TASK);
        }
    }

    @Override
    public void removeAllSubTasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(Epic::removeAllSubtask);

        // Также удаляем из приоритезированной коллекции
        if (prioritizedTasks != null) {
            prioritizedTasks.removeIf(task -> task.getType() == TaskType.SUBTASK);
        }
    }

    @Override
    public void removeAllEpic() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                historyManager.remove(subtaskId);
            }
        }
        epics.clear();
        subtasks.clear();

        // Также очищаем приоритезированную коллекцию
        if (prioritizedTasks != null) {
            prioritizedTasks.clear();
        }
    } //и эпики тоже

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return List.of();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;

        prioritizedTasks.remove(tasks.get(task.getId()));

        // Проверка на пересечение: исключаем текущую задачу из проверки
        if (checkTaskIntersection(task, task.getId())) {
            // Можно вернуть ошибку или просто не обновлять
            prioritizedTasks.add(tasks.get(task.getId())); // возвращаем старую задачу обратно в приоритеты
            return;
        }

        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;

        prioritizedTasks.remove(subtasks.get(subtask.getId()));

        // Проверка на пересечение: исключаем текущую подзадачу из проверки
        if (checkTaskIntersection(subtask, subtask.getId())) {
            prioritizedTasks.add(subtasks.get(subtask.getId()));
            return;
        }

        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
        updateEpicStatus(subtask.getEpicId());
        updateEpicFields(subtask.getEpicId());
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }


    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(model.Status.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;
        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != model.Status.DONE) {
                    allDone = false;
                }
                if (subtask.getStatus() != model.Status.NEW) {
                    allNew = false;
                }
            }
        }
        if (allDone) {
            epic.setStatus(model.Status.DONE);
        } else if (allNew) {
            epic.setStatus(model.Status.NEW);
        } else {
            epic.setStatus(model.Status.IN_PROGRESS);
        }
    }


}