import manager.Managers;
import manager.TaskManager;
import model.Task;
import model.Subtask;
import model.Epic;
import model.Status;

public class Main {

    private static void printHistory(TaskManager manager) {
        System.out.println("=====================");
        System.out.println("История просмотров:");
        System.out.println("=====================");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        //Создание задач и эпиков
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStatus(Status.NEW);
        manager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        task2.setStatus(Status.IN_PROGRESS);
        manager.createTask(task2);

        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Эпик 1");
        manager.createEpic(epicWithSubtasks);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicWithSubtasks.getId());
        subtask1.setStatus(Status.NEW);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicWithSubtasks.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", epicWithSubtasks.getId());
        subtask3.setStatus(Status.DONE);
        manager.createSubtask(subtask3);

        Epic epicWithoutSubtasks = new Epic("Эпик без подзадач", "Эпик 2");
        manager.createEpic(epicWithoutSubtasks);

        // Просмотр задач в разном порядке в истории
        System.out.println("Просмотр задач в разном порядке в истории:");

        manager.getTaskById(task1.getId());
        printHistory(manager);

        manager.getEpicById(epicWithSubtasks.getId());
        printHistory(manager);

        manager.getTaskById(task2.getId());
        printHistory(manager);

        manager.getSubtaskById(subtask1.getId());
        printHistory(manager);

        manager.getSubtaskById(subtask2.getId());
        printHistory(manager);

        manager.getEpicById(epicWithoutSubtasks.getId());
        printHistory(manager);

        manager.getSubtaskById(subtask3.getId());
        printHistory(manager);

        // Проверка на дубли и обновление порядкаа
        manager.getTaskById(task1.getId());
        printHistory(manager);

        manager.getEpicById(epicWithSubtasks.getId());
        printHistory(manager);


        System.out.println("=====================");
        System.out.println("Удаляем задачу 1 из истории:");
        manager.removeTask(task1.getId());
        printHistory(manager);

        System.out.println("=====================");
        System.out.println("Удаляем эпик с тремя подзадачами:");
        manager.removeEpic(epicWithSubtasks.getId());
        printHistory(manager);

    }
}