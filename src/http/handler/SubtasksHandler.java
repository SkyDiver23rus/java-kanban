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
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/"); // /subtasks, /subtasks/{id}, /subtasks/epic/{epicId}

            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /subtasks
            List<Subtask> subtasks = manager.getAllSubtasks();
            sendText(exchange, gson.toJson(subtasks));
        } else if (parts.length == 3) { // /subtasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                Subtask subtask = manager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange, "Подзадача не найдена");
                } else {
                    sendText(exchange, gson.toJson(subtask));
                }
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else if (parts.length == 4 && "epic".equals(parts[2])) { // /subtasks/epic/{epicId}
            try {
                int epicId = Integer.parseInt(parts[3]);
                List<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
                sendText(exchange, gson.toJson(subtasks));
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный epicId");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для GET");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(reader, Subtask.class);
        manager.createSubtask(subtask);
        sendText(exchange, gson.toJson(subtask));
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) { // /subtasks
            manager.removeAllSubTasks();
            sendText(exchange, "Все подзадачи удалены");
        } else if (parts.length == 3) { // /subtasks/{id}
            try {
                int id = Integer.parseInt(parts[2]);
                manager.removeSubtask(id);
                sendText(exchange, "Подзадача удалена");
            } catch (NumberFormatException ex) {
                sendNotAcceptable(exchange, "Некорректный id");
            }
        } else {
            sendNotAcceptable(exchange, "Некорректный эндпоинт для DELETE");
        }
    }
}