package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = new Gson();

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Subtask> subtasks = manager.getAllSubtasks();
                        sendText(exchange, gson.toJson(subtasks));
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        Subtask subtask = manager.getSubtaskById(id);
                        if (subtask == null) {
                            sendNotFound(exchange, "Подзадача не найдена");
                        } else {
                            sendText(exchange, gson.toJson(subtask));
                        }
                    } else if (query.startsWith("epicId=")) {
                        int epicId = Integer.parseInt(query.replace("epicId=", ""));
                        List<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
                        sendText(exchange, gson.toJson(subtasks));
                    } else {
                        sendNotFound(exchange, "Некорректный запрос");
                    }
                    break;
                case "POST":
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(reader, Subtask.class);
                    manager.createSubtask(subtask);
                    sendText(exchange, gson.toJson(subtask));
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.removeAllSubTasks();
                        sendText(exchange, "Все подзадачи удалены");
                    } else if (query.startsWith("id=")) {
                        int id = Integer.parseInt(query.replace("id=", ""));
                        manager.removeSubtask(id);
                        sendText(exchange, "Подзадача удалена");
                    } else {
                        sendNotFound(exchange, "Некорректный запрос");
                    }
                    break;
                default:
                    sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}